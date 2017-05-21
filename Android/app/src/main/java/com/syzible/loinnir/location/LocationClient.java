package com.syzible.loinnir.location;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

/**
 * Created by ed on 12/05/2017.
 */

public class LocationClient {

    public static final LatLng ATHLONE = new LatLng(53.4232575, -7.9402598);
    public static final LatLng UCD = new LatLng(53.309543, -6.218028);
    public static final LatLng GOOSEBERRY_HILL = new LatLng(52.252133, -8.969127);

    public static final float INITIAL_LOCATION_ZOOM = 8.0f;
    public static final float MY_LOCATION_ZOOM = 14.0f;
    public static final int USER_LOCATION_RADIUS = 500;

    public static void stopLocationService(Context context) {
        SmartLocation.with(context).location().stop();
    }
}
