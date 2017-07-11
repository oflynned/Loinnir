package com.syzible.loinnir.services;

import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.syzible.loinnir.network.Endpoints;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.objects.Message;
import com.syzible.loinnir.objects.User;
import com.syzible.loinnir.utils.BroadcastFilters;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ed on 26/05/2017.
 */

public class MessagingService extends FirebaseMessagingService {

    private enum NotificationTypes {
        new_partner_message, new_locality_update
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        System.out.println("Received FCM update");

        if (remoteMessage.getData().size() > 0) {
            System.out.println("Data in packet: " + remoteMessage.getData());
            String message_type = remoteMessage.getData().get("notification_type");

            if (message_type.equals(NotificationTypes.new_locality_update.name())) {
                onLocalityInfoUpdate();
            } else if (message_type.equals(NotificationTypes.new_partner_message.name())) {
                try {
                    onPartnerMessage(remoteMessage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        // background notifications if the app is dead or not in focus
        if (remoteMessage.getNotification() != null) {
            String partnerId = remoteMessage.getData().get("message_body");
            RestClient.post(getApplicationContext(), Endpoints.GET_PAST_CONVERSATION_PREVIEWS,
                    );

            NotificationUtils.generateNotification(getApplicationContext(),
                    remoteMessage.getData().get("message_title"),
                    remoteMessage.getData());
        }

        super.onMessageReceived(remoteMessage);
    }

    private void onLocalityInfoUpdate() {
        System.out.println("Dispatching onLocalityInfoUpdate()");
        // new locality update in chat, emit a broadcast to force an update if the locality fragment is active
        String newLocalityIntent = BroadcastFilters.new_locality_info_update.toString();
        Intent intent = new Intent(newLocalityIntent);
        getApplicationContext().sendBroadcast(intent);
    }

    private void onPartnerMessage(RemoteMessage remoteMessage) throws JSONException {
        System.out.println("Dispatching onPartnerMessage()");
        String notificationBody = remoteMessage.getData().get("message");
        User sender = new User(new JSONObject(remoteMessage.getData().get("from_details")));

        // on message received in the foreground
        Message message = new Message(sender, new JSONObject(notificationBody));

        // for updating UI or creating notifications on receiving a message
        String newMessageIntent = BroadcastFilters.new_partner_message.toString();
        Intent newDataIntent = new Intent(newMessageIntent);
        newDataIntent.putExtra("partner_id", sender.getId());
        getApplicationContext().sendBroadcast(newDataIntent);

        // NotificationUtils.generateMessageNotification(getApplicationContext(), sender, message);
    }
}
