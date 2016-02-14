package com.example.gWatchApp.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import com.example.gWatchApp.bledriver.BleDriver;

/**
 * Created by Konrad on 2016-01-18.
 */
public class ClearMemoryDialog extends DialogFragment
{
    private Activity activity;
    private BleDriver bleDriver;
    AlertDialog.Builder dialogBuilder;


    public void showDialog()
    {
        dialogBuilder = new AlertDialog.Builder(activity);

        dialogBuilder.setMessage("Czy na pewno chcesz kontynuowac? Utracisz wszystkie dane o trasach.");
        dialogBuilder.setPositiveButton("Tak", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                byte[] data = new byte[1];
                data[0] = 0x0B;
                bleDriver.sendData(data);
            }
        });
        dialogBuilder.setNegativeButton("Nie", null);

        dialogBuilder.create().show();
    }

    public void setActivity(Activity activity)
    {
        this.activity = activity;
    }

    public void setBleDriver(BleDriver bleDriver)
    {
        this.bleDriver = bleDriver;
    }
}
