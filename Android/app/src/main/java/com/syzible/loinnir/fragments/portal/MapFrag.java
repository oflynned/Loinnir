package com.syzible.loinnir.fragments.portal;

import android.Manifest;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.syzible.loinnir.R;
import com.syzible.loinnir.location.LocationClient;
import com.syzible.loinnir.network.Endpoints;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.objects.User;
import com.syzible.loinnir.utils.Constants;
import com.syzible.loinnir.utils.JSONUtils;
import com.syzible.loinnir.utils.LocalStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

/**
 * Created by ed on 07/05/2017.
 */

public class MapFrag extends Fragment implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private int GREEN_500;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBroadcastReceiver();

        GREEN_500 = ContextCompat.getColor(getActivity(), R.color.green500);
        getActivity().setTitle(getResources().getString(R.string.app_name));
    }

    @Override
    public void onResume() {
        super.onResume();

        setMapPosition();
    }

    private void setMapPosition() {
        if (googleMap != null) {
            if (LocalStorage.getBooleanPref(LocalStorage.Pref.should_share_location, getActivity())) {
                getWebServerLocation();
            } else {
                googleMap.clear();
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LocationClient.ATHLONE, LocationClient.INITIAL_LOCATION_ZOOM));
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_frag, container, false);

        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (Constants.DEV_MODE)
            this.googleMap.getUiSettings().setZoomControlsEnabled(true);

        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LocationClient.ATHLONE, LocationClient.INITIAL_LOCATION_ZOOM));
    }

    private void getWebServerLocation() {
        googleMap.clear();

        RestClient.post(getActivity(), Endpoints.GET_OTHER_USERS, JSONUtils.getIdPayload(getActivity()),
                new BaseJsonHttpResponseHandler<JSONArray>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                User user = new User(response.getJSONObject(i));
                                addUserCircle(new LatLng(user.getLatitude(), user.getLongitude()), false);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {

                    }

                    @Override
                    protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                        return new JSONArray(rawJsonData);
                    }
                });

        // get my last known location and move to it on the map
        RestClient.post(getActivity(), Endpoints.GET_USER, JSONUtils.getIdPayload(getActivity()),
                new BaseJsonHttpResponseHandler<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONObject response) {
                        try {
                            User me = new User(response);
                            LatLng location = new LatLng(me.getLatitude(), me.getLongitude());
                            addUserCircle(location, true);
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, LocationClient.MY_LOCATION_ZOOM));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

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

    private int getFillColour() {
        int r = (GREEN_500) & 0xFF;
        int g = (GREEN_500 >> 8) & 0xFF;
        int b = (GREEN_500 >> 16) & 0xFF;
        int a = 128;

        return Color.argb(a, r, g, b);
    }

    private void addUserCircle(final LatLng latLng, boolean isMe) {
        googleMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(LocationClient.USER_LOCATION_RADIUS)
                .strokeColor(GREEN_500)
                .fillColor(getFillColour()));

        if (isMe) {
            // delay zooming in from overall map to the user's position so it looks better
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            latLng, LocationClient.MY_LOCATION_ZOOM));
                }
            }, 1000);
        }
    }

    private void registerBroadcastReceiver() {
        getActivity().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("updated_location")) {
                    getWebServerLocation();
                }
            }
        }, new IntentFilter("updated_location"));
    }
}
