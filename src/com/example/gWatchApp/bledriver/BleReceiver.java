package com.example.gWatchApp.bledriver;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by Konrad on 2016-01-07.
 */
public class BleReceiver
{
    private BluetoothGatt connection;
    private BleDriver     bleDriver;

    private static final Handler handler = new Handler();
    private static final short DEV_TIME_UPDATE_DELAY = 500;

    private BluetoothGattService                service;
    private BluetoothGattCharacteristic         writeChar;
    private BluetoothGattCharacteristic         indicateChar;
    private BluetoothGattCharacteristic         notifyChar;

    private final static String TAG = BluetoothHandler.class.getSimpleName();

    BleReceiver(BleDriver bleDriver)
    {
        this.bleDriver = bleDriver;
    }

    public void onReceive(BluetoothGattCharacteristic characteristic, Intent intent)
    {
        final String action = intent.getAction();
        switch (action)
        {
            case BluetoothHandler.ACTION_GATT_CONNECTED:
            {
                ///TODO: Write CCCD descriptor
                Log.i(TAG, "Rozpoczynam odkrywanie serwisow");
                connection.discoverServices();
                break;
            }
            case BluetoothHandler.ACTION_GATT_DISCONNECTED:
            {

                break;
            }
            case BluetoothHandler.ACTION_GATT_SERVICES_DISCOVERED:
            {
                service = connection.getService(UUID.fromString
                        ("ac040001-7214-e7b9-3c20-16caac2007b0"));
                writeChar = service.getCharacteristic(UUID.fromString
                        ("ac040002-7214-e7b9-3c20-16caac2007b0"));
                indicateChar = service.getCharacteristic(UUID.fromString
                        ("ac040003-7214-e7b9-3c20-16caac2007b0"));
                notifyChar = service.getCharacteristic(UUID.fromString
                        ("ac040004-7214-e7b9-3c20-16caac2007b0"));

                bleDriver.setService(service);
                bleDriver.setWriteChar(writeChar);
                bleDriver.setIndicateChar(indicateChar);
                bleDriver.setNotifyChar(notifyChar);

                BluetoothGattDescriptor indicateDescriptor = indicateChar.getDescriptor(UUID.fromString
                        ("00002902-0000-1000-8000-00805f9b34fb"));
                BluetoothGattDescriptor notifyDescriptor = notifyChar.getDescriptor(UUID.fromString
                        ("00002902-0000-1000-8000-00805f9b34fb"));


                Log.i(TAG, "Sprawdzam wartosc indicateDescriptor");
                /// Enable the indicate CCCD descriptor
                indicateDescriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                Log.i(TAG, "Wpisuje INDICATION_ENABLED do indicateDescriptor");

                Log.i(TAG, "Sprawdzam wartosc notifyDescriptor");
                /// Enable the notify CCCD descriptor
                notifyDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                Log.i(TAG, "Wpisuje NOTIFICATION_ENABLED do notifyDescriptor");

                connection.setCharacteristicNotification(indicateChar, true);
                connection.setCharacteristicNotification(notifyChar, true);

                /// Send the descriptors
                bleDriver.bleTransmissionInProgress = true;

                connection.writeDescriptor(indicateDescriptor);
//                this.bleDriver.getBleAdapter().getState();
//                bleDriver.bleTransmissionInProgress = true;
//                while(bleDriver.bleTransmissionInProgress == true)
//                {}
//                connection.writeDescriptor(notifyDescriptor);
//                while(bleDriver.bleTransmissionInProgress == true)
//                {}
                final BluetoothGatt con = connection;
                handler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        con.writeDescriptor(notifyDescriptor);
                    }
                }, 1000);
                Log.i(TAG, "Wysylam deskryptory");


                final BleDriver temp = bleDriver;
                handler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        temp.updateTimeOnDevice();
                    }
                }, DEV_TIME_UPDATE_DELAY);

                break;
            }
            case BluetoothHandler.ACTION_DATA_AVAILABLE:
            {
                byte data[] = intent.getByteArrayExtra(BluetoothHandler.EXTRA_DATA);
                if(characteristic == indicateChar)
                    bleDriver.receivedData(data);
                else
                if(characteristic == notifyChar)
                    bleDriver.notifiedData(data);
                break;
            }
        }
    }

    public void setConnection(BluetoothGatt connection)
    {
        this.connection = connection;
    }
}
