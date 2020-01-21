package com.cryptoticket.reactnativepushnotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

/**
 * Push notification broadcast receiver.
 * Used to handle messages on interaction with notifications. (Ex: on button clicks in notification)
 */
class PushNotificationBroadcastReceiver : BroadcastReceiver() {

    /**
     * Actions that can be broadcasted to this receiver
     */
    object Actions {
        val CLOSE_NOTIFICATION = "com.cryptoticket.reactnativepushnotification.action.CLOSE_NOTIFICATION"
    }

    /**
     * On broadcast message receive
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        // on CLOSE_NOTIFICATION action hide push notification by passed id
        if(intent!!.action.equals(Actions.CLOSE_NOTIFICATION)) {
            val id = intent!!.getIntExtra("id", 0)
            NotificationManagerCompat.from(context!!).cancel(id)
        }

    }
}
