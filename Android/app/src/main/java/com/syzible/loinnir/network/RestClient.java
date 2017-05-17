package com.syzible.loinnir.network;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by ed on 12/05/2017.
 */

public class RestClient {
    private static final int API_VERSION = 1;
    private static final String LOCAL_ENDPOINT = "http://10.0.2.2:3000";
    private static final String REMOTE_ENDPOINT = "http://www.loinnir.ie";
    private static final String BASE_URL = LOCAL_ENDPOINT + "/api/v" + API_VERSION;

    public static final String CREATE_USER = "/users/create";
    public static final String EDIT_USER = "/users/edit";
    public static final String DELETE_USER = "/users/delete";

    public static final String GET_USER = "/users/get";
    public static final String GET_ALL_USERS = "/users/get-all";
    public static final String GET_NEARBY_USERS = "/users/get-nearby";
    public static final String GET_RANDOM_USER = "/users/get-random";

    public static final String[] MEDIA_TYPES = {
            "image/png",
            "image/jpeg",
            "image/gif"
    };

    private static AsyncHttpClient client = new AsyncHttpClient();

    private RestClient() {
    }

    public static void get(String url, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), null, responseHandler);
    }

    public static void getExternal(String url, AsyncHttpResponseHandler responseHandler) {
        client.setEnableRedirects(true);
        client.get(url, null, responseHandler);
    }

    public static void post(Context context, String url, JSONObject data, AsyncHttpResponseHandler responseHandler) {
        try {
            client.post(context, getAbsoluteUrl(url), new StringEntity(data.toString()), "application/json", responseHandler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void delete(Context context, String url, JSONObject data, AsyncHttpResponseHandler responseHandler) {
        try {
            client.delete(context, getAbsoluteUrl(url), new StringEntity(data.toString()), "application/json", responseHandler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

}
