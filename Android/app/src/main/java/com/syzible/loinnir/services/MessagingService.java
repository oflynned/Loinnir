package com.syzible.loinnir.services;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by ed on 26/05/2017.
 */

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        System.out.println("Message received!");
        System.out.println(remoteMessage.getFrom() + ": " + remoteMessage.getData());

        // if contains a data payload
        if (remoteMessage.getData().size() > 0) {

        }

        if (remoteMessage.getNotification() != null) {
            NotificationUtils.generateNotification(getApplicationContext(), remoteMessage);
        }

        super.onMessageReceived(remoteMessage);
    }
}
