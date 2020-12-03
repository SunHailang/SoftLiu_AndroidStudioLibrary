package com.softliu.hlsun.netutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class NetworkUtils {

    public static final int GET_TOKEN = 1;
    public static final int CHECK_TOKEN = 2;
    public static final int REFRESH_TOKEN = 3;
    public static final int GET_INFO = 4;
    public static final int GET_IMG = 5;

    public void SendRequest(final RequestHandler handler) {
        // 开启线程发送请求

        HttpsThread httpsThread = new HttpsThread(handler);
        httpsThread.start();

    }

}
