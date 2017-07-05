package com.syzible.loinnir.services;

import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.syzible.loinnir.objects.Message;
import com.syzible.loinnir.objects.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ed on 26/05/2017.
 */

public class MessagingService extends FirebaseMessagingService {

    private enum NotificationTypes {
        new_partner_message, new_locality_information, app_information
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        System.out.println("Received FCM update");

        if (remoteMessage.getData().size() > 0) {
            System.out.println(remoteMessage.getData());
            try {
                String message_type = remoteMessage.getData().get("notification_type");

                if (message_type.equals(NotificationTypes.new_locality_information.name())) {
                    // new locality update in chat, emit a broadcast to force an update if the locality fragment is active
                    Intent intent = new Intent(NotificationTypes.new_locality_information.name());
                    getApplicationContext().sendBroadcast(intent);
                } else if (message_type.equals(NotificationTypes.new_partner_message.name())) {
                    // on message received in the foreground
                    User from = new User(new JSONObject(remoteMessage.getData().get("from_details")));
                    Message message = new Message(from, new JSONArray(remoteMessage.getData().get("message")));

                    // for updating UI or creating notifications on receiving a message
                    // TODO update partner conversation fragment to properly use new filter
                    Intent newDataIntent = new Intent("com.syzible.loinnir.new_message");
                    newDataIntent.putExtra("partner_id", from.getId());
                    getApplicationContext().sendBroadcast(newDataIntent);

                    NotificationUtils.generateMessageNotification(getApplicationContext(), from, message);
                } else if (message_type.equals(NotificationTypes.app_information.name())) {
                    // in case a periodic notification is dispatched to all users about Loinnir
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // on chat partner message update -- only in background
        if (remoteMessage.getNotification() != null) {
            NotificationUtils.generateNotification(getApplicationContext(), remoteMessage);
        }

        super.onMessageReceived(remoteMessage);
    }
}
