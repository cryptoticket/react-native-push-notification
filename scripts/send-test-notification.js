/**
 * Sending plain data notifications via twilio
 */

const config = require('./config');

const accountSid = config.ACCOUNT_SID;
const authToken = config.AUTH_TOKEN;
const serviceId = config.SERVICE_ID;
const userIdentity = 'user-1';
const fcmAddress = 'dixjxuPSemY:APA91bE8IvwT9t5pGvEMGNx9DV_ghXe8GLx3rf3wGcQ07rilPUr_OJBvggouYuStCBPuMThiUY-F0_QjIg8ud8J_h3KU-hLBMxqMiC9Jg1ZlEKIRcmVDMqA577M1WhgS_qeM3MmhueiS';

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
try {
    client.notify.services(serviceId)
             .notifications
             .create({data: dataEvent, identity: userIdentity})
             .then(notification => console.log(notification.sid));
} catch(err) {
    console.log("error on sending notification");
    console.log(err);
}


console.log("finished");