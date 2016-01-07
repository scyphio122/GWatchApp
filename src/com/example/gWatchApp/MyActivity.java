package com.example.gWatchApp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class MyActivity extends Activity implements View.OnClickListener
{
    private BleDriver   bleDriver;
    private Button      scanButton;
    private ListView    bleScannedDevicesList;
    private BleDeviceList  bleDeviceList;
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

        bleScannedDevicesList = (ListView)findViewById(R.id.bleScanList);
        bleScannedDevicesList.setAdapter(bleDeviceList);

        bleDriver = new BleDriver(this, bleDeviceList);
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
