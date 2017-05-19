package com.syzible.loinnir.fragments.portal;

import android.Manifest;
import android.app.Fragment;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.syzible.loinnir.R;
import com.syzible.loinnir.utils.DisplayUtils;
import com.syzible.loinnir.utils.EmojiUtils;

/**
 * Created by ed on 07/05/2017.
 */

public class MapFrag extends Fragment implements OnMapReadyCallback, LocationListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;

    // TODO remove these, just in use until actual current location is polled
    private static final LatLng MAP_CENTRE = new LatLng(53.309543, -6.218028);
    private static final float MY_LOCATION_ZOOM = 14.0f;
    private static final int USER_LOCATION_RADIUS = 250;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1000);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (googleMap != null) renderMap();
        googleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    private void renderMap() {
        googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MAP_CENTRE, MY_LOCATION_ZOOM));

        int GREEN_500 = ContextCompat.getColor(getActivity(), R.color.green500);
        int r = (GREEN_500) & 0xFF;
        int g = (GREEN_500 >> 8) & 0xFF;
        int b = (GREEN_500 >> 16) & 0xFF;
        int a = 128;

        googleMap.addCircle(new CircleOptions()
                .center(MAP_CENTRE)
                .radius(250)
                .strokeColor(GREEN_500)
                .fillColor(Color.argb(a, r, g, b)));

        DisplayUtils.generateToast(getActivity(), "Ag fanacht ar d'áit " + EmojiUtils.getEmoji(EmojiUtils.COOL));
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        System.out.println("Location services connected");
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        googleMap.setMyLocationEnabled(true);
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        } else {
            handleNewLocation(location);
        }
    }

    private void handleNewLocation(Location location) {
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("I am here!");
        googleMap.addMarker(options);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("Location services suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("Location services failed");

        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(connectionResult.getErrorMessage());
            System.out.println("Location services failed (" + connectionResult.getErrorCode() + ")");
        }
    }
}
