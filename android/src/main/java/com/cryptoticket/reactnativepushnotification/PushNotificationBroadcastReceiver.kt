package com.cryptoticket.reactnativepushnotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationManagerCompat

/**
 * Push notification broadcast receiver.
 * Used to handle messages on interaction with notifications. (Ex: on button clicks in notification)
 */
open class PushNotificationBroadcastReceiver : BroadcastReceiver() {

    /**
     * Actions that can be broadcasted to this receiver
     */
    object Actions {
        val CLOSE_NOTIFICATION = "com.cryptoticket.reactnativepushnotification.action.CLOSE_NOTIFICATION"
        val OPEN_URL = "com.cryptoticket.reactnativepushnotification.action.OPEN_URL"
        val PRESS_ON_NOTIFICATION = "com.cryptoticket.reactnativepushnotification.action.PRESS_ON_NOTIFICATION"
    }

    /**
     * On broadcast message receive
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        // on CLOSE_NOTIFICATION action hide push notification by passed id
        if(intent?.action.equals(Actions.CLOSE_NOTIFICATION)) {
            val id = intent?.getIntExtra("id", 0)
            NotificationManagerCompat.from(context!!).cancel(id!!)
        }
        // on OPEN_URL action open url in browser
        if(intent?.action.equals(Actions.OPEN_URL)) {
            val url = intent?.getStringExtra("url")
            val openUrlIntent = Intent(Intent.ACTION_VIEW).apply {
                setData(Uri.parse(url))
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context!!.startActivity(openUrlIntent)
        }
        // on PRESS_ON_NOTIFICATION action open app or open app via deep link
        if(intent?.action.equals(Actions.PRESS_ON_NOTIFICATION)) {
            onNotificationPress(context, intent)
        }
    }

    /**
     * Open main activity by default
     */
    open fun onNotificationPress(context: Context?, intent: Intent?) {
        val mainIntent = Intent()
        mainIntent.setClassName(
                context,
                context?.packageManager?.getApplicationInfo(context?.packageName, PackageManager.GET_META_DATA)?.metaData?.getString(PushNotificationModule.DEFAULT_ACTIVITY)
        )
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(mainIntent)
    }
}
