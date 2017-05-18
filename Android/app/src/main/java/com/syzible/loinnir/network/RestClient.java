package com.syzible.loinnir.network;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by ed on 18/05/2017.
 */

public class RestClient {
    private static AsyncHttpClient client = new AsyncHttpClient();

    private RestClient() {
    }

    public static void get(String url, AsyncHttpResponseHandler responseHandler) {
        client.get(Endpoints.getAbsoluteURL(url), null, responseHandler);
    }

    public static void getExternal(String url, AsyncHttpResponseHandler responseHandler) {
        client.setEnableRedirects(true);
        client.get(url, null, responseHandler);
    }

    public static void post(Context context, String url, JSONObject data, AsyncHttpResponseHandler responseHandler) {
        try {
            client.post(context, Endpoints.getAbsoluteURL(url), new StringEntity(data.toString()), "application/json", responseHandler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void delete(Context context, String url, JSONObject data, AsyncHttpResponseHandler responseHandler) {
        try {
            client.delete(context, Endpoints.getAbsoluteURL(url), new StringEntity(data.toString()), "application/json", responseHandler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
