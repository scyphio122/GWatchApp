package com.example.gWatchApp.bledriver;

import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import com.example.gWatchApp.MyActivity;
import com.example.gWatchApp.R;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class BleDriver
{
    static byte msg_number = 0;

    private BluetoothManager bleManager;
    private BluetoothAdapter bleAdapter;
    private BleDeviceScanner bleScanner;
    private BluetoothGatt connection;
    private BleReceiver bleReceiver;
    private BluetoothHandler bleHandler;
    private BleDeviceList bleDevicesList;
    private MyActivity activity;

    public boolean bleTransmissionInProgress = false;

    private ArrayList<String> text;
    private static String currentText;
    BluetoothGattService service;
    BluetoothGattCharacteristic writeChar;
    BluetoothGattCharacteristic indicateChar;
    BluetoothGattCharacteristic notifyChar;


    public enum bleRequestEnum
    {
        REQUEST_BLE_DISABLED,
        REQUEST_BLE_ENABLED
    }

    public BleDriver(MyActivity activity)
    {
        bleManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bleManager.getAdapter();
        this.activity = activity;
        /// Check if BLE is enabled
        if (bleAdapter == null || !bleAdapter.isEnabled())
        {
            Intent enableBleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBleIntent, 1);
        }
        bleDevicesList = new BleDeviceList(activity, R.layout.ble_scan_device_element);
        bleScanner = new BleDeviceScanner(bleAdapter, bleDevicesList, activity);

        bleReceiver = new BleReceiver(this);
        bleHandler = new BluetoothHandler(bleReceiver, this);

        text = new ArrayList<String>(0);
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
        TextView txTextView = (TextView) activity.getVpPager().findViewById(R.id.sentDataTextField);
        txTextView.setText(parseHexToString(data));

        TextView rxRawData = (TextView) this.activity.getVpPager().findViewById(R.id.receivedRawDataTextField);
        TextView rxAsciiData = (TextView) this.activity.getVpPager().findViewById(R.id.receivedASCIIDataTextFrame);

        rxAsciiData.setText(" ");
        rxRawData.setText(" ");
    }

    public void updateTimeOnDevice()
    {
        /// Set the current time on the device
        TimeZone timeZone = TimeZone.getDefault();
        long timeUTC = System.currentTimeMillis();

        int offset = timeZone.getRawOffset() / 1000;
        int time = (int) (timeUTC / 1000) + offset;


        byte[] data = new byte[5];
        data[0] = 0x00;
        data[1] = (byte) (time & 0xFF);
        data[2] = (byte) ((time >> 8) & 0xFF);
        data[3] = (byte) ((time >> 16) & 0xFF);
        data[4] = (byte) ((time >> 24) & 0xFF);

        sendData(data);
    }

    public void receivedData(byte[] data)
    {
        // this.activity.displayReceivedData(new String(data));
        TextView rxRawData = (TextView) this.activity.getVpPager().findViewById(R.id.receivedRawDataTextField);
        TextView rxAsciiData = (TextView) this.activity.getVpPager().findViewById(R.id.receivedASCIIDataTextFrame);



        switch (data[0])
        {
            case 1:
            {

                long timestamp = parseBytesInInt(data, 1);
                text.add(convertTimestampToHex(timestamp*1000));

//                rxAsciiData.setText(rxAsciiData.getText() + text);
                break;
            }
            case 2:
            {
                switch (msg_number)
                {
                    case 0:
                    {
                        text.add(new String("Szerokość geograficzna:\n") + new String(data, 1, 3) + '°' + new
                            String(data, 4, 2) + "." + new String(data, 6, 5));
                        break;
                    }
                    case 1:
                    {
                        text.add(new String("Długość geograficzna:\n") + new String(data, 1, 3) + '°' + new
                                String(data, 4, 2) + "." + new String(data, 6, 5));
                        break;
                    }
                    case 2:
                    {
                        text.add(new String("Wysokość nad poziomem morza:\n") + new String(data, 1, data.length-1));
                        msg_number = 0;
                        break;
                    }

                }
                msg_number++;

            }
        }
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(text.size() != 0)
                {
                    currentText = String.copyValueOf(text.get(0).toCharArray());
                    rxAsciiData.setText(rxAsciiData.getText() + currentText + "\n");
                    rxRawData.setText(rxRawData.getText() + parseHexToString(data) + "\n");
                    text.remove(0);
                }
            }
        });
}

    public MyActivity getActivity()
    {
        return activity;
    }

    public BleDeviceList getBleDevicesList()
    {
        return bleDevicesList;
    }

    String parseHexToString(byte[] data)
    {
        StringBuilder sb = new StringBuilder(data.length);

        for (int i = 0; i < data.length; i++)
        {
            sb.append(String.format("0x%02X", data[i]));
            sb.append(" ");
        }
        return sb.toString();
    }

    private int parseBytesInInt(byte[] bytes, int startOffset)
    {
        int timestamp = (bytes[startOffset]) |
                (bytes[startOffset + 1] << 8) & 0x0000ff00 |
                (bytes[startOffset + 2] << 16) & 0x00ff0000 |
                (bytes[startOffset + 3] << 24) & 0xff000000;
        return timestamp;
    }

    String convertTimestampToHex(long timestampMillis)
    {
        DateFormat sdf = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");

        timestampMillis = timestampMillis - TimeZone.getDefault().getRawOffset();
        Date date = new Date(timestampMillis);

        String text = "Czas urzadzenia to\n" +
                 "(HH:MM:SS DD-MM-YYYY) :\n" + sdf.format(date) + "\n\n";
        return text;
    }
}

