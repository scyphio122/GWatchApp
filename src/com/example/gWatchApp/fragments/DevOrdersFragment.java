package com.example.gWatchApp.fragments;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.gWatchApp.R;
import com.example.gWatchApp.bledriver.BleDeviceList;
import com.example.gWatchApp.bledriver.BleDriver;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Konrad on 2016-01-10.
 */
public class DevOrdersFragment extends Fragment implements View.OnClickListener
{
    private String          title;
    private int             page;

    private BleDriver bleDriver;

    Button  getSatsUsedButton;
    Button  getGpsFixButton;
    Button  getGpsPosButton;
    Button  startSamplingButton;
    Button  stopSamplingButton;
    Button  getTrackListButton;
    Button  getTrackButton;
    Button  getBatVoltButton;
    Button  clearMemoryButton;
    Button  getDeviceTimeButton;
    Button  setDeviceTimeButton;
    Switch  gpsOnOffSwitch;
    NumberPicker trackNumberPicker;

    public static DevOrdersFragment newInstance(int page, String title)
    {
        DevOrdersFragment devOrderFragment = new DevOrdersFragment();
        Bundle args = new Bundle();
        args.putInt("devOrdersInt", page);
        args.putString("devOrdersTitle", title);

        devOrderFragment.setArguments(args);

        return devOrderFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("devOrdersInt", 1);
        title = getArguments().getString("devOrdersString");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup containter, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.connectedscreen, containter, false);

        getSatsUsedButton = (Button)view.findViewById(R.id.satsUsedButton);
        getSatsUsedButton.setOnClickListener(this);

        getGpsFixButton = (Button)view.findViewById(R.id.gpsFixButton);
        getGpsFixButton.setOnClickListener(this);

        getGpsPosButton = (Button)view.findViewById(R.id.gpsPosButton);
        getGpsPosButton.setOnClickListener(this);

        startSamplingButton = (Button)view.findViewById(R.id.startGpsSamplingButton);
        startSamplingButton.setOnClickListener(this);

        stopSamplingButton = (Button)view.findViewById(R.id.stopSamplingButton);
        stopSamplingButton.setOnClickListener(this);

        getTrackListButton = (Button)view.findViewById(R.id.trackListButton);
        getTrackListButton.setOnClickListener(this);

        getTrackButton = (Button)view.findViewById(R.id.getTrackButton);
        getTrackButton.setOnClickListener(this);

        getBatVoltButton = (Button)view.findViewById(R.id.batVoltButton);
        getBatVoltButton.setOnClickListener(this);

        clearMemoryButton = (Button)view.findViewById(R.id.clearMemButton);
        clearMemoryButton.setOnClickListener(this);

        getDeviceTimeButton = (Button)view.findViewById(R.id.getDevTimeButton);
        getDeviceTimeButton.setOnClickListener(this);

        setDeviceTimeButton = (Button)view.findViewById(R.id.setDevTimeButton);
        setDeviceTimeButton.setOnClickListener(this);

        trackNumberPicker = (NumberPicker)view.findViewById(R.id.trackNumberPicker);

        gpsOnOffSwitch = (Switch)view.findViewById(R.id.gpsOnOffSwitch);
        gpsOnOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                byte data[] = new byte[1];
                gpsOnOffSwitch.setSelected(b);
                if(b)
                {
                    data[0] = 13;

                }
                else
                {
                    data[0] = 14;
                }
                /// Turn on the
                bleDriver.sendData(data);
            }
        });


        return view;
    }

    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.satsUsedButton:
            {
                byte []data = new byte[1];
                data[0] = 0x0C;
                bleDriver.sendData(data);
                break;
            }
            case R.id.gpsFixButton:
            {
                byte []data = new byte[1];
                data[0] = 0x04;
                bleDriver.sendData(data);
                break;
            }
            case R.id.gpsPosButton:
            {
                byte []data = new byte[1];
                data[0] = 0x02;
                bleDriver.sendData(data);
                break;
            }
            case R.id.startGpsSamplingButton:
            {
                byte []data = new byte[1];
                data[0] = 0x09;
                bleDriver.sendData(data);
                break;
            }
            case R.id.stopSamplingButton:
            {
                byte []data = new byte[1];
                data[0] = 0x0A;
                bleDriver.sendData(data);
                break;
            }
            case R.id.trackListButton:
            {
                byte []data = new byte[1];
                data[0] = 0x06;
                bleDriver.sendData(data);
                break;
            }
            case R.id.getTrackButton:
            {
                int trackNumber = trackNumberPicker.getValue();
                byte []data = new byte[2];
                data[0] = 0x05;
                data[1] = (byte)trackNumber;

                bleDriver.sendData(data);
                break;
            }
            case R.id.batVoltButton:
            {
                byte []data = new byte[1];
                data[0] = 0x07;
                bleDriver.sendData(data);
                break;
            }
            case R.id.clearMemButton:
            {
                byte []data = new byte[1];
                data[0] = 0x0B;
                bleDriver.sendData(data);
                break;
            }
            case R.id.getDevTimeButton:
            {
                byte []data = new byte[1];
                data[0] = 0x01;
                bleDriver.sendData(data);
                break;
            }
            case R.id.setDevTimeButton:
            {
                TimeZone timeZone = TimeZone.getDefault();
                long timeUTC = System.currentTimeMillis();

                int offset = timeZone.getRawOffset()/1000;
                int time = (int)(timeUTC/1000) + offset;


                byte []data = new byte[5];
                data[0] = 0x00;
                data[1] = (byte)(time & 0xFF);
                data[2] = (byte)((time >> 8 ) & 0xFF);
                data[3] = (byte)((time >> 16) & 0xFF);
                data[4] = (byte)((time >> 24) & 0xFF);

                bleDriver.sendData(data);
                break;
            }
        }
    }

    public void setBleDriver(BleDriver bleDriver)
    {
        this.bleDriver = bleDriver;
    }

}
