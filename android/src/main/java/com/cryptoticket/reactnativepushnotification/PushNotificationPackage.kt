package com.cryptoticket.reactnativepushnotification

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import java.util.*
import kotlin.collections.ArrayList

class PushNotificationPackage : ReactPackage {

    override fun createNativeModules(reactContext: ReactApplicationContext): MutableList<NativeModule> {
        val modules = ArrayList<NativeModule>()
        modules.add(PushNotificationModule(reactContext))
        return modules
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): MutableList<ViewManager<*, *>> {
        return Collections.emptyList<ViewManager<*, *>>()
    }
}
