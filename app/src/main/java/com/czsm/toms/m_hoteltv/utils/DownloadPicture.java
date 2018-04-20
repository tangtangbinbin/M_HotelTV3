package com.czsm.toms.m_hoteltv.utils;

import android.os.Environment;
import android.util.Log;

import com.czsm.toms.m_hoteltv.Global.MyApplication;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Administrator on 2018/4/16.
 */

public class DownloadPicture {

    final  static String APKPATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/M_HotelTV1/";

    public void download(final String name, final String path){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(path);
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    int fileLenth = connection.getContentLength();
                    InputStream input = new BufferedInputStream(url.openStream());
                    File path = new File(APKPATH);
                    if (!path.exists()){
                        path.mkdirs();
                    }
                    File file = new File(path+name);
                    if (!file.exists()){
                        file.createNewFile();
                    }
                    OutputStream output = new FileOutputStream(file);

                    byte data[] = new byte[1024];
                    int count;
                    while ((count=input.read(data))!=-1){
                        output.write(data,0,count);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void downloadvideo( final String path){
        final String name= getName(path);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(path);
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    InputStream input = new BufferedInputStream(url.openStream());
                    File path = new File(APKPATH);
                    if (!path.exists()){
                        path.mkdirs();
                    }
                    File file = new File(path+"/"+name);
                    if (!file.exists()){
                        file.createNewFile();
                        Log.w("the new file local",file.getPath());
                    }else {
                        Log.w("the file exist","文件已经存在，不要下载");
                        MyApplication.config_editor.putString("local_strqdgg",APKPATH+name);
                        MyApplication.config_editor.commit();
                        return;
                    }
                    OutputStream output = new FileOutputStream(file);

                    byte data[] = new byte[1024];
                    int count;
                    while ((count=input.read(data))!=-1){
                        output.write(data,0,count);
                    }
                    output.flush();
                    output.close();
                    input.close();
                    MyApplication.config_editor.putString("local_strqdgg",APKPATH+name);
                    MyApplication.config_editor.commit();
                    Log.w("the local name1",APKPATH+name);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public String getName(String path) {
        String name = path.substring(path.lastIndexOf("upload/")+7,path.length());
        Log.w("the sub name",name);
        return name;
    }

}
