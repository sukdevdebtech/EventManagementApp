package com.techexactly.eventmanager.data.repository

import com.techexactly.eventmanager.data.model.Event
import com.techexactly.eventmanager.util.FirebaseErrorMapper
import com.techexactly.eventmanager.util.Resource
import com.techexactly.eventmanager.util.safeCall
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

interface EventRepository {
    /** Real-time stream of the user's events, newest first. */
    fun observeEvents(): Flow<Resource<List<Event>>>
    suspend fun getEvent(id: String): Resource<Event>
    suspend fun addEvent(event: Event): Resource<Unit>
    suspend fun updateEvent(event: Event): Resource<Unit>
    suspend fun deleteEvent(id: String): Resource<Unit>
}

class FirestoreEventRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : EventRepository {

    private fun eventsRef(): CollectionReference {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("You are not logged in.")
        return db.collection("users").document(uid).collection("events")
    }

    override fun observeEvents(): Flow<Resource<List<Event>>> = callbackFlow {
        if (auth.currentUser == null) {
            trySend(Resource.Error("You are not logged in."))
            close()
            return@callbackFlow
        }
        val registration = eventsRef()
            .orderBy("dateTime", Query.Direction.DESCENDING) // reverse chronological
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> trySend(Resource.Error(FirebaseErrorMapper.map(error)))
                    snapshot != null -> trySend(Resource.Success(snapshot.toObjects(Event::class.java)))
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getEvent(id: String): Resource<Event> = safeCall {
        val ref = eventsRef().document(id)
        // Prefer the local cache (fast + works offline); fall back to the server.
        val snapshot = try {
            ref.get(Source.CACHE).await()
        } catch (e: FirebaseFirestoreException) {
            ref.get().await()
        }
        snapshot.toObject(Event::class.java) ?: throw IllegalStateException("Event not found.")
    }

    override suspend fun addEvent(event: Event) = safeCall {
        eventsRef().document().set(event.copy(createdAt = Timestamp.now())).awaitOrQueue()
    }

    override suspend fun updateEvent(event: Event) = safeCall {
        eventsRef().document(event.id).update(
            mapOf(
                "title" to event.title,
                "description" to event.description,
                "dateTime" to event.dateTime,
                "location" to event.location,
                "reminderSent" to false
            )
        ).awaitOrQueue()
    }

    override suspend fun deleteEvent(id: String) = safeCall {
        eventsRef().document(id).delete().awaitOrQueue()
    }

    /**
     * With offline persistence enabled, Firestore write Tasks only complete once the server
     * acknowledges. When offline that could take forever, so after a short timeout we treat the
     * write as "queued locally" - Firestore syncs it automatically when back online, and the
     * snapshot listener already shows the change.
     */
    private suspend fun Task<Void>.awaitOrQueue() {
        try {
            withTimeout(WRITE_TIMEOUT_MS) { await() }
        } catch (_: TimeoutCancellationException) {
            // queued offline - nothing to do
        }
    }

    private companion object {
        const val WRITE_TIMEOUT_MS = 2_500L
    }
}
