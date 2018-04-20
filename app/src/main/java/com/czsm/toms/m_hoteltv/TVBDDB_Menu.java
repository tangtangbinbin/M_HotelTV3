package com.czsm.toms.m_hoteltv;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.czsm.toms.m_hoteltv.Global.APP;
import com.czsm.toms.m_hoteltv.Global.MyApplication;
import com.czsm.toms.m_hoteltv.adapter.CategoryAdapter;
import com.czsm.toms.m_hoteltv.config.APPConfig;
import com.czsm.toms.m_hoteltv.webservice.MyWebService;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.czsm.toms.m_hoteltv.Global.MyApplication.config_editor;

/**
 * Created by Administrator on 2018/4/20.
 */

public class TVBDDB_Menu extends AppCompatActivity {

    RecyclerView category,menu;
    private List<String> list = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bddb_menu);
        category = findViewById(R.id.re_category);
        menu = findViewById(R.id.re_menu);

        for (int i=0;i<10;i++){
            list.add(i+"");
        }
        category.setLayoutManager(new LinearLayoutManager(TVBDDB_Menu.this,LinearLayoutManager.VERTICAL,false));
        CategoryAdapter adapter = new CategoryAdapter(TVBDDB_Menu.this,list);
        category.setAdapter(adapter);
        getServiceData();
    }

    private void getServiceData(){
        HashMap properties = new HashMap<>();
        String strMd_Number = MyApplication.config.getString("Md_Number","-1");
        if (strMd_Number.equals("-1")){
            strMd_Number = APPConfig.Md_Number;
        }
        properties.put("strMd_Number",strMd_Number);
        MyWebService.callWebService(APP.WEB_SERVER_URL, "getZbListbyMd", properties, new MyWebService.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if(result != null){
                    try {
                        Log.w("webservice6",result.toString());
                        /*SoapObject detail = (SoapObject) result.getProperty("getZbListbyMdResult");
                        SoapObject diffgram = (SoapObject) detail.getProperty("diffgram");
                        SoapObject NewDataSet = (SoapObject) diffgram.getProperty("NewDataSet");
                        SoapObject ds = (SoapObject) NewDataSet.getProperty("ds");
                        String Md_Etime = (String) ds.getProperty("Md_Etime").toString();//授权时间

                        APP.empower = Md_Etime;
                        //保存信息到sp
                        config_editor.putString("Md_Etime",Md_Etime);
                        config_editor.commit();*/
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    Log.w("webservice6","获取数据失败");
                }
            }
        });

    }
}
