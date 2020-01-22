package com.cryptoticket.reactnativepushnotification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.facebook.react.bridge.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId


class PushNotificationModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    /**
     * Notification templates
     */
    object Templates {
        val COMMON = 0
        val EVENT = 1
    }

    /**
     * Default activity meta key from android manifest
     */
    val DEFAULT_ACTIVITY = "com.cryptoticket.reactnativepushnotification.default_activity"

    /**
     * Push notification broadcast receiver class name
     */
    val BROADCAST_RECEVIER_CLASSNAME = "com.cryptoticket.reactnativepushnotification.PushNotificationBroadcastReceiver"

    /**
     * Returns module that should be used in React Native
     *
     * Ex(js):
     * import {PushNotificationAndroid} from "@cryptoticket/react-native-push-notification"
     *
     * @return React Native module name
     */
    override fun getName(): String {
        return "PushNotificationAndroid"
    }

    /**
     * Returns constants that can be used in React Native
     *
     * Ex(js):
     * import {PushNotificationAndroid} from "@cryptoticket/react-native-push-notification"
     * console.log(PushNotificationAndroid.CHANNEL_IMPORTANCE_NONE);
     *
     * @return map object with constants
     */
    override fun getConstants(): MutableMap<String, Any> {
        val constants = mutableMapOf<String, Any>()
        // notification channel importance levels
        constants.put("CHANNEL_IMPORTANCE_NONE", NotificationManager.IMPORTANCE_NONE)
        constants.put("CHANNEL_IMPORTANCE_MIN", NotificationManager.IMPORTANCE_MIN)
        constants.put("CHANNEL_IMPORTANCE_LOW", NotificationManager.IMPORTANCE_LOW)
        constants.put("CHANNEL_IMPORTANCE_DEFAULT", NotificationManager.IMPORTANCE_DEFAULT)
        constants.put("CHANNEL_IMPORTANCE_HIGH", NotificationManager.IMPORTANCE_HIGH)
        constants.put("CHANNEL_IMPORTANCE_MAX", NotificationManager.IMPORTANCE_MAX)
        // notification priorities (used for compatibility with android <= 7(SDK <= 25))
        constants.put("PRIORITY_MIN", NotificationCompat.PRIORITY_MIN)
        constants.put("PRIORITY_LOW", NotificationCompat.PRIORITY_LOW)
        constants.put("PRIORITY_DEFAULT", NotificationCompat.PRIORITY_DEFAULT)
        constants.put("PRIORITY_HIGH", NotificationCompat.PRIORITY_HIGH)
        constants.put("PRIORITY_MAX", NotificationCompat.PRIORITY_MAX)
        // notification templates
        constants.put("TEMPLATE_COMMON", Templates.COMMON)
        constants.put("TEMPLATE_EVENT", Templates.EVENT)
        return constants
    }

    /**
     * Creates notification channel.
     * Notification channels are required from android 8 (SDK 26).
     * This methods can be called multiple times, channels are not recreated.
     *
     * @param id notification channel id, used on showing push notification
     * @param name notification channel name, displayed in app push notification settings
     * @param desc notification channel description
     * @param importance notification channel importance, the more importance the more chances user will see a notification
     */
    @ReactMethod
    fun createChannel(id: String, name: String, desc: String, importance: Int) {
        // create channel only if API is available in SDK, android >= 8 (SDK >= 26)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, name, importance).apply {
                description = desc
            }
            val notificationManager: NotificationManager = reactApplicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Returns device FCM token in React Native promise
     *
     * @param promise React Native promise
     */
    @ReactMethod
    fun getDeviceToken(promise: Promise) {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    promise.reject("E_FIREBASE_DEVICE_TOKEN", "Unable to retrieve device FCM token")
                }
                val token = task.result?.token
                promise.resolve(token)
            })
    }

    /**
     * Shows push notification
     *
     * @param notificationId notification id, needed in case we would want to modify a notification
     * @param template template id
     * @param channelId notification channel id
     * @param data notification data attributes, for different templates there are different data attributes
     * @param priority notification priority, used for backward compatibility with android <= 7 (SDK <= 25), android >= 8 uses channels
     */
    @ReactMethod
    fun show(notificationId: Int, template: Int, channelId: String, data: ReadableMap, priority: Int = NotificationCompat.PRIORITY_DEFAULT) {

        // prepare pending intent that opens main activity by class name from android manifest
        val mainIntent = Intent()
        mainIntent.setClassName(
                reactApplicationContext,
                reactApplicationContext.packageManager.getApplicationInfo(reactApplicationContext.packageName, PackageManager.GET_META_DATA).metaData.getString(DEFAULT_ACTIVITY)
        )
        val pendingIntent = TaskStackBuilder.create(reactApplicationContext).run {
            addNextIntentWithParentStack(mainIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        // prepare base notification builder
        val builder = NotificationCompat.Builder(reactApplicationContext, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(priority)

        // template common push notification
        if(template == Templates.COMMON) {
            builder.apply {
                setContentTitle(data.getString("title"))
                setContentText(data.getString("message"))
            }
        }

        // template event push notification
        if(template == Templates.EVENT) {
            val remoteViews = RemoteViews(reactApplicationContext.packageName, R.layout.notification_template_event)
            // set title
            if(!data.isNull("title")) {
                remoteViews.setViewVisibility(R.id.textViewTitle, View.VISIBLE)
                remoteViews.setTextViewText(R.id.textViewTitle, data.getString("title"))
            }
            // set message
            if(!data.isNull("message")) {
                remoteViews.setViewVisibility(R.id.textViewMessage, View.VISIBLE)
                remoteViews.setTextViewText(R.id.textViewMessage, data.getString("message"))
            }
            // set event media image
            if(!data.isNull("media")) {
                if(!data.getString("media")!!.isEmpty()) {
                    remoteViews.setViewVisibility(R.id.imageViewMedia, View.VISIBLE)
                    val bitmap = Glide.with(reactApplicationContext).asBitmap().load(data.getString("media")).submit().get()
                    remoteViews.setImageViewBitmap(R.id.imageViewMedia, bitmap)
                }
            }
            // if url param exists open this url in browser on notification click
            if(!data.isNull("url")) {
                if(!data.getString("url")!!.isEmpty()) {
                    val openUrlIntent = Intent(PushNotificationBroadcastReceiver.Actions.OPEN_URL)
                    openUrlIntent.component = ComponentName(reactApplicationContext, BROADCAST_RECEVIER_CLASSNAME)
                    openUrlIntent.putExtra("url", data.getString("url"))
                    val openUrlPendingIntent = PendingIntent.getBroadcast(reactApplicationContext, 0, openUrlIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                    builder.setContentIntent(openUrlPendingIntent)
                }
            }
            // on check button click send CLOSE_NOTIFICATION action to broadcast receiver that closes notification
            val closeNotificationIntent = Intent(PushNotificationBroadcastReceiver.Actions.CLOSE_NOTIFICATION)
            closeNotificationIntent.component = ComponentName(reactApplicationContext, BROADCAST_RECEVIER_CLASSNAME)
            closeNotificationIntent.putExtra("id", notificationId)
            val closeNotificationPendingIntent = PendingIntent.getBroadcast(reactApplicationContext, notificationId, closeNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            remoteViews.setOnClickPendingIntent(R.id.buttonCheck, closeNotificationPendingIntent)
            // set notification template
            builder.setContent(remoteViews)
        }

        // show notification
        NotificationManagerCompat.from(reactApplicationContext).notify(notificationId, builder.build())
    }

}
