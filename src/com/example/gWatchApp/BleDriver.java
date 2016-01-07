package com.example.gWatchApp;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.ListAdapter;
import android.widget.Toast;


/**
 * Created by Konrad on 2016-01-06.
 */


class DeviceScanActivity
{
    private BluetoothAdapter bleAdapter;
    private boolean scanning;
    private final Handler handler = new Handler();
    private static final short SCAN_PERIOD = 10000;
    private MyActivity activity;
    private BleDeviceList leDeviceListAdapter;


    private final BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback()
    {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord)
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    leDeviceListAdapter.add(bluetoothDevice);
                    leDeviceListAdapter.notifyDataSetChanged();
                }
            });

        }
    };

    DeviceScanActivity(BluetoothAdapter bleAdapter, BleDeviceList deviceListHandle, MyActivity activity)
    {
        this.bleAdapter = bleAdapter;
        this.leDeviceListAdapter = deviceListHandle;
        this.activity = activity;
    }

    public void startScanning(final boolean enable)
    {
        if(enable)
        {
            handler.postDelayed(new Runnable(){
              @Override
              public void run()
              {
                  if(scanning == true)
                  {
                      scanning = false;
                      bleAdapter.stopLeScan(leScanCallback);
                      Toast.makeText(activity, "Nie znaleziono zadnego urzadzenia", Toast.LENGTH_SHORT).show();
                  }
              }
            }, SCAN_PERIOD);

            scanning = true;
            bleAdapter.startLeScan(leScanCallback);
        }
        else
        {
            scanning = false;
            bleAdapter.stopLeScan(leScanCallback);
        }

    }

}

public class BleDriver
{
    private BluetoothManager bleManager;
    private BluetoothAdapter bleAdapter;
    private DeviceScanActivity bleScan;
    MyActivity activity;
    public enum bleRequestEnum
    {
        REQUEST_BLE_DISABLED,
        REQUEST_BLE_ENABLED
    }

    BleDriver(MyActivity activity, BleDeviceList deviceList)
    {
        bleManager = (BluetoothManager)activity.getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bleManager.getAdapter();
        this.activity = activity;
        /// Check if BLE is enabled
        if(bleAdapter == null || !bleAdapter.isEnabled())
        {
            Intent enableBleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBleIntent, 1);
        }

        bleScan = new DeviceScanActivity(bleAdapter, deviceList, activity);
    }

    public void startScanning()
    {
        bleScan.startScanning(true);
    }

    public void stopScanning()
    {
        bleScan.startScanning(false);
    }
}
