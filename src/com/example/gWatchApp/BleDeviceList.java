package com.example.gWatchApp;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.LinkedList;


/**
 * Created by Konrad on 2016-01-07.
 */
public class BleDeviceList extends ArrayAdapter<BluetoothDevice>
    {
    public BleDeviceList(Context context, int resource)
    {
        super(context, resource, new LinkedList<BluetoothDevice>());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        BluetoothDevice device = getItem(position);
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.ble_scan_device_element,parent, false);
        TextView deviceName = (TextView) convertView.findViewById(R.id.deviceName);
        TextView deviceMAC = (TextView) convertView.findViewById(R.id.deviceMAC);
//        Button   connectButton = (Button)convertView.findViewById(R.id.deviceConnect);

        deviceName.setText(device.getName());
        deviceMAC.setText(device.getAddress());

        return convertView;
    }
}
