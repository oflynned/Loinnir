package com.syzible.loinnir.objects;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ed on 08/05/2017.
 */

public class Conversation implements IDialog {

    private ArrayList<User> users = new ArrayList<>();
    private String id, profilePic, name;
    private IMessage lastMessage;
    private int unreadCount;

    public Conversation(User partner, Message lastMessage) {
        users.add(partner);
        this.id = partner.getId();
        this.profilePic = partner.getAvatar();
        this.name = partner.getName();
        this.lastMessage = lastMessage;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDialogPhoto() {
        return profilePic;
    }

    @Override
    public String getDialogName() {
        return name;
    }

    @Override
    public List<? extends IUser> getUsers() {
        return users;
    }

    @Override
    public IMessage getLastMessage() {
        return lastMessage;
    }

    @Override
    public void setLastMessage(IMessage message) {
        this.lastMessage = message;
    }

    @Override
    public int getUnreadCount() {
        // TODO implement on server
        return 0;
    }
}
