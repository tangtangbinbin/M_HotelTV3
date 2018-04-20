package com.czsm.toms.m_hoteltv;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.czsm.toms.m_hoteltv.Global.MyApplication;
import com.czsm.toms.m_hoteltv.config.APPConfig;
import com.czsm.toms.m_hoteltv.utils.HandlerUtils;
import com.czsm.toms.m_hoteltv.utils.PureNetUtil;
import com.czsm.toms.m_hoteltv.utils.getParameterUtils;
import com.czsm.toms.m_hoteltv.utils.RemoveChinese;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.czsm.toms.m_hoteltv.Global.MyApplication.config;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,View.OnFocusChangeListener,HandlerUtils.OnReceiveMessageListener{

    private Button btn_zhibo,btn_dianbo,btn_info,btn_config,btn_bddb;
    private TextView title,info;
    private ProgressDialog progressDialog;
    final  static String APKPATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/M_HotelTV1/";
    String url ="";
    private HandlerUtils.HandlerHolder handlerHolder;
    LinearLayout root;
    String db_canplay ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        handlerHolder = new HandlerUtils.HandlerHolder(this);
        root = findViewById(R.id.lin_root);
        title = findViewById(R.id.hotel_title);
        btn_zhibo = findViewById(R.id.btn_zhibo);
        btn_zhibo.setBackgroundColor(Color.BLUE);
        btn_dianbo = findViewById(R.id.btn_dianbo);
        btn_bddb = findViewById(R.id.btn_dianbo2);
        btn_info = findViewById(R.id.btn_info);
        info = findViewById(R.id.main_info);
        btn_config = findViewById(R.id.btn_config);

        db_canplay = config.getString("strdb","");
        if (db_canplay.length()==0){
            db_canplay = APPConfig.strdb;
        }
        Log.w("the db_canplay",db_canplay);
        if (db_canplay.equals("1")){
            Log.w("the db_canplay","true");
            btn_bddb.setVisibility(View.VISIBLE);
        }
        //点击监听
        btn_zhibo.setOnClickListener(this);
        btn_dianbo.setOnClickListener(this);
        btn_info.setOnClickListener(this);
        btn_config.setOnClickListener(this);
        btn_bddb.setOnClickListener(this);

        //焦点改变监听
        btn_zhibo.setOnFocusChangeListener(this);
        btn_dianbo.setOnFocusChangeListener(this);
        btn_info.setOnFocusChangeListener(this);
        btn_config.setOnFocusChangeListener(this);
        btn_bddb.setOnFocusChangeListener(this);
        String strggy = config.getString("strggy","");
        if (strggy.length()==0){
            strggy = APPConfig.strggy;
        }
        String infostr = "尊敬的宾客：\n        "+strggy;
        info.setText(infostr.replace("\\n","\n"));
        loadMessage();//加载数据（wifi，日期，天气等）
        checkUpdate();

        //15秒进入直播
        handlerHolder.sendEmptyMessageDelayed(4,15000);
        //30秒后加载数据
        handlerHolder.sendEmptyMessageDelayed(5,30000);

    }

    private boolean checkEmpower(){
        String time = config.getString("Md_Etime","");
        if (time.length()<=0){
            return true;
        }
        time = time.substring(0,time.lastIndexOf("+"));
        Log.w("the empower",time);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date endtime = df.parse(time);
            Date nowtime = df.parse(df.format(new Date()));
            if (endtime.after(nowtime))
            Log.w("the time compare", String.valueOf(endtime.after(nowtime)));
            return endtime.after(nowtime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }
    private void checkUpdate(){
        int nowcode = getParameterUtils.getVersionCode(this);
        if (config.getInt("nbbh",0)==0){
            return;
        }
        int newcode = config.getInt("nbbh",0);
        if (newcode!=nowcode){
            handlerHolder.sendEmptyMessage(2);
        }
    }

    private void loadMessage() {

        //加载本地背景图片
       /* Drawable drawable = Drawable.createFromPath(APKPATH+"/bgpicture.png");
        root.setBackground(drawable);
        File file = new File(APKPATH+"/bgpicture.png");
        if (!file.exists()){
            root.setBackgroundResource(R.mipmap.iu4);
        }*/


        //显示日期
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EE yyyy-MM-dd");//星期 年月日
        title.setText(simpleDateFormat.format(new Date()));

        //显示wifi信息
        String strwifimm = config.getString("strwifimm","");
        if (strwifimm.length()==0){
            strwifimm = APPConfig.strwifimm;
        }
        title.append("   "+strwifimm);

        //获取天气数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String cityName = URLEncoder.encode(config.getString("strcity",""),"UTF-8");
                    String url = "https://www.sojson.com/open/api/weather/json.shtml?city="+cityName;
                    String  result =  PureNetUtil.getServiceInfo(url);
                    Message msg = new Message();
                    msg.what=3;
                    msg.obj = result;
                    if (msg.obj!=null){
                        handlerHolder.sendMessage(msg);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_zhibo:
                if (!checkEmpower()){
                    Log.w("the empower","授权过期");
                    break;
                }
                Intent toZhiBo = new Intent();
                toZhiBo.setClass(MainActivity.this,TVZhiBo.class);
                startActivity(toZhiBo);
                break;
            case R.id.btn_dianbo:
                if (!checkEmpower()) {
                    Log.w("the empower", "授权过期");
                    break;
                }
                //调用腾讯播放器
                doStartApplicationWithPackageName("com.ktcp.video");
                break;
            case R.id.btn_info:
                Intent toInfo = new Intent();
                toInfo.setClass(MainActivity.this,HotelInfo.class);
                startActivity(toInfo);
                break;
            case R.id.btn_config:
                Intent toConfig = new Intent();
                toConfig.setClass(MainActivity.this,ConfigInfo.class);
                startActivity(toConfig);
                break;
            case R.id.btn_dianbo2:
                Intent tobddb = new Intent();
                tobddb.setClass(MainActivity.this,TVBDDB_Menu.class);
                startActivity(tobddb);
        }
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        //获得焦点为蓝色，失去焦点为灰色
        if(b){
            view.setBackgroundColor(Color.BLUE);
        }else {
            view.setBackgroundColor(Color.parseColor("#3c3f41"));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        handlerHolder.removeMessages(4);
        if (keyCode == KeyEvent.KEYCODE_BACK){
            return true;//主界面取消返回键功能
        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    public void handlerMessage(Message msg) {
        switch (msg.what){
            case 1:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("应用未安装，是否下载？");
                builder.setTitle("提示");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        //云视听极光url
                        //url = "http://sfapp01.oss-cn-hangzhou.aliyuncs.com/apk/com.ktcp.video.e73ffdcf90e7e9d9a9d554a60e84ac11.apk";
                        String urlstr = config.getString("stryyxzdz","");
                        if (urlstr.length()==0){
                            urlstr = APPConfig.stryyxzdz;
                        }
                        url = urlstr;
                        showDowload();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.setCancelable(false);
                builder.create().show();
                break;
            case 2:
                handlerHolder.removeMessages(4);
                AlertDialog.Builder builder_update = new AlertDialog.Builder(MainActivity.this);
                builder_update.setMessage(config.getString("strgxgsy",""));
                builder_update.setTitle("提示");
                builder_update.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        //服务器端apk下载链接
                        url = config.getString("strxzdz","");
                        showDowload();
                    }
                });
                builder_update.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder_update.setCancelable(false);
                builder_update.create().show();
                break;
            case 3:
                //显示天气
                Log.w("msg3:",msg.obj.toString());
                try {
                    JSONObject object = new JSONObject(msg.obj.toString());
                    int status = object.getInt("status");
                    if (status==200){
                        JSONObject data = object.getJSONObject("data");
                        JSONArray forecast = data.getJSONArray("forecast");
                        JSONObject today = (JSONObject) forecast.get(0);
                        String high = today.getString("high");
                        String low = today.getString("low");
                        String type = today.getString("type");
                        Log.w("the end ", RemoveChinese.noChinese(high)+"~"+RemoveChinese.noChinese(low)+" "+type);
                        title.append("   "+RemoveChinese.noChinese(low)+" - "+RemoveChinese.noChinese(high)+" "+type);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case 4:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,TVZhiBo.class);
                startActivity(intent);
                break;
            case 5:
                if (PureNetUtil.isConnect(MainActivity.this)){
                    new MyApplication().initdata();
                    checkUpdate();
                }
                break;

        }
    }


    private void showDowload(){
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("下载中。。。");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        DownLoadFile downLoadFile = new DownLoadFile();
        downLoadFile.execute(url);
    }

    private class DownLoadFile extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                int fileLenth = connection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream());
                File path = new File(APKPATH);
                if (!path.exists()){
                    path.mkdirs();
                }
                File apk = new File(path+"/temp.apk");
                if (!apk.exists()){
                    apk.createNewFile();
                }
                OutputStream output = new FileOutputStream(apk);

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count=input.read(data))!=-1){
                    total+=count;
                    publishProgress((int)total*100/fileLenth);
                    output.write(data,0,count);
                }
                output.flush();
                output.close();
                input.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try{
                progressDialog.setCancelable(false);
                progressDialog.show();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            Intent i = new Intent(Intent.ACTION_VIEW);
            Uri uri = null;
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
                i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                uri = FileProvider.getUriForFile(getApplicationContext(),"com.czsm.toms.m_hoteltv.fileprovider",new File(APKPATH+"/temp.apk"));
            }else {
                uri = Uri.fromFile(new File(APKPATH+"/temp.apk"));
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            i.setDataAndType(uri,"application/vnd.android.package-archive");
            startActivity(i);

        }
    }
    private void doStartApplicationWithPackageName(String packagename) {

            // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
            PackageInfo packageinfo = null;
            try {
                    packageinfo = getPackageManager().getPackageInfo(packagename, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            if (packageinfo == null) {//未识别到应用，则下载
                    Message msg = new Message();
                    msg.what = 1;
                    handlerHolder.sendMessage(msg);
                    return;
                }

           // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(packageinfo.packageName);

           // 通过getPackageManager()的queryIntentActivities方法遍历
            List<ResolveInfo> resolveinfoList = getPackageManager()
                    .queryIntentActivities(resolveIntent, 0);

            ResolveInfo resolveinfo = resolveinfoList.iterator().next();
            if (resolveinfo != null) {
                    // packagename = 参数packname
                   String packageName = resolveinfo.activityInfo.packageName;
                    // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
                    String className = resolveinfo.activityInfo.name;
                    // LAUNCHER Intent
                   Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);

                   // 设置ComponentName参数1:packagename参数2:MainActivity路径
                    ComponentName cn = new ComponentName(packageName, className);

                    intent.setComponent(cn);
                    startActivity(intent);
                }else{
                Log.w("activityname","activitynotfind");
            }
    }

}
