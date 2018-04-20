package com.czsm.toms.m_hoteltv.config;

import com.czsm.toms.m_hoteltv.bean.IPAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/4/19.
 */

public class APPConfig {
    public static String Md_Number = "2";//酒店编号
    public static String strwifimm = "WIFI:02887021000 订房电话：02887021000";//wifi信息
    public static String strFjbh = "8888";//客房编号
    public static String strkkdz = "";//客控地址
    public static String strdb = "1";//是否有本地点播 1为有
    public static String strdbfwqdz = "";//本地点播地址
    public static String stryyxzdz = "http://sfapp01.oss-cn-hangzhou.aliyuncs.com/apk/com.ktcp.video.e73ffdcf90e7e9d9a9d554a60e84ac11.apk";//点播应用
    public static String strggy = "您好，欢迎入住iu酒店，我们将竭诚为您服务！酒店为您提供尽善尽美的服务、温馨舒适的客房。祝您入住愉快。";//欢迎语

    public static List<IPAddress> iplist = new ArrayList<>();
    public static List<IPAddress> loadaddress(){
        iplist.clear();
        for(int i=1;i<38;i++){
            IPAddress ipAddress = new IPAddress();
            ipAddress.setNum(i);
            ipAddress.setIpaddress("http://172.16.0.9/live/iptv00"+i+".m3u8");
            iplist.add(ipAddress);
        }
        return iplist;
    }
}
