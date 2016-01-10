package com.example.gWatchApp.bledriver;

import android.app.Activity;
import android.bluetooth.*;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import com.example.gWatchApp.MyActivity;
import com.example.gWatchApp.R;
import com.example.gWatchApp.fragments.ScanFragment;

import java.util.TimeZone;

/**
 * Created by Konrad on 2016-01-07.
 */
public class BluetoothHandler
{
    private final static String TAG = BluetoothHandler.class.getSimpleName();

    private BleReceiver bleReceiver;
    private BluetoothGatt connection;
    private BleDriver   bleDriver;
    public int connectionState = STATE_DISCONNECTED;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    BluetoothHandler(BleReceiver bleReceiver, BleDriver bleDriver)
    {
        this.bleReceiver = bleReceiver;
        this.bleDriver = bleDriver;
    }

    public final BluetoothGattCallback gattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            String intentAction;
            final MyActivity act = bleDriver.getActivity();
            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                intentAction = ACTION_GATT_CONNECTED;
                connectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);

                bleDriver.getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Button b = (Button)act.getVpPager().findViewById(R.id.connectButton);
                        b.setText("Disconnect");
                        Toast.makeText(act, "Discovering services...", Toast.LENGTH_SHORT).show();
                    }
                });

                Log.i(TAG, "Connected to GATT server.");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                intentAction = ACTION_GATT_DISCONNECTED;
                connectionState = STATE_DISCONNECTED;
                connection.close();
                bleDriver.getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Button b = (Button)act.getVpPager().findViewById(R.id.connectButton);
                        b.setText("Connect");
                        Toast.makeText(act, "Disconnected", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            Log.i(TAG, "Serwisy odkryte");
            final MyActivity act = bleDriver.getActivity();
            bleDriver.getActivity().runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(act, "Services Discovered", Toast.LENGTH_SHORT).show();
                }
            });

            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else
            {
                Log.wtf(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {
            bleDriver.bleTransmissionInProgress = false;
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action)
    {
        final Intent intent = new Intent(action);
        bleReceiver.onReceive(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic)
    {
        final Intent intent = new Intent(action);

        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0)
        {
            intent.putExtra(EXTRA_DATA, data);
            bleReceiver.onReceive(intent);
        }
    }

    public void setConnection(BluetoothGatt connection)
    {
        this.connection = connection;
    }




}