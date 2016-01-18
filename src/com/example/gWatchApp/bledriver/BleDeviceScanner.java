package com.example.gWatchApp.bledriver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.widget.Toast;
import com.example.gWatchApp.MyActivity;

/**
 * Created by Konrad on 2016-01-06.
 */


public class BleDeviceScanner
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
                boolean newDevice = true;
                @Override
                public void run()
                {
                    /// Check if the device does not already exist on the list
                    for(int i=0; i<leDeviceListAdapter.getCount(); i++)
                    {
                        if(leDeviceListAdapter.getItem(i).getAddress().equals(bluetoothDevice.getAddress()))
                        {
                            newDevice = false;
                            break;
                        }
                        else
                        {
                            newDevice = true;
                        }
                    }
                    /// If the device does not exist on the list, then add it
                    if(newDevice)
                    {
                        leDeviceListAdapter.add(bluetoothDevice);
                        leDeviceListAdapter.notifyDataSetChanged();
                        scanning = false;
                    }
                }
            });

        }
    };

    BleDeviceScanner(BluetoothAdapter bleAdapter, BleDeviceList deviceListHandle, MyActivity activity)
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
                      Toast.makeText(activity, "Scan completed", Toast.LENGTH_SHORT).show();
                  }
              }
            }, SCAN_PERIOD);

            scanning = true;
            bleAdapter.startLeScan(leScanCallback);
            Toast.makeText(activity, "Scanning...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            scanning = false;
            bleAdapter.stopLeScan(leScanCallback);
            Toast.makeText(activity, "Scan completed", Toast.LENGTH_SHORT).show();
        }

    }

}
