package com.example.gWatchApp;

/**
 * Created by Konrad on 2016-01-11.
 */
public class GpsSample
{
    private int timestamp;
    private double latitude;
    private double longtitude;

    public int getTimestamp()
    {
        return timestamp;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongtitude()
    {
        return longtitude;
    }

    public void setTimestamp(int timestamp)
    {
        this.timestamp = timestamp;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public void setLongtitude(double longtitude)
    {
        this.longtitude = longtitude;
    }
}
