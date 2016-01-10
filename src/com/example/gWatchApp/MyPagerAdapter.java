package com.example.gWatchApp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.example.gWatchApp.bledriver.BleDriver;
import com.example.gWatchApp.fragments.DevOrdersFragment;
import com.example.gWatchApp.fragments.ScanFragment;

/**
 * Created by Konrad on 2016-01-10.
 */
public class MyPagerAdapter extends FragmentPagerAdapter
{
    private static int NUM_ITEMS = 3;
    private static BleDriver driver;

    public MyPagerAdapter(FragmentManager fragmentManager, BleDriver driver)
    {
        super(fragmentManager);
        this.driver = driver;
    }

    @Override
    public int getCount()
    {
        return NUM_ITEMS;
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
            {
                ScanFragment fragment = ScanFragment.newInstance(0, new String("Devices List"));
                fragment.setBleDriver(driver);
                fragment.setBleDeviceList(driver.getBleDevicesList());
                return fragment;
            }

            case 1:
            {
                DevOrdersFragment fragment = DevOrdersFragment.newInstance(1, "Device Orders");
                fragment.setBleDriver(driver);
                return fragment;
            }
//
//                case 2:
//                    return ScanFragment.newInstance(2, )
            default:
                return new Fragment();
        }
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        switch(position)
        {
            case 0:
                return "Devices List";
            case 1:
                return "Device Orders";
            case 2:
                return "Map";

        }
        return null;
    }



}