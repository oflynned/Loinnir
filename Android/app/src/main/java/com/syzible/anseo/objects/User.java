package com.syzible.anseo.objects;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ed on 07/05/2017.
 */

public class User {

    private String id;
    private float longitude, latitude;
    private String forename, surname;
    private String imageUrl;

    public User(JSONObject data) throws JSONException {
        this.id = data.getString("_id");
        this.longitude = (float) data.getDouble("lng");
        this.latitude = (float) data.getDouble("lat");
        this.forename = data.getString("forename");
        this.surname = data.getString("surname");
        this.imageUrl = data.getString("image_url");
    }

    public String getId() {
        return id;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public String getForename() {
        return forename;
    }

    public String getSurname() {
        return surname;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
