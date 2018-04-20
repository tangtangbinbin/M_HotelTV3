package com.czsm.toms.m_hoteltv;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.czsm.toms.m_hoteltv.Global.MyApplication;
import com.czsm.toms.m_hoteltv.bean.IPAddress;
import com.czsm.toms.m_hoteltv.config.APPConfig;
import com.czsm.toms.m_hoteltv.utils.HandlerUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import static com.czsm.toms.m_hoteltv.Global.MyApplication.config;

public class TVZhiBo extends Activity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, View.OnTouchListener,HandlerUtils.OnReceiveMessageListener{

    /**
     * View播放
     */
    private VideoView videoView;

    /**
     * 加载预览进度条
     */
    private ProgressBar progressBar;

    /**
     * 设置view播放控制条
     */
    private MediaController mediaController;

    /**
     * 标记当视频暂停时播放位置
     */
    private int intPositionWhenPause=-1;

    /**
     * 设置窗口模式下的videoview的宽度
     */
    private int videoWidth;

    /**
     * 设置窗口模式下videoview的高度
     */
    private int videoHeight;
    int index = 0;
    public TextView channel;
    private List<IPAddress> iplist ;
    HandlerUtils.HandlerHolder handlerHolder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tvzhi_bo);
        channel = findViewById(R.id.zhibo_channel1);
        handlerHolder = new HandlerUtils.HandlerHolder(this);
        //读取sp的ipaddress
        String json = config.getString("ipjson", null);
        if (json != null)
        {
            Type type = new TypeToken<List<IPAddress>>(){}.getType();
            iplist = new Gson().fromJson(json, type);
            Log.w("iplist from ","local");
        }else {
            iplist = APPConfig.loadaddress();
            Log.w("iplist from ","config");
        }
        Log.w("the iplist size",iplist.size()+"");
        initVideoView();

        //显示台数
        handlerHolder.sendEmptyMessage(1);

    }

    /**
     *初始化videoview播放
     */
    public void initVideoView(){
        //初始化进度条
        progressBar= (ProgressBar) findViewById(R.id.progressBar);
        //初始化VideoView
        videoView= (VideoView) findViewById(R.id.videoView);
        //初始化videoview控制条
        mediaController=new MediaController(this);
        //设置videoview的控制条
        //videoView.setMediaController(mediaController);
        //设置显示控制条
        //mediaController.show(0);
        //设置播放完成以后监听
        videoView.setOnCompletionListener(this);
        //设置发生错误监听，如果不设置videoview会向用户提示发生错误
        videoView.setOnErrorListener(this);
        //设置在视频文件在加载完毕以后的回调函数
        videoView.setOnPreparedListener(this);
        //设置videoView的点击监听
        videoView.setOnTouchListener(this);
        if (iplist!=null){
            Uri uri=Uri.parse(iplist.get(0).getIpaddress());
            videoView.setVideoURI(uri);
            //设置为全屏模式播放
            setVideoViewLayoutParams(1);
        }

    }
    @Override
    protected void onStart() {
        super.onStart();
        //启动视频播放
        videoView.start();
        //设置获取焦点
        videoView.setFocusable(true);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.w("keycode",keyCode+"");
        switch (keyCode){
            case 166://遥控器节目上键
            case KeyEvent.KEYCODE_DPAD_UP:
                index++;
                if (index>=iplist.size()){
                    index = 0;
                }
                Log.w("the ip address",iplist.get(index).getIpaddress());
                //显示台数
                handlerHolder.sendEmptyMessage(1);
                if(videoView.isPlaying()){
                    videoView.stopPlayback();
                    videoView.setVideoURI(Uri.parse(iplist.get(index).getIpaddress()));
                    videoView.start();
                }else {
                    videoView.setVideoURI(Uri.parse(iplist.get(index).getIpaddress()));
                    videoView.start();
                }
                break;
            case 167://遥控器节目下键
            case KeyEvent.KEYCODE_DPAD_DOWN:
                index--;
                if (index<0){
                    index = iplist.size()-1;
                }
                Log.w("the ip address",iplist.get(index).getIpaddress());
                //显示台数
                handlerHolder.sendEmptyMessage(1);
                if(videoView.isPlaying()){
                    videoView.stopPlayback();
                    videoView.setVideoURI(Uri.parse(iplist.get(index).getIpaddress()));
                    videoView.start();
                }else {
                    videoView.setVideoURI(Uri.parse(iplist.get(index).getIpaddress()));
                    videoView.start();
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 设置videiview的全屏和窗口模式
     * @param paramsType 标识 1为全屏模式 2为窗口模式
     */
    public void setVideoViewLayoutParams(int paramsType){

        if(1==paramsType) {
            RelativeLayout.LayoutParams LayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            videoView.setLayoutParams(LayoutParams);
        }else{
            //动态获取宽高
            DisplayMetrics DisplayMetrics=new DisplayMetrics();
            this.getWindowManager().getDefaultDisplay().getMetrics(DisplayMetrics);
            videoHeight=DisplayMetrics.heightPixels-50;
            videoWidth=DisplayMetrics.widthPixels-50;
            RelativeLayout.LayoutParams LayoutParams = new RelativeLayout.LayoutParams(videoWidth,videoHeight);
            LayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            videoView.setLayoutParams(LayoutParams);
        }

    }
    /**
     * 视频播放完成以后调用的回调函数
     */
    @Override
    public void onCompletion(MediaPlayer mp) {

    }
    /**
     * 视频播放发生错误时调用的回调函数
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what){
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.e("text","发生未知错误");

                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.e("text","媒体服务器死机");
                break;
            default:
                Log.e("text","onError+"+what);
                break;
        }
        switch (extra){
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
                Log.e("text","onError+"+extra);
                break;
        }

       /* if(videoView.isPlaying()){
            videoView.stopPlayback();
            videoView.setVideoURI(Uri.parse(tvip[index]));
            videoView.start();
        }else {
            videoView.setVideoURI(Uri.parse(tvip[index]));
            videoView.start();
        }*/
        //如果未指定回调函数， 或回调函数返回假，VideoView 会通知用户发生了错误。
        return true;
    }

    /**
     * 视频文件加载文成后调用的回调函数
     * @param mp
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        //如果文件加载成功,隐藏加载进度条
        progressBar.setVisibility(View.GONE);

    }

    /**
     * 对videoView的触摸监听
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }



    /**
     * 页面暂停效果处理
     */
    @Override
    protected  void onPause() {
        super.onPause();
        //如果当前页面暂定则保存当前播放位置，并暂停
        intPositionWhenPause=videoView.getCurrentPosition();
        //停止回放视频文件
        videoView.stopPlayback();
    }

    /**
     * 页面从暂停中恢复
     */
    @Override
    protected void onResume() {
        super.onResume();
        //跳转到暂停时保存的位置
        if(intPositionWhenPause>=0){
            videoView.seekTo(intPositionWhenPause);
            //初始播放位置
            intPositionWhenPause=-1;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null!=videoView){
            videoView=null;
        }
    }

    @Override
    public void handlerMessage(Message msg) {
        if (msg.what==1){
            //channel.setText(iplist.get(index).getNum());
            channel.setText(index+1+"");
        }
    }
}
