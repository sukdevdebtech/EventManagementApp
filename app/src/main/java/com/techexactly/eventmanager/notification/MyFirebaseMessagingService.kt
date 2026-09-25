package com.techexactly.eventmanager.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        FcmTokenSaver.save(token)
    }

    /** Called when a message arrives while the app is in the foreground (or a data-only message). */
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "Event reminder"
        val body = message.notification?.body ?: message.data["body"].orEmpty()
        NotificationHelper.show(this, title, body)
    }
}
