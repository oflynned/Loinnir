package com.syzible.loinnir.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.syzible.loinnir.location.LocationClient;
import com.syzible.loinnir.network.Endpoints;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.utils.LocalStorage;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by ed on 19/05/2017.
 */

public class UpdateLocationService extends IntentService {
    public UpdateLocationService() {
        super(UpdateLocationService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // poll location of the user and send it to the server
        if (!LocalStorage.getID(getApplicationContext()).equals("")) {
            JSONObject payload = new JSONObject();

            try {
                payload.put("fb_id", LocalStorage.getID(getApplicationContext()));

                // TODO poll for real location
                payload.put("lng", LocationClient.GOOSEBERRY_HILL.longitude);
                payload.put("lat", LocationClient.GOOSEBERRY_HILL.latitude);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // don't care about response on user side
            RestClient.post(getApplicationContext(), Endpoints.UPDATE_USER_LOCATION, payload, new BaseJsonHttpResponseHandler<JSONObject>() {
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
}
