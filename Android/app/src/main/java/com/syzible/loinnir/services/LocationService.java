package com.syzible.loinnir.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.syzible.loinnir.activities.MainActivity;
import com.syzible.loinnir.network.Endpoints;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.utils.LocalStorage;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by ed on 30/05/2017.
 */

public class LocationService extends Service {
    private LocationManager locationManager = null;
    private static final int LOCATION_INTERVAL = 1000 * 30;
    private static final float LOCATION_DISTANCE = 1f;

    private class LocationListener implements android.location.LocationListener {

        private Location lastLocation;

        LocationListener(String provider) {
            this.lastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            System.out.println("New location!");
            lastLocation.set(location);
            syncWithServer(location);
        }

        private void syncWithServer(Location location) {
            if (!LocalStorage.getID(getApplicationContext()).equals("")) {
                JSONObject payload = new JSONObject();

                try {
                    payload.put("fb_id", LocalStorage.getID(getApplicationContext()));
                    payload.put("lng", location.getLongitude());
                    payload.put("lat", location.getLatitude());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // don't care about response on user side
                RestClient.post(getApplicationContext(), Endpoints.UPDATE_USER_LOCATION, payload, new BaseJsonHttpResponseHandler<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONObject response) {
                        getApplicationContext().sendBroadcast(new Intent("updated_location"));
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

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            System.out.println("onStatusChanged: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            System.out.println("onProviderEnabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            System.out.println("onProviderDisabled: " + provider);
        }
    }

    LocationListener[] locationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private void registerBroadcastReceiver(MainActivity.BroadcastFilters filter) {
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(MainActivity.BroadcastFilters.start_location_polling.name())) {
                    System.out.println("Polling start location");
                    startPollingLocation();
                } else if (intent.getAction().equals(MainActivity.BroadcastFilters.end_location_polling.name())) {
                    stopPollingLocation();
                }
            }
        }, new IntentFilter(filter.name()));
    }

    @Override
    public void onCreate() {
        registerBroadcastReceiver(MainActivity.BroadcastFilters.start_location_polling);
        registerBroadcastReceiver(MainActivity.BroadcastFilters.end_location_polling);

        startPollingLocation();
        super.onCreate();
    }

    private void startPollingLocation() {
        initializeLocationManager();
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    locationListeners[1]);
        } catch (java.lang.SecurityException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    locationListeners[0]);
        } catch (java.lang.SecurityException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void stopPollingLocation() {
        if (locationManager != null) {
            for (LocationListener locationListener : locationListeners) {
                try {
                    locationManager.removeUpdates(locationListener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPollingLocation();
    }

    private void initializeLocationManager() {
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}