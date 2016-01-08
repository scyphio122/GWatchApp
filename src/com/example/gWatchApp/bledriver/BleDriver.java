package com.example.gWatchApp.bledriver;

import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import com.example.gWatchApp.MyActivity;
import com.example.gWatchApp.R;
import org.apache.http.util.ByteArrayBuffer;


public class BleDriver
{
    private BluetoothManager            bleManager;
    private BluetoothAdapter            bleAdapter;
    private BleDeviceScanner            bleScanner;
    private BluetoothGatt               connection;
    private BleReceiver                 bleReceiver;
    private BluetoothHandler            bleHandler;
    private MyActivity                  activity;
    public boolean                      bleTransmissionInProgress = false;

    BluetoothGattService                service;
    BluetoothGattCharacteristic         writeChar;
    BluetoothGattCharacteristic         indicateChar;
    BluetoothGattCharacteristic         notifyChar;


    public enum bleRequestEnum
    {
        REQUEST_BLE_DISABLED,
        REQUEST_BLE_ENABLED
    }

    public BleDriver(MyActivity activity, BleDeviceList deviceList)
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

        bleScanner = new BleDeviceScanner(bleAdapter, deviceList, activity);

        bleReceiver = new BleReceiver(this);
        bleHandler = new BluetoothHandler(bleReceiver, this);
    }

    public void startScanning()
    {
        bleScanner.startScanning(true);
    }

    public void stopScanning()
    {
        bleScanner.startScanning(false);
    }

    public void connect(BluetoothDevice device)
    {
       connection = device.connectGatt(activity, false, bleHandler.gattCallback);
        bleReceiver.setConnection(this.connection);
       bleHandler.setConnection(this.connection);

    }

    public void disconnect()
    {
        connection.disconnect();
        connection = null;
    }

    public boolean isConnected()
    {
        return bleHandler.connectionState == BluetoothHandler.STATE_CONNECTED;
    }


    public BluetoothManager getBleManager()
    {
        return bleManager;
    }

    public BluetoothAdapter getBleAdapter()
    {
        return bleAdapter;
    }

    public void setService(BluetoothGattService service)
    {
        this.service = service;
    }

    public void setWriteChar(BluetoothGattCharacteristic writeChar)
    {
        this.writeChar = writeChar;
    }

    public void setIndicateChar(BluetoothGattCharacteristic indicateChar)
    {
        this.indicateChar = indicateChar;
    }

    public void setNotifyChar(BluetoothGattCharacteristic notifyChar)
    {
        this.notifyChar = notifyChar;
    }

    public void sendData(byte[] data)
    {
        writeChar.setValue(data);
        connection.writeCharacteristic(writeChar);
        String text = new String(data);
        activity.displayTransmitedData(text);
    }

    public void receivedData(byte[] data)
    {
        this.activity.displayReceivedData(new String(data));
    }

    public MyActivity getActivity()
    {
        return activity;
    }
}
