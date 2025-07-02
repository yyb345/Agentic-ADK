package com.alibaba.dflow.util;

import java.util.HashMap;

public class FunctionUtil {

    public static String getAnymouseFuncName(Object o){
        String fullname = o.getClass().getName();
        // anymouseFuncName may change over different machine
        int last = fullname.lastIndexOf("$Lambda$");
        if(last > 0){
            fullname = fullname.substring(0,last);
        }
        return fullname;
    }
}
