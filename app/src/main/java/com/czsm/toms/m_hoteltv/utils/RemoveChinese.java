package com.czsm.toms.m_hoteltv.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2018/4/11.
 */

public class RemoveChinese {

    //去掉字符串中的中文
    public static String noChinese(String str){
        String reg = "[\u4e00-\u9fa5]";

        Pattern pat = Pattern.compile(reg);

        Matcher mat=pat.matcher(str);

        String repickStr = mat.replaceAll("");
        return repickStr;
    }
}
