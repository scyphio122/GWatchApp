package com.example.gWatchApp;

/**
 * Created by Konrad on 2016-01-11.
 */
public class GpsSample
{
    private int timestamp;
    private String latitude;
    private String longtitude;

    public int getTimestamp()
    {
        return timestamp;
    }

    public String getLatitude()
    {
        return latitude;
    }

    public String getLongtitude()
    {
        return longtitude;
    }

    public void setTimestamp(int timestamp)
    {
        this.timestamp = timestamp;
    }

    public void setLatitude(String latitude)
    {
        this.latitude = latitude;
    }

    public void setLongtitude(String longtitude)
    {
        this.longtitude = longtitude;
    }
}
