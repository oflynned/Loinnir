package com.syzible.loinnir.objects;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ed on 07/05/2017.
 */

public class User {

    private String id, fb_id;
    private float longitude, latitude;
    private String name;
    private String imageUrl;

    public User(JSONObject data) throws JSONException {
        this.id = data.getString("_id");
        this.fb_id = data.getString("fb_id");
        this.longitude = (float) data.getDouble("lng");
        this.latitude = (float) data.getDouble("lat");
        this.name = data.getString("name");
        this.imageUrl = data.getString("profile_pic");
    }

    public String getId() {
        return id;
    }

    public String getFb_id() {
        return fb_id;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
