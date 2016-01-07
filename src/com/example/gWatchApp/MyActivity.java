package com.example.gWatchApp;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.example.gWatchApp.bledriver.BleDeviceList;
import com.example.gWatchApp.bledriver.BleDriver;


public class MyActivity extends Activity implements View.OnClickListener
{
    private BleDriver     bleDriver;
    private Button        scanButton;
    private Button        connectButton;
    private ListView      bleScannedDevicesList;
    private BleDeviceList bleDeviceList;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, "BLE NIE OBSLUGIWANE", Toast.LENGTH_SHORT).show();
            finish();
        }

        bleDeviceList = new BleDeviceList(this, R.layout.ble_scan_device_element);

        scanButton = (Button)findViewById(R.id.scanButton);
        scanButton.setOnClickListener(this);

        connectButton = (Button)findViewById(R.id.connectButton);
        connectButton.setOnClickListener(this);

        bleScannedDevicesList = (ListView)findViewById(R.id.bleScanList);
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

            }
        });
        bleDriver = new BleDriver(this, bleDeviceList);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
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
                    connectButton.setText("Disconnect");
                }
                else
                {
                    bleDriver.disconnect();
                    connectButton.setText("Connect");
                }
                break;
            }
            case R.id.bleScanList:
            {


                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == BleDriver.bleRequestEnum.REQUEST_BLE_ENABLED.ordinal())
        {
            if(resultCode == RESULT_CANCELED)
            {
                Toast.makeText(this, new String("Wylaczam aplikacje..."), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
