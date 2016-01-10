package com.example.gWatchApp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.*;
import com.example.gWatchApp.bledriver.BleDriver;
import com.example.gWatchApp.fragments.ScanFragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentActivity;


public class MyActivity extends FragmentActivity
{
    private BleDriver               bleDriver;
    private FragmentPagerAdapter    adapterViewPager;
    private ViewPager               vpPager;
    private ScanFragment            scanWindow;
//    private DevOrdersFragment       devOrdersFragment;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, "BLE NIE OBSLUGIWANE", Toast.LENGTH_SHORT).show();
            finish();
        }

        bleDriver = new BleDriver(this);

        vpPager = (ViewPager)findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager(), bleDriver);
        vpPager.setAdapter(adapterViewPager);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == BleDriver.bleRequestEnum.REQUEST_BLE_ENABLED.ordinal())
        {
            if(resultCode == RESULT_CANCELED)
            {
                Toast.makeText(this, new String("Wylaczam aplikacje..."), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public ViewPager getVpPager()
    {
        return vpPager;
    }

    /*


    public void displayReceivedData(String data)
    {
        this.rxTextView.setText(rxTextView.getText() + parseHexToString(data.getBytes()) + "\n\r");
    }

    public void displayTransmitedData(String data)
    {
        this.txTextView.setText(parseHexToString(data.getBytes()));
        rxTextView.setText("");
    }

    public Button getConnectButton()
    {
        return connectButton;
    }

    */
}
