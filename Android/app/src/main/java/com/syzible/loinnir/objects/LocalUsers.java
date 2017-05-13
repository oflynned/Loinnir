package com.syzible.loinnir.objects;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by ed on 07/05/2017.
 */

public class LocalUsers {
    private ArrayList<User> users = new ArrayList<>();

    public LocalUsers(JSONArray results) throws JSONException {
        for (int i = 0; i < results.length(); i++) {
            users.add(new User(results.getJSONObject(i)));
        }
    }

    public ArrayList<User> getUsers() {
        return users;
    }
}
