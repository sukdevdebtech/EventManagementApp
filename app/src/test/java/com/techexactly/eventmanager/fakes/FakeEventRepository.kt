package com.techexactly.eventmanager.fakes

import com.techexactly.eventmanager.data.model.Event
import com.techexactly.eventmanager.data.repository.EventRepository
import com.techexactly.eventmanager.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory repository used in unit tests (mimics Firestore's newest-first ordering). */
class FakeEventRepository : EventRepository {

    private val events = MutableStateFlow<List<Event>>(emptyList())
    private var nextId = 1
    var failWith: String? = null

    override fun observeEvents(): Flow<Resource<List<Event>>> = events.map { list ->
        Resource.Success(list.sortedByDescending { it.dateTime.toDate().time })
    }

    override suspend fun getEvent(id: String): Resource<Event> =
        events.value.firstOrNull { it.id == id }?.let { Resource.Success(it) } ?: Resource.Error("Event not found.")

    override suspend fun addEvent(event: Event): Resource<Unit> {
        failWith?.let { return Resource.Error(it) }
        events.value = events.value + event.copy(id = "id${nextId++}")
        return Resource.Success(Unit)
    }

    override suspend fun updateEvent(event: Event): Resource<Unit> {
        failWith?.let { return Resource.Error(it) }
        events.value = events.value.map { if (it.id == event.id) event else it }
        return Resource.Success(Unit)
    }

    override suspend fun deleteEvent(id: String): Resource<Unit> {
        failWith?.let { return Resource.Error(it) }
        events.value = events.value.filterNot { it.id == id }
        return Resource.Success(Unit)
    }
}
