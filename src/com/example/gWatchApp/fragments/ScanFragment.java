package com.example.gWatchApp.fragments;


import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.example.gWatchApp.R;
import com.example.gWatchApp.bledriver.BleDeviceList;
import com.example.gWatchApp.bledriver.BleDriver;

/**
 * Created by Konrad on 2016-01-09.
 */
public class ScanFragment extends Fragment implements View.OnClickListener
{
    private String          title;
    private int             page;

    private BleDriver       bleDriver;
    private Button          scanButton;
    private Button          connectButton;
    private ListView        bleScannedDevicesList;
    private BleDeviceList   bleDeviceList;


    public static ScanFragment newInstance(int page, String title)
    {
        ScanFragment scanFragment = new ScanFragment();
        Bundle args = new Bundle();
        args.putInt("scanInt", page);
        args.putString("scanTitle", title);

        scanFragment.setArguments(args);

        return scanFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("scanInt", 0);
        title = getArguments().getString("scanString");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup containter, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.scanview, containter, false);

        //bleDeviceList = new BleDeviceList(getActivity(), R.layout.ble_scan_device_element);

        scanButton = (Button)view.findViewById(R.id.scanButton);
        scanButton.setOnClickListener(this);

        connectButton = (Button)view.findViewById(R.id.connectButton);
        connectButton.setOnClickListener(this);


        bleScannedDevicesList = (ListView)view.findViewById(R.id.bleScanList);
        bleScannedDevicesList.setAdapter(bleDeviceList);
        bleScannedDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                if(bleDeviceList.chosenView != null)
                {
                    bleDeviceList.chosenView.setSelected(false);
                    view.setBackgroundColor(Color.BLUE);
                }
                bleDeviceList.chosenView = view;
                view.setBackgroundColor(Color.RED);
                view.setSelected(true);
                connectButton.setEnabled(true);

            }
        });

        return view;
    }

    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.scanButton:
            {
                bleDeviceList.clear();
                bleDriver.startScanning();
                break;
            }
            case R.id.connectButton:
            {
                if(!bleDriver.isConnected())
                {
                    if (bleDeviceList.chosenView == null)
                        return;
                    TextView deviceMACAddress = (TextView) bleDeviceList.chosenView.findViewById(R.id.deviceMAC);

                    BluetoothDevice chosenDevice = bleDriver.getBleAdapter().getRemoteDevice(String.valueOf
                            (deviceMACAddress.getText()));

                    bleDriver.connect(chosenDevice);
                }
                else
                {
                    bleDriver.disconnect();
                }
                break;
            }
        }
    }

    public void setBleDriver(BleDriver bleDriver)
    {
        this.bleDriver = bleDriver;
    }

    public void setBleDeviceList(BleDeviceList bleDeviceList)
    {
        this.bleDeviceList = bleDeviceList;
    }

    public Button getScanButton()
    {
        return scanButton;
    }

    public Button getConnectButton()
    {
        return connectButton;
    }

    public ListView getBleScannedDevicesList()
    {
        return bleScannedDevicesList;
    }
}
