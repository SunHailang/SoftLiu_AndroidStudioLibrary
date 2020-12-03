package com.softliu.hlsun.netutils;

import android.os.Message;

import java.util.HashMap;

public class RequestHandler {

    public Callback callback = null;

    public int tag;

    public String urlString;

    public String Method = "GET";

    public HashMap<String, Object> Params = null;

    public HashMap<String, Object> Header = null;
    // 毫秒
    public int ConnectTimeout = 5000;

    // 毫秒
    public int ReadTimeout = 5000;

    public RequestHandler(String _url, String _method, int _tag, Callback _callback) {
        this.urlString = _url;
        this.Method = _method;
        this.tag = _tag;
        this.callback = _callback;
    }

    public RequestHandler(String _url, int _tag, Callback _callback, HashMap<String, Object> _params) {
        this.urlString = _url;
        this.tag = _tag;
        this.callback = _callback;
        this.Params = _params;
    }

    public RequestHandler(String _url, int _tag, Callback _callback, HashMap<String, Object> _params, HashMap<String, Object> _header) {
        this.urlString = _url;
        this.tag = _tag;
        this.callback = _callback;
        this.Params = _params;
        this.Header = _header;
    }

    public RequestHandler(String _url, int _tag, Callback _callback, HashMap<String, Object> _params, HashMap<String, Object> _header, int _connectTimeout, int _readTimeout) {
        this.urlString = _url;
        this.tag = _tag;
        this.callback = _callback;
        this.Params = _params;
        this.Header = _header;
        this.ConnectTimeout = _connectTimeout;
        this.ReadTimeout = _readTimeout;
    }
}

interface Callback {
    public void execute(Message msg);
}

