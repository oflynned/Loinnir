package com.syzible.loinnir.objects;

import com.google.android.gms.maps.model.LatLng;
import com.stfalcon.chatkit.commons.models.IUser;
import com.syzible.loinnir.utils.Constants;
import com.syzible.loinnir.utils.EncodingUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.CharsetDecoder;

/**
 * Created by ed on 07/05/2017.
 */

public class User implements IUser {

    private String fb_id;
    private float longitude, latitude;
    private LatLng location;
    private String name;
    private String avatar;
    private String locality;

    public User(JSONObject data) throws JSONException {
        this.fb_id = data.getString("fb_id");
        this.longitude = (float) data.getDouble("lng");
        this.latitude = (float) data.getDouble("lat");
        this.location = new LatLng(latitude, longitude);
        this.name = EncodingUtils.decodeText(data.getString("name"));
        this.avatar = data.getString("profile_pic");
        this.locality = data.getString("locality");
    }

    @Override
    public String getId() {
        return fb_id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public LatLng getLocation() {
        return location;
    }

    public String getLocality() {
        return locality;
    }
}
