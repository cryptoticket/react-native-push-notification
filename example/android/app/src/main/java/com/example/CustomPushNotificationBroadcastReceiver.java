package com.example;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.cryptoticket.reactnativepushnotification.PushNotificationBroadcastReceiver;

public class CustomPushNotificationBroadcastReceiver extends PushNotificationBroadcastReceiver {
    @Override
    public void onNotificationPress(Context context, Intent intent) {
        boolean shouldHandleDeepLink = true;
        if(shouldHandleDeepLink) {
            // list all notification data attributes
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                for (String key : bundle.keySet()) {
                    Log.d("CUSTOM RECEIVER", key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
                }
            }
            // open app by deep link
            Intent mainIntent = new Intent(Intent.ACTION_VIEW);
            mainIntent.setData(Uri.parse("app://content/1"));
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainIntent);
        } else {
            // open main app activity by default
            super.onNotificationPress(context, intent);
        }
    }
}
