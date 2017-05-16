package com.syzible.loinnir.fragments.portal;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.syzible.loinnir.R;

/**
 * Created by ed on 07/05/2017.
 */

public class MapFrag extends Fragment implements OnMapReadyCallback, LocationListener {
    private GoogleMap googleMap;

    // TODO remove these, just in use until actual current location is polled
    private static final LatLng MAP_CENTRE = new LatLng(53.309543, -6.218028);
    private static final float MY_LOCATION_ZOOM = 14.0f;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MAP_CENTRE, MY_LOCATION_ZOOM));
    }

    @Override
    public void onLocationChanged(Location location) {

    }
}
