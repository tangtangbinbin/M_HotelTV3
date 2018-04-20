package com.czsm.toms.m_hoteltv.Global;
import android.view.KeyEvent;
/**
 * Created by Administrator on 2018/4/3.
 */

public class APP {

    //webservice地址
    public static final String WEB_SERVER_URL = "http://www.scczsm.com/Service.asmx";

    // 命名空间
    public static final String NAMESPACE = "http://cdczsm.org/";

    //组合键序列
    public final static Integer[] MULT_KEY = new Integer[]{KeyEvent.KEYCODE_DPAD_UP,KeyEvent.KEYCODE_DPAD_DOWN,KeyEvent.KEYCODE_DPAD_LEFT,KeyEvent.KEYCODE_DPAD_RIGHT};

    public static String md_num = "0";//酒店编号
    public static String kf_num = "0";//房间编号
    public static String wifi_info = "";//wifi信息
    public static String kk_address= "";//客控地址

    public static int app_code = 0;//软件版本号
    public static String app_address = "";//软件下载地址
    public static String app_update = "";//软件更新提示

    public static String qdgg_address = "";//启动广告地址
    public static String bgpicture_address = "";//背景图片地址
    public static String jdinfo_musicaddress = "";//酒店介绍背景音乐地址
    public static String welocome = "";//欢迎语
    public static String db_name = "";//点播应用名称
    public static String db_address = "";//点播应用下载地址
    public static String city_name = "";//当前城市名字
    public static String jdinfo_bgpicture = "";//酒店介绍背景图片

    public static String empower = "";//授权时间




}
