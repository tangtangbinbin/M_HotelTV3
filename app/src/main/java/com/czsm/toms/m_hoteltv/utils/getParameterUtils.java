package com.czsm.toms.m_hoteltv.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by Administrator on 2018/4/12.
 */

public class getParameterUtils {

    /**
     * 获取版本号
     */
    public static int getVersionCode(Context con){
        int localVersion = 0;
        try {
            PackageInfo packageInfo = con.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(con.getPackageName(), 0);
            localVersion = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return localVersion;
    }

    /**
     * 获取版本号
     */
    public static String getVersionName(Context con){
        String localVersion = "";
        try {
            PackageInfo packageInfo = con.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(con.getPackageName(), 0);
            localVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return localVersion;
    }
}
