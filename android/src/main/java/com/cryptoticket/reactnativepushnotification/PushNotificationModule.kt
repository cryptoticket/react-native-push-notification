package com.cryptoticket.reactnativepushnotification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import com.google.firebase.messaging.FirebaseMessaging
import java.io.IOException


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
    companion object {
        val DEFAULT_ACTIVITY = "com.cryptoticket.reactnativepushnotification.default_activity"
    }


    /**
     * Default push notification broadcast receiver class name
     */
    val DEFAULT_BROADCAST_RECEVIER_CLASSNAME = "com.cryptoticket.reactnativepushnotification.PushNotificationBroadcastReceiver"

    /**
     * Meta key from AndroidManifest.xml for default broadcast receiver classname
     */
    val META_KEY_DEFAULT_BROADCAST_RECEVIER_CLASSNAME = "com.cryptoticket.reactnativepushnotification.default_broadcast_receiver"

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
     * Returns default broadcast receiver class name
     */
    fun getDefaultBroadcastReceiverClassName(): String {
        // if target broadcast receiver exists in AndroidManifest.xml then use it, else use default broadcast receiver
        var defaultBroadcastReceiverClassName = reactApplicationContext.packageManager.getApplicationInfo(reactApplicationContext.packageName, PackageManager.GET_META_DATA).metaData.getString(META_KEY_DEFAULT_BROADCAST_RECEVIER_CLASSNAME)
        if(defaultBroadcastReceiverClassName == null) {
            defaultBroadcastReceiverClassName = DEFAULT_BROADCAST_RECEVIER_CLASSNAME
        }
        return defaultBroadcastReceiverClassName
    }

    /**
     * Cancels scheduled notification by its id
     *
     * @param notificationId scheduled notification id to be cancelled
     */
    @ReactMethod
    fun cancelScheduledNotification(notificationId: Int) {
        // recreate pending intent
        val mainIntent = Intent(PushNotificationBroadcastReceiver.Actions.SHOW_SCHEDULED_NOTIFICATION)
        mainIntent.component = ComponentName(reactApplicationContext, getDefaultBroadcastReceiverClassName())
        val pendingIntent = PendingIntent.getBroadcast(reactApplicationContext, notificationId, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        // cancel notification via alarm manager
        val alarmManager = reactApplicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
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
        try {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        promise.resolve(token)
                    } else {
                        promise.reject("E_FIREBASE_DEVICE_TOKEN", "Unable to retrieve device FCM token")
                    }
                })
        } catch (err: IOException) {
            promise.reject("E_FIREBASE_DEVICE_TOKEN", "Firebase instance is not available");
        }
    }

    /**
     * Shows push notification
     *
     * @param notificationId notification id, needed in case we would want to modify a notification
     * @param template template id
     * @param channelId notification channel id
     * @param data notification data attributes, for different templates there are different data attributes
     * @param priority notification priority, used for backward compatibility with android <= 7 (SDK <= 25), android >= 8 uses channels
     * @param badgeNumber app icon badge number
     * @param showAt unix timestamp when push notification should be shown, can be used for scheduled notifications, if showAt == 0 then show right now
     */
    @ReactMethod
    fun show(notificationId: Int, template: Int, channelId: String, data: ReadableMap, priority: Int = NotificationCompat.PRIORITY_DEFAULT, badgeNumber: Int = 0, showAt: Int = 0) {

        var mainIntentAction = PushNotificationBroadcastReceiver.Actions.PRESS_ON_NOTIFICATION
        // if delaySeconds > 0 then notification is scheduled
        if (showAt > 0) mainIntentAction = PushNotificationBroadcastReceiver.Actions.SHOW_SCHEDULED_NOTIFICATION

        // prepare pending intent that opens main activity
        val mainIntent = Intent(mainIntentAction)
        mainIntent.component = ComponentName(reactApplicationContext, getDefaultBroadcastReceiverClassName())
        // add all notification data attributes to intent extra params
        data.entryIterator.forEach {
            mainIntent.putExtra(it.key, if (it.value == null) null else it.value.toString())
        }
        val pendingIntent = PendingIntent.getBroadcast(reactApplicationContext, notificationId, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // prepare base notification builder
        val builder = NotificationCompat.Builder(reactApplicationContext, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(priority)
                .setNumber(badgeNumber)

        // template common push notification
        if(template == Templates.COMMON) {
            builder.apply {
                setContentTitle(data.getString("title"))
                setContentText(data.getString("message"))
                setStyle(NotificationCompat.BigTextStyle().bigText(data.getString("message")))
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
                    openUrlIntent.component = ComponentName(reactApplicationContext, getDefaultBroadcastReceiverClassName())
                    openUrlIntent.putExtra("url", data.getString("url"))
                    val openUrlPendingIntent = PendingIntent.getBroadcast(reactApplicationContext, 0, openUrlIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    builder.setContentIntent(openUrlPendingIntent)
                }
            }
            // on check button click send CLOSE_NOTIFICATION action to broadcast receiver that closes notification
            val closeNotificationIntent = Intent(PushNotificationBroadcastReceiver.Actions.CLOSE_NOTIFICATION)
            closeNotificationIntent.component = ComponentName(reactApplicationContext, getDefaultBroadcastReceiverClassName())
            closeNotificationIntent.putExtra("id", notificationId)
            val closeNotificationPendingIntent = PendingIntent.getBroadcast(reactApplicationContext, notificationId, closeNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            remoteViews.setOnClickPendingIntent(R.id.buttonCheck, closeNotificationPendingIntent)
            // set notification template
            builder.setContent(remoteViews)
        }

        // if show at is 0 then notification is not scheduled, show right now
        if (showAt == 0) {
            // show notification
            NotificationManagerCompat.from(reactApplicationContext).notify(notificationId, builder.build())
        } else {
            // if android version < 6 then show notification right now because alarm manager with exact time is not supported there
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                NotificationManagerCompat.from(reactApplicationContext).notify(notificationId, builder.build())
            } else {
                // schedule notification via alarm manager
                val alarmManager = reactApplicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                // convert showAt to "long" type because int timestamp is overflowed after multiplication by 1000 (RN supports only "int" function param types for numbers)
                val showAtMilliseconds = showAt.toLong() * 1000
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, showAtMilliseconds, pendingIntent)
            }
        }
    }

}
