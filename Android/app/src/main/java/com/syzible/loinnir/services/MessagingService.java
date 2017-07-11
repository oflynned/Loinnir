package com.syzible.loinnir.services;

import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
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
        // on message received in the foreground
        User from = new User(new JSONObject(remoteMessage.getData().get("from_details")));
        Message message = new Message(from, new JSONObject(remoteMessage.getData().get("message")));

        // for updating UI or creating notifications on receiving a message
        String newMessageIntent = BroadcastFilters.new_partner_message.toString();
        Intent newDataIntent = new Intent(newMessageIntent);
        newDataIntent.putExtra("partner_id", from.getId());
        getApplicationContext().sendBroadcast(newDataIntent);

        NotificationUtils.generateMessageNotification(getApplicationContext(), from, message);
    }
}
