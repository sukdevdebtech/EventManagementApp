package com.techexactly.eventmanager.notification

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

/** Stores this device's FCM token at users/{uid}.fcmToken so a backend can send reminders. */
object FcmTokenSaver {

    fun saveCurrentToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { save(it) }
    }

    fun save(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users").document(uid)
            .set(mapOf("fcmToken" to token), SetOptions.merge())
    }
}
