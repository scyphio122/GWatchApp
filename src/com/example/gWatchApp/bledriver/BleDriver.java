package com.example.gWatchApp.bledriver;

import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import com.example.gWatchApp.MyActivity;
import com.example.gWatchApp.R;

import java.nio.ByteBuffer;
import java.sql.Time;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class BleDriver
{
    static int msg_number = 0;
    int msg_number_to_receive = 0;
    private BluetoothManager bleManager;
    private BluetoothAdapter bleAdapter;
    private BleDeviceScanner bleScanner;
    private BluetoothGatt connection;
    private BleReceiver bleReceiver;
    private BluetoothHandler bleHandler;
    private BleDeviceList bleDevicesList;
    private MyActivity activity;

    public boolean bleTransmissionInProgress = false;

    private ConcurrentLinkedQueue<String> text;
    private ConcurrentLinkedQueue<String> rawData;

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

        text = new ConcurrentLinkedQueue<String>();
        rawData = new ConcurrentLinkedQueue<String>();

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

        rawData.add(parseHexToString(data));

        Log.i("Ble receiver", "Receiver called. Data Length: " + data.length);
        try
        {
        switch (data[0])
        {
            case 1:
            {

                long timestamp = parseBytesInInt(data, 1);
                text.add("Czas urządzenia:\nhh:mm:ss DD-MM-YYYY\n" + convertTimestampToHex(timestamp*1000));

//                rxAsciiData.setText(rxAsciiData.getText() + text);
                break;
            }
            case 2:
            {
                switch (msg_number)
                {
                    case 0:
                    {
                        text.add(new String("Szerokość geograficzna:\n") + new String(data, 1, 3) + "°" + new
                                String(data, 4, 2) + "." + new String(data, 6, 5));
                        msg_number++;
                        break;
                    }
                    case 1:
                    {

                        text.add(new String("Długość geograficzna:\n") + new String(data, 1, 3) + "°" + new
                                String(data, 4, 2) + "." + new String(data, 6, 5));
                        msg_number++;
                        break;
                    }
                    case 2:
                    {
                        text.add(new String("Wysokość nad poziomem morza:\n") + new String(data, 1, data.length-1));
                        msg_number = 0;
                        break;
                    }
                }

                break;
            }
            case 3:
                return;
            case 4:
            {
                if(data[1] == '0')
                    text.add(new String("Brak fix'a pozycji"));
                else
                    text.add(new String("Fix pozycji OK"));
                break;
            }
            case 5:
            {
                return;
            }
            case 6:
            {
                if(msg_number == 0)
                {
                    msg_number_to_receive = data[1];
                    text.add("Lista dostępnych tras zapisanych w pamięci urządzenia: " + Byte.toString(data[1]) + "\n");
                    final Button b = (Button)activity.getVpPager().findViewById(R.id.getTrackButton);
                    final NumberPicker n = (NumberPicker) activity.getVpPager().findViewById(R.id.trackNumberPicker);
                    final byte num = data[1];
                        activity.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(num > 0)
                                {
                                    Log.i("Get Track", "Odblokowuje przycisk getTrack.");
                                    b.setEnabled(true);
                                    n.setMinValue(1);
                                    n.setMaxValue(num);
                                    n.setValue(1);
                                }
                                else
                                {
                                    Log.i("Get Track", "Blokuje przycisk getTrack.");
                                    b.setEnabled(false);
                                    n.setMinValue(0);
                                    n.setValue(0);
                                    n.setMaxValue(0);
                                }
                            }
                        });
                }
                else
                {
                    text.add("Trasa nr " + Byte.toString(data[1]) + " ; Czas zapisu: \n" + "(hh:mm:ss DD-MM-YYYY)\n" +
                            convertTimestampToHex
                                    (parseBytesInInt(data, 2) * 1000));
                }
                msg_number++;
                if(msg_number == msg_number_to_receive + 1)
                {
                    msg_number = 0;
                }
                break;
            }
            case 7:
            {
                return;
            }
            case 8:
            {
                text.add(new String(data, 1, data.length-1));
                break;
            }
            case 9:
            {
                if(data[1] == 0)
                    text.add("Rozpoczęcie zapisu próbek - OK");
                else if(data[1] == 8)
                {
                    text.add("Błąd rozpoczęcia zapisu próbek - zły stan");
                }
                break;
            }
            case 10:
            {
                if(data[1] == 0)
                    text.add("Zakończenie zapisu próbek - OK");
                else if(data[1] == 8)
                {
                    text.add("Błąd zakończenia zapisu próbek - zły stan");
                }
                break;
            }
            case 11:
            {
                if(data[1] == 0)
                    text.add("Pamięć wyczyszczona - OK");
                else
                    text.add("Błąd czyszczenia pamięci");
                break;
            }
            case 12:
            {
                if(data[1] == 0)
                    data[1] = '0';
                text.add("Urządzenie komunikuje się z " + new String(data, 1, 1) + " satelitami");
                break;
            }
        }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        Log.i("Ble receiver", "Packet arrived: " + parseHexToString(data));

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Log.i("GUI thread", "GUI redrawed");
                synchronized (text)
                {
                    while (text.size() != 0)
                    {
                        String txt = text.poll();
                        if (txt != null)
                            rxAsciiData.setText(rxAsciiData.getText() + txt + "\n");
                    }
                }
                synchronized (rawData)
                {
                    while (rawData.size() != 0)
                    {
                        String raw = rawData.poll();
                        if (raw != null)
                            rxRawData.setText(rxRawData.getText() + raw + "\n");
                    }
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

    private long parseBytesInInt(byte[] bytes, int startOffset)
    {

        long timestamp = 0;
        for(int i = 0; i<4; i++)
        {
            timestamp |= ((bytes[startOffset + i]) << (i * 8)) & (0x000000FF <<i*8);
        }
        return timestamp;
    }

    String convertTimestampToHex(long timestampMillis)
    {
        DateFormat sdf = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        TimeZone tz = TimeZone.getDefault();
        int offset = tz.getRawOffset();
        timestampMillis -= offset;
        c.setTimeInMillis(timestampMillis);

        Date currentTime = (Date)c.getTime();
        return sdf.format(currentTime);
    }
}

