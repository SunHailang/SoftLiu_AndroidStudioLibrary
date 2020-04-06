package com.softliu.hlsun;

import android.app.Activity;

public class SoftLiuNative {

    public static String TAG = "SoftLiu";

    public static Activity m_activity;

    public SoftLiuNative(Activity a){
        m_activity =a;
    }

    public static SoftLiuNative Init(Activity a){
        return new SoftLiuNative(a);
    }

    public int Add(int a, int b)
    {
        return a+b;
    }

    public String GetPackName()
    {
        return m_activity.getPackageName();
    }
}
