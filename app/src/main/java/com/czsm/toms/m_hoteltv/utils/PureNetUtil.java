package com.czsm.toms.m_hoteltv.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2018/4/10.
 */

public class PureNetUtil {
    //传入url
    public static String getServiceInfo(String str){
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) new URL(str).openConnection();
            conn.setConnectTimeout(5000);
            conn.connect();
            if (conn.getResponseCode()==200){
                InputStream is = conn.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String inputLine = "";
                String resultData = "";
                while((inputLine = br.readLine())!=null){
                    resultData+=inputLine+"\n";
                }
                conn.disconnect();
                return  resultData;
            }else{
                return "服务器连接失败";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static boolean isConnect(Context context) {
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                // 获取网络连接管理的对象
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null&& info.isConnected()) {
                    // 判断当前网络是否已经连接
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.v("error",e.toString());
        }
        return false;
    }
}
