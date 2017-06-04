package com.syzible.loinnir.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.syzible.loinnir.network.Endpoints;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.utils.LocalStorage;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by ed on 30/05/2017.
 */

public class LocationService extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private boolean isReceivingUpdates = false;

    private Location lastLocation;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;

    private static final long UPDATE_INTERVAL = 1000 * 60 * 15;
    private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        this.lastLocation = location;
        syncWithServer(location);
    }

    private void startLocationUpdates() {
        if (!isReceivingUpdates) {
            isReceivingUpdates = true;

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    private void stopLocationUpdates() {
        if (isReceivingUpdates) {
            isReceivingUpdates = false;
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
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

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(LocationService.this)
                .addApi(LocationServices.API)
                .build();

        createLocationRequest();
    }

    private void createLocationRequest() {
        googleApiClient.connect();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
