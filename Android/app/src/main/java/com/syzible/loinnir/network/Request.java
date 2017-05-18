package com.syzible.loinnir.network;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ed on 16/12/2016
 */

abstract class Request <T> extends AsyncTask<Object, Void, T> {
    private NetworkCallback<T> networkCallback;
    private String url, verb;
    private HttpURLConnection connection;

    Request(NetworkCallback<T> networkCallback, String url, String verb) {
        this.networkCallback = networkCallback;
        this.url = url;
        this.verb = verb;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Void... progress) {
        super.onProgressUpdate(progress);
    }

    @Override
    protected T doInBackground(Object... objects) {
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod(verb);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-length", "0");
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.connect();

            return transferData();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(T o) {
        super.onPostExecute(o);
        assert networkCallback != null;
        if (o != null) networkCallback.onSuccess(o);
        else networkCallback.onFailure();
    }

    HttpURLConnection getConnection() {
        return connection;
    }

    public abstract T transferData();
}
