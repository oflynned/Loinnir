package com.syzible.loinnir.location;

import android.content.Context;
import android.location.Location;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

/**
 * Created by ed on 12/05/2017.
 */

public class LocationClient {

    public static void stopLocationService(Context context) {
        SmartLocation.with(context).location().stop();
    }
}
