package com.techexactly.eventmanager

import android.app.Application
import com.techexactly.eventmanager.notification.NotificationHelper
import com.techexactly.eventmanager.util.ThemeHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

class EventApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Offline persistence (enabled by default on Android – configured explicitly for clarity).
        FirebaseFirestore.getInstance().firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(
                PersistentCacheSettings.newBuilder()
                    .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build()
            )
            .build()

        ThemeHelper.applySaved(this)
        NotificationHelper.createChannel(this)
    }
}
