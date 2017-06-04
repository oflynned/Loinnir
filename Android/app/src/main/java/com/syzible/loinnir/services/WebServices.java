package com.syzible.loinnir.services;

import android.content.Context;

import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.syzible.loinnir.network.Endpoints;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.utils.LocalStorage;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by ed on 04/06/2017.
 */

public class WebServices {
    public static void updateBroadcastLocation(Context context, boolean isBroadcasting) {
        JSONObject object = new JSONObject();
        try {
            object.put("fb_id", LocalStorage.getID(context));
            object.put("is_broadcasting", isBroadcasting);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RestClient.post(context, Endpoints.EDIT_USER, object, new BaseJsonHttpResponseHandler<JSONObject>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONObject response) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONObject errorResponse) {

            }

            @Override
            protected JSONObject parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return new JSONObject(rawJsonData);
            }
        });
    }
}
