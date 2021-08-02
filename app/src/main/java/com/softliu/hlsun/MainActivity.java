package com.softliu.hlsun;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.unity3d.player.UnityPlayerActivity;

public class MainActivity extends UnityPlayerActivity {

    public static String TAG = "SoftLiu";

    public static Activity m_activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_activity = this;
        Log.i(TAG, "onCreate: Android Start Success.");
    }
}
