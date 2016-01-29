package com.example.gWatchApp.bledriver;

import android.app.ProgressDialog;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.*;
import com.example.gWatchApp.GpsSample;
import com.example.gWatchApp.KlmCreator;
import com.example.gWatchApp.MyActivity;
import com.example.gWatchApp.R;
import com.example.gWatchApp.fragments.MapViewFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;


public class BleDriver
{
    static int msg_number = 0;
    int msg_total_bytes_number_to_receive = 0;
    int msg_number_to_receive = 0;
    int msg_current_received_bytes_number = 0;


    private BluetoothManager bleManager;
    private BluetoothAdapter bleAdapter;
    private BleDeviceScanner bleScanner;
    private BluetoothGatt connection;
    private BleReceiver bleReceiver;
    private BluetoothHandler bleHandler;
    private BleDeviceList bleDevicesList;
    private MyActivity activity;

    public boolean bleTransmissionInProgress = false;

    private BluetoothGattService service;
    private BluetoothGattCharacteristic writeChar;
    private BluetoothGattCharacteristic indicateChar;
    private BluetoothGattCharacteristic notifyChar;

    private ConcurrentLinkedQueue<String> text;
    private ConcurrentLinkedQueue<String> rawData;

    private LinkedList<GpsSample>  gpsSamples;
    private GpsSample              currentGpsSample;
    private ProgressDialog          progressDialog;
    private Handler                 progressBarHandler;

    private GoogleMap              map;
    private MapViewFragment        mapViewFragment;

    String asciiText = "";
    String rawText = "";

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

        progressDialog= new ProgressDialog(getActivity());
        progressBarHandler = new Handler();
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        currentGpsSample = new GpsSample();
    }

    public void startScanning()
    {
        bleScanner.startStopScanning(true);
    }

    public void stopScanning()
    {
        bleScanner.startStopScanning(false);
    }

    public void connect(BluetoothDevice device)
    {
        Toast.makeText(activity, "Connecting...", Toast.LENGTH_SHORT).show();
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

        asciiText = "";
        rawText = "";
        rxAsciiData.setText("");
        rxRawData.setText("");
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

        rawData.add(parseHexToString(data) + "\n");

        try
        {
        switch (data[0])
        {
            case 1:
            {
                long timestamp = parseBytesInInt(data, 1);
                text.add("Czas urządzenia:\nhh:mm:ss DD-MM-YYYY\n" + convertTimestampMillisToHex(timestamp * 1000));

//                rxAsciiData.setText(rxAsciiData.getText() + text);
                redrawGUI(rxAsciiData, rxRawData);
                break;
            }
            case 2:
            {
                switch (msg_number)
                {
                    case 0:
                    {
                        String latitude = new String(data, 1, 3) + "°" + new String(data, 4, 2) + "." + new String
                                (data, 6, 5);
                        text.add(new String("Szerokość geograficzna:\n") + latitude);
                        currentGpsSample = new GpsSample();
                        String integer = latitude.substring(0, 3);
                        String fract = latitude.substring(4, 6) + "." + latitude
                                .substring(7, 11);
                        BigDecimal fract_bd = new BigDecimal(fract);
                        double longtitude_d  = new BigDecimal(integer).doubleValue() + fract_bd.doubleValue()/60;

                        currentGpsSample.setLatitude(longtitude_d);
                        msg_number++;
                        break;
                    }
                    case 1:
                    {
                        String longtitude = new String(data, 1, 3) + "°" + new
                                String(data, 4, 2) + "." + new String(data, 6, 5);
                        text.add(new String("Długość geograficzna:\n") + longtitude);

                        String integer = longtitude.substring(0, 3);
                        String fract = longtitude.substring(4, 6) + "." + longtitude
                                .substring(7, 11);
                        BigDecimal fract_bd = new BigDecimal(fract);
                        double longtitude_d  = new BigDecimal(integer).doubleValue() + fract_bd.doubleValue()/60;

                        currentGpsSample.setLongtitude(longtitude_d);

                        msg_number++;

                        break;
                    }
                    case 2:
                    {
                        text.add(new String("Wysokość nad poziomem morza:\n") + new String(data, 1, data.length-1));
                        msg_number = 0;
                        activity.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                map.clear();
                                map.addMarker(new MarkerOptions().position(new LatLng(currentGpsSample.getLatitude(),
                                        currentGpsSample.getLongtitude())));
                            }
                        });
                        redrawGUI(rxAsciiData, rxRawData);
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
                redrawGUI(rxAsciiData, rxRawData);
                break;
            }
            case 5:
            {
                parseTrack(data);
                break;
            }
            case 6:
            {
                parseTrackList(data);
                redrawGUI(rxAsciiData, rxRawData);
                break;
            }
            case 7:
            {
                return;
            }
            case 8:
            {
                text.add(new String(data, 1, data.length-1));
                redrawGUI(rxAsciiData, rxRawData);
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
                redrawGUI(rxAsciiData, rxRawData);
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
                redrawGUI(rxAsciiData, rxRawData);
                break;
            }
            case 11:
            {
                if(data[1] == 0)
                    text.add("Pamięć wyczyszczona - OK");
                else
                    text.add("Błąd czyszczenia pamięci");
                redrawGUI(rxAsciiData, rxRawData);
                break;
            }
            case 12:
            {
                if(data[1] == 0)
                    data[1] = '0';
                text.add("Urządzenie komunikuje się z " + new String(data, 1, 1) + " satelitami");
                redrawGUI(rxAsciiData, rxRawData);
                break;
            }
        }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


}
    public void notifiedData(byte[] data)
    {
        final ToggleButton button = (ToggleButton) activity.findViewById(R.id.fixIndicatorButton);
        if(data[1] == '0' || data[1] == 0)
        {
            getActivity().runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    button.setChecked(false);
                }
            });

        }
        else
        {
            getActivity().runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    button.setChecked(true);
                }
            });

        }
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
            timestamp |= ((bytes[startOffset + i]) << (i * 8)) & (0x000000FF << (i*8));
        }
        return timestamp;
    }

    private int parseBytesInShort(byte[] bytes, int startOffset)
    {
        int timestamp = 0;
        for(int i = 0; i<2; i++)
        {
            timestamp |= ((bytes[startOffset + i]) << (i * 8)) & (0x000000FF <<(i*8));
        }
        return timestamp;
    }

    String convertTimestampMillisToHex(long timestampMillis_p)
    {
        long timestampMillis = timestampMillis_p;
        DateFormat sdf = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        TimeZone tz = TimeZone.getDefault();
        int offset = tz.getRawOffset();
        timestampMillis -= offset;
        c.setTimeInMillis(timestampMillis);

        Date currentTime = (Date)c.getTime();
        return sdf.format(currentTime);
    }

    private void parseTrackList(byte data[])
    {
        msg_number++;
        if(msg_number == 1)
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
                    if(num > 0 && num != 0xFFFF)
                    {
                        b.setEnabled(true);
                        n.setMinValue(1);
                        n.setMaxValue(num);
                        n.setValue(1);
                        n.setEnabled(true);
                    }
                    else
                    {
                        b.setEnabled(false);
                        n.setMinValue(0);
                        n.setValue(0);
                        n.setMaxValue(0);
                        n.setEnabled(false);
                    }
                }
            });
        }
        else
        {
            /// Ret val
            if(msg_number == msg_number_to_receive + 2)
            {
                int retVal = 0;
                parseBytesInInt(data, 1);
                if(retVal == 0)
                    text.add("\nLista tras odebrana pomyślnie");
                else
                    text.add("\nBłąd odbioru listy tras");
                msg_number = 0;
            }
            else
            {
                text.add("Trasa nr " + Byte.toString(data[1]) + " ; Czas zapisu: \n" + "(hh:mm:ss DD-MM-YYYY)\n" +
                        convertTimestampMillisToHex
                                (parseBytesInInt(data, 2) * 1000) +"\n");
            }
        }
    }

    private void parseTrack(byte[] data)
    {
        msg_number++;
        byte s = (byte)(msg_number % 2);
        if(msg_number == 1)
        {
            msg_total_bytes_number_to_receive = (int)parseBytesInInt(data, 1);
            msg_current_received_bytes_number = 0;
            text.add("Liczba bajtów do odebrania: " + parseBytesInShort(data, 1) + "\n");
            gpsSamples = new LinkedList<GpsSample>();
            currentGpsSample = new GpsSample();

            progressDialog.setProgress(0);

            progressDialog.setMax(msg_total_bytes_number_to_receive);
            rawText = "";
            asciiText = "";
            getActivity().runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    progressDialog = ProgressDialog.show(getActivity(), "Loading track in progress", "Please wait for " +
                            "a while");
                }
            });


        }
        else
        {
            if(msg_current_received_bytes_number < msg_total_bytes_number_to_receive)
            {
                switch (s)
                {
                    case 0:
                    {
                        msg_current_received_bytes_number += data.length - 1;
                        String longtitude = new String(data, 5, 3) + "°" + new String(data, 8, 2) + "."
                                + new String(data, 10, 4) + "'" + new String(data, 14, 1);
                        int timestamp = (int)parseBytesInInt(data, 1);
                        String integer = longtitude.substring(0, 3);
                        String fract = longtitude.substring(4, 6) + "." + longtitude
                                .substring(7, 11);
                        BigDecimal fract_bd = new BigDecimal(fract);
                        double longtitude_d  = new BigDecimal(integer).doubleValue() + fract_bd.doubleValue()/60;
                        String temp = longtitude.substring(0, 3) + "."+ longtitude.substring(4, 6) + longtitude
                                .substring
                                        (7, 11);
                        currentGpsSample.setTimestamp(timestamp);

                        currentGpsSample.setLongtitude(longtitude_d);

                        text.add("Timestamp próbki: \n" + convertTimestampMillisToHex((long)timestamp * 1000)
                        +"\n" + "Długość geograficzna: " + longtitude);
                        break;
                    }
                    case 1:
                    {
                        msg_current_received_bytes_number += data.length - 1;
                        String latitude = new String(data, 1, 3) + "°" + new String(data, 4, 2) + "."
                                + new String(data, 6, 4) + "'" + new String(data, 10, 1);
                        String temp = latitude.substring(0, 3) + "." + latitude.substring(4, 6) + latitude.substring
                                (7,11);
                        String integer = latitude.substring(0, 3);
                        String fract = latitude.substring(4, 6) + "." + latitude
                                .substring(7, 11);
                        BigDecimal fract_bd = new BigDecimal(fract);
                        double latitude_d = new BigDecimal(integer).doubleValue() + fract_bd.doubleValue()/60;
                        BigDecimal v = new BigDecimal(temp);
                        currentGpsSample.setLatitude(latitude_d );
                        gpsSamples.add(currentGpsSample);
                        currentGpsSample = new GpsSample();
//                        rawData.add(parseHexToString(data));
                        text.add("Szerokość geograficzna: " + latitude + "\n");
                        break;
                    }
                }
            }
            else
            {
                int ret_val = (int)parseBytesInInt(data, 1);
                if(ret_val == 0)
                {
                    text.add("Trasa odebrana pomyślnie");
                    progressDialog.dismiss();
                    TextView rxAsciiData = (TextView)getActivity().getVpPager().findViewById(R.id
                            .receivedASCIIDataTextFrame);
                    TextView rxRawData = (TextView)getActivity().getVpPager().findViewById(R.id
                            .receivedRawDataTextField);
                    redrawGUI(rxAsciiData, rxRawData);
                    mapViewFragment.drawOnMap(this.gpsSamples, this.map);
                }
                else
                    text.add("Błąd odbioru trasy");

                if(msg_current_received_bytes_number >= msg_total_bytes_number_to_receive)
                {
                    msg_number = 0;
                }
            }

            final ProgressDialog pd = progressDialog;
            final int progress = msg_current_received_bytes_number;
            final int max_progress = msg_total_bytes_number_to_receive;
            getActivity().runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    int perc = progress/max_progress * 100;
                    pd.setProgress(perc);
                }
            });
        }


    }

    private void redrawGUI(TextView rxAsciiData, TextView rxRawData)
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (text)
                {
                    while (text.size() != 0)
                    {
                        String txt = text.poll();
                        if (txt != null)
                        {
                            asciiText += txt + "\n";
//                            rxAsciiData.setText(rxAsciiData.getText() + txt + "\n");
                            rxAsciiData.setText(asciiText);
                        }

                    }
                }
                synchronized (rawData)
                {
                    while (rawData.size() != 0)
                    {
                        String raw = rawData.poll();
                        if (raw != null)
                        {
//                            rxRawData.setText(rxRawData.getText() + raw + "\n");
                            rawText += raw + "\n";
                            rxRawData.setText(rawText);
                        }
                    }
                }
            }
        });
    }

    public void setMap(GoogleMap map)
    {
        this.map = map;
    }

    public void setMapViewFragment(MapViewFragment mapViewFragment)
    {
        this.mapViewFragment = mapViewFragment;
    }

    public GoogleMap getMap()
    {
        return map;
    }

    public BluetoothGattCharacteristic getIndicateChar()
    {
        return indicateChar;
    }

    public BluetoothGattCharacteristic getNotifyChar()
    {
        return notifyChar;
    }
}

