import React from 'react';
import { Alert, Button, Text, View } from 'react-native';

import { PushNotificationAndroid } from './PushNotification';

export default class App extends React.Component {

  /**
   * Creates a new notification channel
   */
  onCreateChannelPress = () => {
    try {
      const channelId = "my_channel_id";
      const channelName = "my_channel_name";
      const channelDesc = "my_channel_desc";
      const channelImportance = PushNotificationAndroid.CHANNEL_IMPORTANCE_DEFAULT;
      PushNotificationAndroid.createChannel(channelId, channelName, channelDesc, channelImportance);
      Alert.alert("Success", `Channel ${channelName} created`);
    } catch (err) {
      Alert.alert("Error", err);
    }
  };

  /**
   * Show device FCM token
   */
  onGetDeviceTokenPress = async () => {
    try {
      const token = await PushNotificationAndroid.getDeviceToken();
      Alert.alert("Token", token);
      console.log(token);
    } catch(err) {
      Alert.alert("Error", err);
    }
  };

  /**
   * Show standard notification
   */
  onShowCommonNotificationPress = () => {
    try {
      const notificationId = 1;
      const template = PushNotificationAndroid.TEMPLATE_COMMON;
      const channelId = 'my_channel_id';
      const data = {
        title: "my title",
        message: "my message"
      };
      const priority = PushNotificationAndroid.PRIORITY_DEFAULT;
      PushNotificationAndroid.show(notificationId, template, channelId, data, priority);
    } catch(err) {
      Alert.alert("Error", err);
    }
  }
  
  /**
   * Renders JSX template
   * @return JSX template
   */
  render() {
    return (
      <View>
        <Text>Examples</Text>
        
        <View style={{marginTop: 10}} >
          <Button title="Create channel" onPress={this.onCreateChannelPress} />
        </View>

        <View style={{marginTop: 10}} >
          <Button title="Get device token" onPress={this.onGetDeviceTokenPress} />
        </View>

        <View style={{marginTop: 10}} >
          <Button title="Show common notification" onPress={this.onShowCommonNotificationPress} />
        </View>
      </View>
    );
  }
}
