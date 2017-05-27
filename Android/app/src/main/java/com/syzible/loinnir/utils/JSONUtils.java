package com.syzible.loinnir.utils;

import android.content.Context;

import com.syzible.loinnir.objects.User;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ed on 27/05/2017.
 */

public class JSONUtils {

    public static JSONObject getIdPayload(Context context) {
        JSONObject o = new JSONObject();
        try {
            o.put("fb_id", LocalStorage.getID(context));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return o;
    }

    public static JSONObject getPartnerInteractionPayload(User partner, Context context) {
        JSONObject o = new JSONObject();
        try {
            o.put("my_id", LocalStorage.getID(context));
            o.put("partner_id", partner.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return o;
    }
}
