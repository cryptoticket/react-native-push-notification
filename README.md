# React Native Push Notification

This package helps to show:
- local and remote(firebase) push notifications
- push notifications with custom layout both local and remote

Current version is for **Android only**.

## Correct way to send push notifications from server

There are 3 types of notifications:
- common notification (title + message)
- notification only with custom data attributes
- common notification with data attributes

When app is in background only plain data notifications can be customized (firebase *onMessageRecieve()* is fired only for them). That is why from a backend part you should send push notifications only with data attributes. Example:
```
{
	data: {
		title: "push notification title",
		message: "push notification body",
		media: "https://example.com/image.png", // optional
		url: "https://google.com" // optional
	}
}
```
More info [here](https://firebase.google.com/docs/cloud-messaging/android/receive). Twilio example [here](scripts/send-test-notification.js) (NOTICE: you should set twilio parameters: *account id, auth token, service id, device fcm token*).

## How to install
1. Install npm package
```
npm install @cryptoticket/react-native-push-notification --save
```
2. Following [this](https://firebase.google.com/docs/android/setup) tutorial make the following:
- add generated `google-services.json` file to `android/app` folder
- add google services dependency to `<project>/build.gradle`:
```
buildscript {
	// ... other settings
	dependencies {
		// ... other dependencies
		classpath 'com.google.gms:google-services:4.3.2' // add this line with latest version
	}
}
```
- apply google services plugin to `<project>/<app-module>/build.gradle`:
```
apply plugin: 'com.google.gms.google-services'  // add this line
```
3. Add to your app android manifest to the application tag the following code:
```
 <!-- start notification settings -->
 <meta-data
    android:name="com.cryptoticket.reactnativepushnotification.default_activity"
    android:value="com.example.MainActivity" />
 <meta-data
 	android:name="com.cryptoticket.reactnativepushnotification.default_channel_id"
 	android:value="my_channel_id" />
 <meta-data
   android:name="com.google.firebase.messaging.default_notification_icon"
   android:resource="@drawable/ic_notification" />
 <meta-data 
 	android:name="com.google.firebase.messaging.default_notification_color"
 	android:resource="@android:color/black" />
 <service
   android:name="com.cryptoticket.reactnativepushnotification.CustomFirebaseMessagingService"
   android:exported="false">
     <intent-filter>
     	<action android:name="com.google.firebase.MESSAGING_EVENT" />
     </intent-filter>
 </service>
 <receiver android:name="com.cryptoticket.reactnativepushnotification.PushNotificationBroadcastReceiver"  android:exported="true">
 	<intent-filter>
 		<action android:name="com.cryptoticket.reactnativepushnotification.action.CLOSE_NOTIFICATION"/>
 		<category android:name="android.intent.category.DEFAULT"/>
 	</intent-filter>
 </receiver>
 <!-- end notification settings -->
```
Manifest explanation:
- **meta-data(com.cryptoticket.reactnativepushnotification.default_activity)**: your application default activity main. When user clicks on push notification this activity will be opened. Usually you should set here the string similar to *com.yourpackage.MainActivity*.
- **meta-data(com.cryptoticket.reactnativepushnotification.default_channel_id):** default notification channel name for remote notifications. By default all local and remote notifications will use this name. NOTICE: you should manually create this channel on package init(check the *PushNotificationAndroid.createChannel()* API). 
- **meta-data(com.google.firebase.messaging.default_notification_icon)**: default notification icon for remote push notifications.
-  **meta-data(com.google.firebase.messaging.default_notification_color)**: default notification background color.
- **service(com.cryptoticket.reactnativepushnotification.CustomFirebaseMessagingService)**: custom firebase service that receives and shows push notifications with custom layout. This service works "out of the box" so you don't need to implement any *onNotification()* listeners in your React Native code.
- **receiver(com.cryptoticket.reactnativepushnotification.PushNotificationBroadcastReceiver)**: broadcast receiver that handles notification interactions(ex: on custom button click in notification). At the moment can receive only CLOSE_NOTIFICATION action with id in *Intent extras* that closes push notification.

4. Create a notification channel for local and remote notifications(the one from *meta-data(com.cryptoticket.reactnativepushnotification.default_channel_id*) on app init (required for android >= 8, SDK >= 26):
```
import { PushNotificationAndroid } from '@cryptoticket/react-native-push-notification';
const channelId = "my_channel_id";
const channelName = "my_channel_name";
const channelDesc = "my_channel_desc";
const channelImportance = PushNotificationAndroid.CHANNEL_IMPORTANCE_DEFAULT;
PushNotificationAndroid.createChannel(channelId, channelName, channelDesc, channelImportance);
```

## API

### getDeviceToken()

Returns device FCM token.

Example:
```
import { PushNotificationAndroid } from '@cryptoticket/react-native-push-notification';
const token = await PushNotificationAndroid.getDeviceToken();
console.log(token); // 7rilPUr_OJBvggou...
```

### createChannel(channelId, channelName, channelDesc, channelImportance)

Creates a notification channel. For android >= 8 (SDK >= 26) channels are required when you show a push notification. NOTICE: you should call this method on app init and pass *channel id* from your manifest file.

- **channelId**: channel id. Used on notification show.
- **channelName**: human readable channel name. Channel name is displayed in app notification settings.
- **channelDesc**: channel description.
- **channelImportance**: channel importance, the more importance the more chances that user will see a notification. Available values:
	- PushNotificationAndroid.IMPORTANCE_NONE
	- PushNotificationAndroid.IMPORTANCE_MIN
	- PushNotificationAndroid.IMPORTANCE_LOW
	- PushNotificationAndroid.IMPORTANCE_DEFAULT
	- PushNotificationAndroid.IMPORTANCE_HIGH
	- PushNotificationAndroid.IMPORTANCE_MAX

Example:
```
import { PushNotificationAndroid } from '@cryptoticket/react-native-push-notification';
const channelId = "my_channel_id";
const channelName = "my_channel_name";
const channelDesc = "my_channel_desc";
const channelImportance = PushNotificationAndroid.CHANNEL_IMPORTANCE_DEFAULT;
PushNotificationAndroid.createChannel(channelId, channelName, channelDesc, channelImportance);
```

### show(notificationId, template, channelId, data, priority = PushNotificationAndroid.PRIORITY_DEFAULT)

Shows a push notification. You can use this method locally. This method is also called when remote notification is received.

- **notificationId**: notification id. We need this id in case we would want to modify a notification.
- **template**: template id. There are 2 templates available:
	- PushNotificationAndroid.TEMPLATE_COMMON: standard push notification with title and message, 
	- PushNotificationAndroid.TEMPLATE_EVENT: push notification with custom template. Consists of: button with checkmark, small media image, url that should be opened on notification content click, title and text. When your app receive a remote notification with `media` or `url` data attributes then PushNotificationAndroid.TEMPLATE_EVENT will be used.
- **channelId**: channel id. By default you should use the one from android manifest (as remote notifications use the same one).
- **data**: notification data attributes.
- **priority(optional)**: notification priority. Android >= 8 (SDK >= 26) uses notification channels to set priority. Android < 8 (SDK < 26) sets priority directly on a notification. So this priority field is for compatibility with Android < 8 (SDK < 26). Available notification priorities:
	- PushNotificationAndroid.PRIORITY_MIN
	- PushNotificationAndroid.PRIORITY_LOW
	- PushNotificationAndroid.PRIORITY_DEFAULT
	- PushNotificationAndroid.PRIORITY_HIGH
	- PushNotificationAndroid.PRIORITY_MAX

Example (default notification with title and message):
```
import { PushNotificationAndroid } from '@cryptoticket/react-native-push-notification';
const notificationId = 1;
const template = PushNotificationAndroid.TEMPLATE_COMMON;
 const channelId = 'my_channel_id';
 const data = {
 	title: "my title",
 	message: "my message"
 };
const priority = PushNotificationAndroid.PRIORITY_DEFAULT;
PushNotificationAndroid.show(notificationId, template, channelId, data, priority);
```

Example (custom notification template with checkmark button, media image, url that will be opened on notification content click, title and message):
```
import { PushNotificationAndroid } from '@cryptoticket/react-native-push-notification';
const template = PushNotificationAndroid.TEMPLATE_EVENT;
const channelId = 'my_channel_id';
const data = {
	title: "title", // optional (can be null)
	message: "my message", // optional (can be null)
	media: "http://red-msk.ru/wp-content/uploads/2019/02/canva-photo-editor-22.png", // optional (can be null)
	url: "https://google.com" // optional (can be null)
};
const priority = PushNotificationAndroid.PRIORITY_DEFAULT;
PushNotificationAndroid.show(notificationId, template, channelId, data, priority);
```

### How to run example folder
1. Inside the `example` folder run:
```
npm install
```
2. Following [this tutorial](https://firebase.google.com/docs/android/setup) generate `google-services.json` and add it to `example/android/app` folder.

### Troubleshooting
- **java.lang.IllegalStateException: GeneratedAppGlideModuleImpl is implemented incorrectly. If you've manually implemented this class, remove your implementation. The Annotation processor will generate a correct implementation.**

You may get this error on app build if one of your npm dependencies uses glide. Solution is [here](https://github.com/DylanVann/react-native-fast-image/blob/master/docs/app-glide-module.md). You should add the following code to your `android/build.gradle`:
```
project.ext {
    excludeAppGlideModule = true
}
```
- **twilio remote notifications**

By default twilio wraps notification body(message) in `twi_body` param. This package expects param `message` to be a notification body, not `twi_body`. Solution: copy `twi_body` notification attribute to `message` attribute on remote notification receive.

Create a new class and extend existing `CustomFirebaseMessagingService`:
```
package com.example;

import com.cryptoticket.reactnativepushnotification.CustomFirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;

/**
 * Extends react-native-push-notification message service.
 */
public class MainMessagingService extends CustomFirebaseMessagingService {
    
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        /**
         * By default twilio sends notification body message in "twi_body" param.
         * Our lib needs notification message param to be called "message",
         * so we copy "twi_body" to "message" in remote message data map.
         */
        if(remoteMessage.getData().containsKey("twi_body")) {
            remoteMessage.getData().put("message", remoteMessage.getData().get("twi_body"));
        }
        // call parent method
        super.onMessageReceived(remoteMessage);
    }
}

```
Delete the default firebase service from the manifest:
```
<service
   android:name="com.cryptoticket.reactnativepushnotification.CustomFirebaseMessagingService"
   android:exported="false">
     <intent-filter>
     	<action android:name="com.google.firebase.MESSAGING_EVENT" />
     </intent-filter>
 </service>
```
Add your custom service (which modifies twilio notification params) to the manifest:
```
<service
  android:name=".MainMessagingService"
  android:exported="false">
  	<intent-filter>
  		<action android:name="com.google.firebase.MESSAGING_EVENT" />
  	</intent-filter>
</service>
```


### TODO
- docs: how to add a custom template
- tests + docs "how to run tests"