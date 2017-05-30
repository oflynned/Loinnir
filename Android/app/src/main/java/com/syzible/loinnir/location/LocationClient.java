package com.syzible.loinnir.location;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;
import com.kishan.askpermission.AskPermission;
import com.kishan.askpermission.ErrorCallback;
import com.kishan.askpermission.PermissionCallback;
import com.kishan.askpermission.PermissionInterface;
import com.syzible.loinnir.utils.LocalStorage;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by ed on 12/05/2017.
 */

public class LocationClient implements LocationListener {

    public static final LatLng ATHLONE = new LatLng(53.4232575, -7.9402598);
    public static final LatLng GOOSEBERRY_HILL = new LatLng(52.252133, -8.969127);

    public static final float INITIAL_LOCATION_ZOOM = 6.0f;
    public static final float MY_LOCATION_ZOOM = 14.0f;
    public static final int USER_LOCATION_RADIUS = 500;

    private static final int PERMISSION_REQUEST_LOCATION = 1;

    private LocationClient() {
    }

    private static LocationClient instance = new LocationClient();

    public static LocationClient getInstance() {
        return instance;
    }

    public void requestLocationPermissions(Activity activity) {
        new AskPermission.Builder(activity)
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .setCallback(new PermissionCallback() {
                    @Override
                    public void onPermissionsGranted(int requestCode) {

                    }

                    @Override
                    public void onPermissionsDenied(int requestCode) {

                    }
                })
                .setErrorCallback(new ErrorCallback() {
                    @Override
                    public void onShowRationalDialog(PermissionInterface permissionInterface, int requestCode) {

                    }

                    @Override
                    public void onShowSettings(PermissionInterface permissionInterface, int requestCode) {

                    }
                })
                .request(PERMISSION_REQUEST_LOCATION);
    }

    public void startPollingLocation(Activity activity) {
        LocationManager locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            new AskPermission.Builder(activity)
                    .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                    .setCallback(new PermissionCallback() {
                        @Override
                        public void onPermissionsGranted(int requestCode) {

                        }

                        @Override
                        public void onPermissionsDenied(int requestCode) {

                        }
                    })
                    .setErrorCallback(new ErrorCallback() {
                        @Override
                        public void onShowRationalDialog(PermissionInterface permissionInterface, int requestCode) {

                        }

                        @Override
                        public void onShowSettings(PermissionInterface permissionInterface, int requestCode) {

                        }
                    })
                    .request(PERMISSION_REQUEST_LOCATION);
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, (android.location.LocationListener) this);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);

        if (location != null) {
            onLocationChanged(location);

            LocalStorage.setFloatPref(LocalStorage.Pref.lat, (float) location.getLatitude(), activity);
            LocalStorage.setFloatPref(LocalStorage.Pref.lng, (float) location.getLongitude(), activity);
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        System.out.println(latitude + ", " + longitude);
    }
}
