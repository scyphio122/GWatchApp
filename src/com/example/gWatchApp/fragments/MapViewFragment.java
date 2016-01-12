package com.example.gWatchApp.fragments;


import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.gWatchApp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Konrad on 2016-01-11.
 */


public class MapViewFragment extends Fragment
{
    SupportMapFragment mapFragment;
    GoogleMap   map;



    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.map, container, false);

        mapFragment = ((SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map));
        map = mapFragment.getMap();

        return v;
    }


    public static MapViewFragment newInstance(int page, String title)
    {
        MapViewFragment mapViewFragment = new MapViewFragment();
        Bundle args = new Bundle();
        args.putInt("mapViewInt", page);
        args.putString("mapViewTitle", title);

        mapViewFragment.setArguments(args);

        return mapViewFragment;
    }

//    @Override
//    public void onMapReady(GoogleMap googleMap)
//    {
//        this.map = googleMap;
//        this.map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//        googleMap.addMarker(new MarkerOptions().position(new LatLng(52.201404, 20.998161)).title("DS �aczek"));
//    }

}
