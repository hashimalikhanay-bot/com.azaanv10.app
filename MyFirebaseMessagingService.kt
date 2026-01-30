package com.azzan.app

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Service to handle incoming FCM messages.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM_SERVICE", "Message received from: ${remoteMessage.from}")

        // Check if message contains a notification or data payload
        // We use our helper to ensure high priority display
        NotificationHelper.showAzaanNotification(applicationContext)
        
        // Detailed logging for debugging
        remoteMessage.notification?.let {
            Log.d("FCM_SERVICE", "Notification Title: ${it.title}")
            Log.d("FCM_SERVICE", "Notification Body: ${it.body}")
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM_SERVICE", "Refreshed token: $token")
    }
}