package com.cryptoticket.reactnativepushnotification

import android.widget.Toast
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class PushNotificationModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "PushNotificationAndroid"
    }

    @ReactMethod
    fun show(message: String, duration: Int) {
        Toast.makeText(reactApplicationContext, message, duration).show()
    }

}