package com.cryptoticket.reactnativepushnotification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CustomFirebaseMessagingService : FirebaseMessagingService() {

    val CUSTOM_FIREBASE_TAG = "FIREBASE"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(CUSTOM_FIREBASE_TAG, "Notification received")
        Log.d(CUSTOM_FIREBASE_TAG, "From: ${remoteMessage.from}")

        // check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d(CUSTOM_FIREBASE_TAG, "Notification payload")
            Log.d(CUSTOM_FIREBASE_TAG, "Title: ${it.title}")
            Log.d(CUSTOM_FIREBASE_TAG, "Body: ${it.body}")
        }

        // check if message contains a data payload
        remoteMessage.data.isNotEmpty().let {
            Log.d(CUSTOM_FIREBASE_TAG, "Data payload")
            Log.d(CUSTOM_FIREBASE_TAG, "Payload: ${remoteMessage.data}")
        }

        // show notification
        // showNotification()
    }

}