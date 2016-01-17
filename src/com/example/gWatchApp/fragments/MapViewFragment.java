package com.example.gWatchApp.fragments;


import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.gWatchApp.GpsSample;
import com.example.gWatchApp.R;
import com.example.gWatchApp.bledriver.BleDriver;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.LinkedList;

/**
 * Created by Konrad on 2016-01-11.
 */


public class MapViewFragment extends Fragment
{
    SupportMapFragment mapFragment;
    GoogleMap           map;
    BleDriver       bleDriver;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.map, container, false);

        mapFragment = ((SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map));
        map = mapFragment.getMap();
        bleDriver.setMap(map);

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        map.addMarker(new MarkerOptions().position(new LatLng(52.201487, 20.998095)));
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

    private void drawTrack(final LinkedList<GpsSample> sampleList, final GoogleMap map)
    {
        if(sampleList.size() < 2)
            return;
        GpsSample   sample;
        PolylineOptions options = new PolylineOptions();

        options.color( Color.parseColor("#CC0000FF") );
        options.width( 5 );
        options.visible( true );
        for ( int i=1; i<sampleList.size();i++ )
        {
            sample = sampleList.get(i);
            map.addMarker(new MarkerOptions().position(new LatLng(sample.getLatitude(), sample.getLongtitude())));
            options.add( new LatLng( sample.getLatitude(), sample.getLongtitude()));
        }

        map.addPolyline( options );


    }
    public void drawOnMap(final LinkedList<GpsSample> sampleList, final GoogleMap map)
    {
        bleDriver.getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                map.clear();
                drawTrack(sampleList, map);
            }
        });

    }

    public void setBleDriver(BleDriver bleDriver)
    {
        this.bleDriver = bleDriver;
    }

    public GoogleMap getMap()
    {
        return map;
    }

    //    @Override
//    public void onMapReady(GoogleMap googleMap)
//    {
//        this.map = googleMap;
//        this.map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//        googleMap.addMarker(new MarkerOptions().position(new LatLng(52.201404, 20.998161)).title("DS �aczek"));
//    }

}
