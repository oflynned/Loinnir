package com.syzible.loinnir.objects;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by ed on 08/05/2017.
 */

public class Conversation {
    private ArrayList<User> participants = new ArrayList<>();

    public Conversation(JSONArray users) throws JSONException {
        for(int i=0; i<users.length(); i++) {
            participants.add(new User(users.getJSONObject(i)));
        }
    }

    public ArrayList<User> getParticipants() {
        return participants;
    }
}
