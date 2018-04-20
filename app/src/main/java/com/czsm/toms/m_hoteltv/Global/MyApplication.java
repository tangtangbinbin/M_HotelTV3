package com.czsm.toms.m_hoteltv.Global;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.czsm.toms.m_hoteltv.bean.IPAddress;
import com.czsm.toms.m_hoteltv.config.APPConfig;
import com.czsm.toms.m_hoteltv.utils.DownloadPicture;
import com.czsm.toms.m_hoteltv.utils.MacUtils;
import com.czsm.toms.m_hoteltv.utils.NetWorkChangReceiver;
import com.czsm.toms.m_hoteltv.utils.PureNetUtil;
import com.czsm.toms.m_hoteltv.webservice.MyWebService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;

import org.ksoap2.serialization.SoapObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2018/4/12.
 */

public class MyApplication extends Application {
    public static SharedPreferences config ;
    public static SharedPreferences.Editor config_editor;
    public static List<IPAddress> iplist = new ArrayList<>();
    HashMap<String, String> properties ;

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();

        config = context.getSharedPreferences("config03",Context.MODE_PRIVATE);
        config_editor = config.edit();

        // 获取当前包名
        String packageName = context.getPackageName();
        // 获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        Bugly.init(getApplicationContext(), "65a8c9b9a9", true);
        //CrashReport.initCrashReport(getApplicationContext(), "65a8c9b9a9", true,strategy);

        initdata();//每次开启时加载新的数据

    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }


    public void initdata(){
        if (PureNetUtil.isConnect(this)){
            //加载网络数据
            /**
             * 通过mac得到酒店房间信息
             */
            final String mac = MacUtils.getMac(this).replace(":","");
            if (mac.equals("000000000000")){//没获取到网络就不获取网络数据
                return;
            }
            properties= new HashMap<String, String>();
            properties.put("vSn",mac);//设置参数
            MyWebService.callWebService(APP.WEB_SERVER_URL, "getFjInfobySn", properties, new MyWebService.WebServiceCallBack() {
                @Override
                public void callBack(SoapObject result) {
                    if(result != null){
                        try {
                            Log.w("webservice1",result.toString());
                            SoapObject detail = (SoapObject) result.getProperty("getFjInfobySnResult");
                            SoapObject diffgram = (SoapObject) detail.getProperty("diffgram");
                            SoapObject NewDataSet = (SoapObject) diffgram.getProperty("NewDataSet");
                            SoapObject ds = (SoapObject) NewDataSet.getProperty("ds");

                            String strFjbh = ds.getProperty("strFjbh").toString();//客房编号
                            String Md_Number = (String) ds.getProperty("Md_Number").toString();//酒店编号
                            String strwifimm = (String) ds.getProperty("strwifimm").toString();//wifi密码
                            String strkkdz = (String) ds.getProperty("strkkdz").toString();//可控地址

                            //保存到全局变量
                            APP.md_num = Md_Number;
                            APP.kf_num = strFjbh;
                            APP.wifi_info = strwifimm;
                            APP.kk_address = strkkdz;
                            Log.w("the md_nunm",APP.md_num+"");
                            //保存信息到sp
                            config_editor.putString("strFjbh",strFjbh);
                            config_editor.putString("Md_Number",Md_Number);
                            config_editor.putString("strwifimm",strwifimm);
                            config_editor.putString("strkkdz",strkkdz);
                            config_editor.commit();
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }else{
                        Log.w("webservice1","获取数据失败");
                    }
                }
            });

            /**
             * 获取软件信息
             */
            properties= new HashMap<String, String>();
            MyWebService.callWebService(APP.WEB_SERVER_URL, "GetIPTV_RJBInfo", properties, new MyWebService.WebServiceCallBack() {
                @Override
                public void callBack(SoapObject result) {
                    if(result != null){
                        Log.w("webservice2",result.toString());
                        try {
                            SoapObject detail = (SoapObject) result.getProperty("GetIPTV_RJBInfoResult");
                            SoapObject diffgram = (SoapObject) detail.getProperty("diffgram");
                            SoapObject NewDataSet = (SoapObject) diffgram.getProperty("NewDataSet");
                            SoapObject ds = (SoapObject) NewDataSet.getProperty("ds");

                            int nbbh = Integer.parseInt(ds.getProperty("nbbh").toString());//版本号
                            String strxzdz = ds.getProperty("strxzdz").toString();//下载地址
                            String strgxgsy = ds.getProperty("strgxgsy").toString();//更新提示
                            Log.w("webservice2", "nbbh:" + nbbh + "strxzdz:" + strxzdz);

                            //保存到sp
                            config_editor.putInt("nbbh",nbbh);
                            config_editor.putString("strxzdz",strxzdz);
                            config_editor.putString("strgxgsy",strgxgsy);
                            config_editor.commit();
                            //保存到全局
                            APP.app_code = nbbh;
                            APP.app_address = strxzdz;
                            APP.app_update = strgxgsy;
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else{
                        Log.w("webservice2","获取数据失败");
                    }

                }
            });


            /**
             * 获取酒店信息
             */
            properties = new HashMap<>();
            properties.put("strMd_Number",config.getString("Md_Number","-1"));
            MyWebService.callWebService(APP.WEB_SERVER_URL, "getInfobyMd", properties, new MyWebService.WebServiceCallBack() {
                @Override
                public void callBack(SoapObject result) {
                    if(result != null){
                        try {
                            Log.w("webservice3",result.toString());
                            SoapObject detail = (SoapObject) result.getProperty("getInfobyMdResult");
                            SoapObject diffgram = (SoapObject) detail.getProperty("diffgram");
                            SoapObject NewDataSet = (SoapObject) diffgram.getProperty("NewDataSet");
                            SoapObject ds = (SoapObject) NewDataSet.getProperty("ds");

                            String strqdgg = (String) ds.getProperty("strqdgg").toString();//启动广告地址
                            String strbjtp = (String) ds.getProperty("strbjtp").toString();//背景图片地址
                            String strbjyy = (String) ds.getProperty("strbjyy").toString();//背景音乐地址
                            String strggy = (String) ds.getProperty("strggy").toString();//欢迎语
                            String stryymc = (String) ds.getProperty("stryymc").toString();//点播应用名字
                            String stryyxzdz = (String) ds.getProperty("stryyxzdz").toString();//点播应用下载地址
                            String strcity = (String) ds.getProperty("strcity").toString();//城市名称
                            String strjdjstp = (String) ds.getProperty("strjdjstp").toString();//酒店介绍图片
                            String strdb = (String) ds.getProperty("strdb").toString();//是否有本地点播
                            String strdbfwqdz = (String) ds.getProperty("strdbfwqdz").toString();//本地点播地址

                           //保存到全局
                            APP.qdgg_address = strqdgg;
                            APP.bgpicture_address = strbjtp;
                            APP.jdinfo_musicaddress = strbjyy;
                            APP.welocome = strggy;
                            APP.db_name = stryymc;
                            APP.db_address = stryyxzdz;
                            APP.city_name = strcity;
                            APP.jdinfo_bgpicture = strjdjstp;
                            //保存信息到sp
                            config_editor.putString("strqdgg",strqdgg);
                            config_editor.putString("strbjtp",strbjtp);
                            config_editor.putString("strbjyy",strbjyy);
                            config_editor.putString("strggy",strggy);
                            config_editor.putString("stryymc",stryymc);
                            config_editor.putString("stryyxzdz",stryyxzdz);
                            config_editor.putString("strcity",strcity);
                            config_editor.putString("strjdjstp",strjdjstp);
                            config_editor.putString("strdb",strdb);
                            config_editor.putString("strdbfwqdz",strdbfwqdz);
                            config_editor.commit();

                            try {
                                //下载背景图片
                                new DownloadPicture().download("/bgpicture.png",strbjtp);
                                //下载视频
                                new DownloadPicture().downloadvideo(strqdgg);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }else{
                        Log.w("webservice3","获取数据失败");
                    }
                }
            });

            /**
             * 获取酒店授权信息
             */
            properties = new HashMap<>();
            properties.put("strMd_Number",config.getString("Md_Number","-1"));
            MyWebService.callWebService(APP.WEB_SERVER_URL, "getJdjssjInfobyMd", properties, new MyWebService.WebServiceCallBack() {
                @Override
                public void callBack(SoapObject result) {
                    if(result != null){
                        try {
                            Log.w("webservice4",result.toString());
                            SoapObject detail = (SoapObject) result.getProperty("getJdjssjInfobyMdResult");
                            SoapObject diffgram = (SoapObject) detail.getProperty("diffgram");
                            SoapObject NewDataSet = (SoapObject) diffgram.getProperty("NewDataSet");
                            SoapObject ds = (SoapObject) NewDataSet.getProperty("ds");
                            String Md_Etime = (String) ds.getProperty("Md_Etime").toString();//授权时间

                            APP.empower = Md_Etime;
                            //保存信息到sp
                            config_editor.putString("Md_Etime",Md_Etime);
                            config_editor.commit();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else{
                        Log.w("webservice4","获取数据失败");
                    }
                }
            });


            /**
             * 获取电视直播流地址
             */
            properties= new HashMap<String, String>();
            properties.put("strMd_Number",config.getString("Md_Number","-1"));//门店编号
            MyWebService.callWebService(APP.WEB_SERVER_URL, "getZbListbyMd", properties, new MyWebService.WebServiceCallBack() {
                @Override
                public void callBack(SoapObject result) {
                    if(result != null){
                        try {
                            Log.w("webservice5",result.toString());
                            SoapObject detail = (SoapObject) result.getProperty("getZbListbyMdResult");
                            SoapObject diffgram = (SoapObject) detail.getProperty("diffgram");
                            SoapObject NewDataSet = (SoapObject) diffgram.getProperty("NewDataSet");
                            for (int i=0;i<NewDataSet.getPropertyCount();i++){
                                SoapObject ds = (SoapObject) NewDataSet.getProperty(i);
                                int strxh = Integer.parseInt(ds.getProperty("strxh").toString());//节目序号
                                String strmc =  ds.getProperty("strmc").toString();//节目名称
                                String strdz =  ds.getProperty("strdz").toString();//节目地址
                                IPAddress ipAddress = new IPAddress();
                                ipAddress.setNum(strxh);
                                ipAddress.setIpaddress(strdz);
                                iplist.add(ipAddress);
                                Log.w("the result5","节目序号"+strxh+"节目名称"+strmc+"节目地址"+strdz);
                            }


                            //保存服务器端信息
                            Gson gson = new Gson();
                            String ipaddress = gson.toJson(iplist);
                            config_editor.putString("ipjson",ipaddress);
                            config_editor.commit();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else{
                        Log.w("webservice5","获取数据失败");
                    }
                }
            });
        }else{
            //Toast.makeText(getApplicationContext(),"没有网络",Toast.LENGTH_SHORT).show();
            //保存配置文件信息到本地
            config_editor.putString("Md_Number", APPConfig.Md_Number);
            config_editor.putString("strwifimm", APPConfig.strwifimm);
            config_editor.putString("strFjbh", APPConfig.strFjbh);
            config_editor.putString("strggy", APPConfig.strggy);
            config_editor.putString("strdb", APPConfig.strdb);
            config_editor.putString("strdbfwqdz", APPConfig.strdbfwqdz);
            /*Gson gson = new Gson();
            iplist = APPConfig.loadaddress();
            String ipaddress = gson.toJson(iplist);
            config_editor.putString("ipjson",ipaddress);*/
            config_editor.commit();


            //读取本地sp数据
            APP.md_num = config.getString("Md_Number","");
            APP.kf_num = config.getString("strFjbh","");
            APP.wifi_info = config.getString("strwifimm","");
            APP.kk_address = config.getString("strkkdz","");
            APP.app_code = config.getInt("nbbh",0);
            APP.app_address = config.getString("strxzdz","");
            APP.app_update = config.getString("strgxgsy","");
            APP.qdgg_address = config.getString("strqdgg","");
            APP.bgpicture_address = config.getString("strbjtp","");
            APP.jdinfo_musicaddress = config.getString("strbjyy","");
            APP.welocome = config.getString("strggy","");
            APP.db_name = config.getString("stryymc","");
            APP.db_address = config.getString("stryyxzdz","");
            APP.city_name = config.getString("strcity","");
            APP.jdinfo_bgpicture = config.getString("strjdjstp","");
            APP.empower = config.getString("Md_Etime","");
            //取出保存在本地的ipaddress
                String json = config.getString("ipjson", null);
                if (json != null)
                {
                    Type type = new TypeToken<List<IPAddress>>(){}.getType();
                    iplist = new Gson().fromJson(json, type);
                }
        }
    }
}
