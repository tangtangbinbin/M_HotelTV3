package com.czsm.toms.m_hoteltv;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.czsm.toms.m_hoteltv.Global.APP;
import com.czsm.toms.m_hoteltv.Global.MyApplication;
import com.czsm.toms.m_hoteltv.config.APPConfig;
import com.czsm.toms.m_hoteltv.interfaces.IMultKeyTrigger;
import com.czsm.toms.m_hoteltv.interfacesimpl.MyMultKeyTrigger;
import com.czsm.toms.m_hoteltv.utils.MacUtils;
import com.czsm.toms.m_hoteltv.utils.getParameterUtils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import static com.czsm.toms.m_hoteltv.Global.MyApplication.config;


public class ConfigInfo extends AppCompatActivity {

    TextView ip, room, version, MAC,empower;
    LinearLayout lin_empower;
    List<Integer> list = Arrays.asList(APP.MULT_KEY);
    private IMultKeyTrigger multKeyTrigger= new MyMultKeyTrigger(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_info);
        ip = findViewById(R.id.ip_address);
        room = findViewById(R.id.room_id);
        lin_empower = findViewById(R.id.lin_empower);
        version = findViewById(R.id.version);
        empower = findViewById(R.id.empower);
        MAC = findViewById(R.id.MAC_id);

        String strempower = config.getString("Md_Etime","");
        try {
            if (strempower.length()!=0){
                strempower = strempower.substring(0,strempower.lastIndexOf("T"));
                empower.setText(strempower);
            }
            String roomnum =  MyApplication.config.getString("strFjbh","");
            if (roomnum.length()==0){
                roomnum = APPConfig.strFjbh;
            }
            String md_num =  MyApplication.config.getString("Md_Number","");
            if (md_num.length()==0){
                md_num = APPConfig.Md_Number;
            }
            room.setText(md_num+" - "+roomnum);
            ip.setText(getHostIP());
            String mac = MacUtils.getMac(this).replace(":","");
            MAC.setText("  "+mac);
            Log.w("the mac",mac);
            version.setText(getParameterUtils.getVersionName(this));
        }catch (Exception e){
            e.printStackTrace();
        }
        if (!checkEmpower()){//过了授权日期就显示授权时间
            lin_empower.setVisibility(View.VISIBLE);
        }


    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.w("onkeydonw", "keyCode:" + keyCode);
        if(handlerMultKey(keyCode, event)){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean checkEmpower(){
        try {
            String time = config.getString("Md_Etime","");
            if (time.length()<=0){
                return true;
            }
            time = time.substring(0,time.lastIndexOf("+"));
            Log.w("the empower",time);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date endtime = df.parse(time);
            Date nowtime = df.parse(df.format(new Date()));
            if (endtime.after(nowtime))
            Log.w("the time compare", String.valueOf(endtime.after(nowtime)));
            return endtime.after(nowtime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean handlerMultKey(int keyCode, KeyEvent event) {
        boolean vaildKey = false;
        if (list.contains(keyCode) && multKeyTrigger.allowTrigger()) {
            // 是否是有效按键输入
            vaildKey = multKeyTrigger.checkKey(keyCode, event.getEventTime());
            // 是否触发组合键
            if (vaildKey && multKeyTrigger.checkMultKey()) {
                //执行触发
                multKeyTrigger.onTrigger();
                //触发完成后清除掉原先的输入
                multKeyTrigger.clearKeys();

                // 打开系统设置界面
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);

            }
        }
        return false;
    }
    public static String getHostIP() {
            String hostIp = null;
           try {
                    Enumeration nis = NetworkInterface.getNetworkInterfaces();
                    InetAddress ia = null;
                    while (nis.hasMoreElements()) {
                            NetworkInterface ni = (NetworkInterface) nis.nextElement();
                            Enumeration<InetAddress> ias = ni.getInetAddresses();
                            while (ias.hasMoreElements()) {
                                   ia = ias.nextElement();
                                    if (ia instanceof Inet6Address) {
                                            continue;// skip ipv6
                                       }
                                    String ip = ia.getHostAddress();
                                    if (!"127.0.0.1".equals(ip)) {
                                            hostIp = ia.getHostAddress();
                                            break;
                                        }
                                }
                        }
                } catch (SocketException e) {
                    Log.i("yao", "SocketException");
                    e.printStackTrace();
                }
            return hostIp;

        }


}
