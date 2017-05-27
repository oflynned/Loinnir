package com.syzible.loinnir.objects;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by ed on 07/05/2017.
 */

public class Message implements IMessage {
    private String id;
    private User sender;
    private long time;
    private String contents;

    public Message(String id, User sender, long time, String contents) {
        this.id = id;
        this.sender = sender;
        this.time = time;
        this.contents = contents;
    }

    public Message(User sender, JSONObject messageObject) {
        try {
            this.sender = sender;
            this.id = messageObject.getString("_id");
            this.time = messageObject.getLong("time");
            this.contents = messageObject.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getText() {
        return contents;
    }

    @Override
    public IUser getUser() {
        return sender;
    }

    @Override
    public Date getCreatedAt() {
        return new Date(time);
    }
}
