package com.example.gWatchApp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.gWatchApp.R;
import com.example.gWatchApp.bledriver.BleDriver;

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
    Button  storageIntervalButton;
    EditText storageIntervalEditText;
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

        storageIntervalButton = (Button)view.findViewById(R.id.storageIntervalButton);
        storageIntervalButton.setOnClickListener(this);

        storageIntervalEditText = (EditText)view.findViewById(R.id.storageIntervalLineEdit);

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

        this.disableButtons();

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
                byte []data = new byte[3];
                data[0] = 0x05;
                data[1] = (byte)trackNumber;
                data[2] = 0;

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
                ClearMemoryDialog dialog = new ClearMemoryDialog();
                dialog.setBleDriver(bleDriver);
                dialog.setActivity(getActivity());

                dialog.showDialog();

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

            case R.id.storageIntervalButton:
            {
                String storageInterval = this.storageIntervalEditText.getText().toString();
                if(!storageInterval.isEmpty() && !storageInterval.equals("0"))
                {
                    int interval = new Integer(storageInterval).intValue();
                    byte data[] = new byte[5];
                    data[0] = 15;
                    data[1] = (byte)(interval & 0xFF);
                    data[2] = (byte)((interval >> 8 ) & 0xFF);
                    data[3] = (byte)((interval >> 16) & 0xFF);
                    data[4] = (byte)((interval >> 24) & 0xFF);

                    bleDriver.sendData(data);
                }
                break;
            }
        }
    }

    public void enableButtons()
    {
        clearMemoryButton.setEnabled(true);
        getBatVoltButton.setEnabled(true);
        getDeviceTimeButton.setEnabled(true);
        getGpsFixButton.setEnabled(true);
        getGpsPosButton.setEnabled(true);
        getSatsUsedButton.setEnabled(true);
        getTrackListButton.setEnabled(true);
        setDeviceTimeButton.setEnabled(true);
        startSamplingButton.setEnabled(true);
        stopSamplingButton.setEnabled(true);
        gpsOnOffSwitch.setEnabled(true);
    }

    public void disableButtons()
    {
        clearMemoryButton.setEnabled(false);
        getBatVoltButton.setEnabled(false);
        getDeviceTimeButton.setEnabled(false);
        getGpsFixButton.setEnabled(false);
        getGpsPosButton.setEnabled(false);
        getSatsUsedButton.setEnabled(false);
        getTrackButton.setEnabled(false);
        getTrackListButton.setEnabled(false);
        setDeviceTimeButton.setEnabled(false);
        startSamplingButton.setEnabled(false);
        stopSamplingButton.setEnabled(false);
        trackNumberPicker.setMaxValue(0);
        trackNumberPicker.setValue(0);
        trackNumberPicker.setMinValue(0);
        trackNumberPicker.setEnabled(false);
        gpsOnOffSwitch.setEnabled(false);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser)
        {
            if(bleDriver.isConnected())
                this.enableButtons();
            else
                this.disableButtons();
        }
    }

    public void setBleDriver(BleDriver bleDriver)
    {
        this.bleDriver = bleDriver;
    }

}
