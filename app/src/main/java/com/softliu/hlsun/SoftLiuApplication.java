package com.softliu.hlsun;

import android.app.Application;
import android.content.Context;

/*
 *    Application 方法执行顺序
 *       构造方法 -> attachBaseContext -> onCreate
 */

public class SoftLiuApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
