package com.example.gWatchApp.fragments;


import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.gWatchApp.GpsSample;
import com.example.gWatchApp.R;
import com.example.gWatchApp.bledriver.BleDriver;
import com.google.android.gms.internal.en;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.math.BigDecimal;
import java.text.DecimalFormat;
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

        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(true);
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
                TextView distance = (TextView)getActivity().findViewById(R.id.distanceTextView);
                BigDecimal entireDistance = new BigDecimal(calculateDistance(sampleList));
                entireDistance = entireDistance.setScale(3, BigDecimal.ROUND_HALF_UP);
                distance.setText(" " + entireDistance.toPlainString() + "km");
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

public double calculateDistance(LinkedList<GpsSample> gpsList)
{
    double entireDistance = 0;
    for(int i=0; i<gpsList.size()-1;i++)
    {
        LatLng startPoint = new LatLng(gpsList.get(i).getLatitude(), gpsList.get(i).getLongtitude());
        LatLng endPoint = new LatLng(gpsList.get(i+1).getLatitude(), gpsList.get(i+1).getLongtitude());
        entireDistance += CalculationByDistance(startPoint, endPoint);
    }

    return entireDistance;
}

    private double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }
}
