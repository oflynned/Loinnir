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
        new_partner_message, app_information
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // handle FCM notification updates from the server
        if (remoteMessage.getData().size() > 0) {
            System.out.println(remoteMessage.getData());
            try {
                String message_type = remoteMessage.getData().get("notification_type");
                String title = remoteMessage.getData().get("message_title");
                String avatar_url = remoteMessage.getData().get("message_avatar");
                User from = new User(new JSONObject(remoteMessage.getData().get("from_details")));
                User to = new User(new JSONObject(remoteMessage.getData().get("to_details")));
                Message message = new Message(from, new JSONArray(remoteMessage.getData().get("message")));

                if (message_type.equals(NotificationTypes.new_partner_message.name())) {
                    Intent newDataIntent = new Intent("new_message");
                    newDataIntent.putExtra("partner_id", from.getId());
                    getApplicationContext().sendBroadcast(newDataIntent);

                    NotificationUtils.generateMessageNotification(getApplicationContext(), from, message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (remoteMessage.getNotification() != null) {
            NotificationUtils.generateNotification(getApplicationContext(), remoteMessage);
        }

        super.onMessageReceived(remoteMessage);
    }
}
