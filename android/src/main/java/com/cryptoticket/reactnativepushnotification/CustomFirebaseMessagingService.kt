package com.cryptoticket.reactnativepushnotification

import android.content.pm.PackageManager
import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableNativeMap
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

open class CustomFirebaseMessagingService : FirebaseMessagingService() {

    val CUSTOM_FIREBASE_TAG = "FIREBASE"
    val DEFAULT_CHANNEL = "com.cryptoticket.reactnativepushnotification.default_channel_id"
    val NOTIFICATION_DATA_ATTRIBUTES = arrayOf("title", "message", "media", "url")

    /**
     * On remote push notification receive callback. Shows push notification.
     *
     * @param remoteMessage push notification data
     */
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

        // initialize empty react native map
        val rnMap = WritableNativeMap()
        NOTIFICATION_DATA_ATTRIBUTES.forEach {
            rnMap.putString(it, null)
        }
        // convert push notification attributes to react native map
        for((key, value) in remoteMessage.data) {
            rnMap.putString(key, value)
        }

        // show notification
        val module = PushNotificationModule(ReactApplicationContext(applicationContext))
        val notificationId = (0..9999999).random()
        val channelId = applicationContext.packageManager.getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA).metaData.getString(DEFAULT_CHANNEL)!!
        // default template is common
        var templateId = PushNotificationModule.Templates.COMMON
        // check if template is for event
        if(!remoteMessage.data.get("media").isNullOrEmpty() || !remoteMessage.data.get("url").isNullOrEmpty()) {
            templateId = PushNotificationModule.Templates.EVENT
        }
        module.show(notificationId, templateId, channelId, rnMap)
    }

}
