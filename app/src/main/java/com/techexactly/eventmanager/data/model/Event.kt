package com.techexactly.eventmanager.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Firestore path: users/{uid}/events/{eventId}
 * All properties have defaults so Firestore can deserialize into this class.
 */
data class Event(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val dateTime: Timestamp = Timestamp.now(),
    val location: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val reminderSent: Boolean = false
)
