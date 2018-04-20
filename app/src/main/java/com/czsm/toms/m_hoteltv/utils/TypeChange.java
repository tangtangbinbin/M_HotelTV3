package com.czsm.toms.m_hoteltv.utils;

import java.util.List;

/**
 * Created by Administrator on 2018/4/12.
 */

public class TypeChange {

    //字符串转数组
    public static String[] stringToArray(String str){
        String[] strs=str.split(",");
        return strs;
    }

    //数组转字符串
    public static String arrayToString(String[] strings){
        String data = "";
        for (int i=0;i<strings.length;i++){
            if (i!=strings.length-1){
                data +=strings[i]+",";
            }
            if (i==strings.length-1){
                data +=strings[i];
            }
        }
        return data;
    }
}
