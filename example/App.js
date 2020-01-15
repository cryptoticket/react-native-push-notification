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
      </View>
    );
  }
}
