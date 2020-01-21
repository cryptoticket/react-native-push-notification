/**
 * Sending plain data notifications via twilio
 */

const config = require('./config');

const accountSid = config.ACCOUNT_SID;
const authToken = config.AUTH_TOKEN;
const serviceId = config.SERVICE_ID;
const userIdentity = 'user-1';
const fcmAddress = config.FCM_ADDRESS;

const client = require('twilio')(accountSid, authToken);

// bind user (only the 1st time) 
/*
try {
    client.notify.services(serviceId)
             .bindings
             .create({
                identity: userIdentity,
                bindingType: 'fcm',
                address: fcmAddress
              })
             .then(binding => console.log(binding.sid));    
} catch(err) {
    console.log("twilio binding error");
    console.log(err);
}
*/

// send push notification

const dataCommon = {
	title: 'my remote title',
	message: 'my remote message'
};

const dataEvent = {
	title: 'event title',
	message: 'event message',
    media: 'http://red-msk.ru/wp-content/uploads/2019/02/canva-photo-editor-22.png'
};

const dataEventWithUrl = {
    title: 'event title',
	message: 'event message',
    media: 'http://red-msk.ru/wp-content/uploads/2019/02/canva-photo-editor-22.png',
    url: 'https://google.com'
};

try {
    client.notify.services(serviceId)
             .notifications
             .create({data: dataCommon, identity: userIdentity})
             .then(notification => console.log(notification.sid));
} catch(err) {
    console.log("error on sending notification");
    console.log(err);
}


console.log("finished");