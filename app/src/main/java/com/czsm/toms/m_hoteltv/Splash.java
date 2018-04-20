package com.czsm.toms.m_hoteltv;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.VideoView;

import com.czsm.toms.m_hoteltv.Global.APP;
import com.czsm.toms.m_hoteltv.Global.MyApplication;
import com.czsm.toms.m_hoteltv.bean.IPAddress;
import com.czsm.toms.m_hoteltv.utils.DownloadPicture;
import com.czsm.toms.m_hoteltv.utils.MacUtils;
import com.czsm.toms.m_hoteltv.utils.PureNetUtil;
import com.czsm.toms.m_hoteltv.utils.getParameterUtils;
import com.czsm.toms.m_hoteltv.webservice.MyWebService;
import com.google.gson.Gson;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.czsm.toms.m_hoteltv.Global.MyApplication.config;
import static com.czsm.toms.m_hoteltv.Global.MyApplication.config_editor;

public class Splash extends AppCompatActivity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener{

    private VideoView videoView;
    String ipaddress = "";
    String TAG = "Splash.class";
    @Override
    protected void onStart() {
        super.onStart();
        videoView.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        videoView = findViewById(R.id.splash_videoView);
        videoView.setOnCompletionListener(this);
        //设置发生错误监听，如果不设置videoview会向用户提示发生错误
        videoView.setOnErrorListener(this);
        //设置在视频文件在加载完毕以后的回调函数
        videoView.setOnPreparedListener(this);
        ipaddress = config.getString("local_strqdgg","");

        if (ipaddress.length()>1){//如果有地址就播放，没有就显示mainactivity
            videoView.setVideoURI(Uri.parse(ipaddress));
            videoView.start();
            Log.w(TAG,ipaddress);
        }else {
            videoView.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.iu3));
            videoView.start();
        }


    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Intent intent = new Intent();
        intent.setClass(Splash.this,MainActivity.class);
        startActivity(intent);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //播放完成就去main
        Intent intent = new Intent();
        intent.setClass(Splash.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        //出错就跳转到main
        Intent intent = new Intent();
        intent.setClass(Splash.this,MainActivity.class);
        startActivity(intent);
        finish();
        switch (i){
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.e("text","发生未知错误");

                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.e("text","媒体服务器死机");
                break;
            default:
                Log.e("text","onError+"+i);
                break;
        }
        switch (i1){
            case MediaPlayer.MEDIA_ERROR_IO:
                //io读写错误
                Log.e("text","文件或网络相关的IO操作错误");
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                //文件格式不支持
                Log.e("text","比特流编码标准或文件不符合相关规范");
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                //一些操作需要太长时间来完成,通常超过3 - 5秒。
                Log.e("text","操作超时");
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                //比特流编码标准或文件符合相关规范,但媒体框架不支持该功能
                Log.e("text","比特流编码标准或文件符合相关规范,但媒体框架不支持该功能");
                break;
            default:
                Log.e("text","onError+"+i1);
                break;
        }
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
    //mediaPlayer.setLooping(true);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (videoView.isPlaying()){
            videoView.stopPlayback();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null!=videoView){
            videoView=null;
        }
    }
}
