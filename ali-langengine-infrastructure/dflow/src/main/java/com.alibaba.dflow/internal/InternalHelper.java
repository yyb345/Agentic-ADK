package com.alibaba.dflow.internal;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.function.Supplier;

import com.alibaba.dflow.internal.ContextStack.ContextNode;

/**
 * 访问控制辅助
 */
public class InternalHelper {

    public static Supplier<String> InstanceIndicator ;

    public static void setStatus(ContextStack contextStack,String status){
        contextStack.setStatus(status);
    }

    public static void rebuildNewStack(ContextStack contextStack,String name,String debugname) {

        String lastStatus = contextStack.getStatus();
        String currentStatus = ContextStack.STATUS_BEGIN;
        if(lastStatus != null && lastStatus.startsWith(ContextStack.STATUS_RETRY)){
            contextStack.pop();
            currentStatus = (ContextStack.STATUS_RETRYING+":"+lastStatus);
        }

        Object lastResult = contextStack.getResult();
        //contextStack.pop();

        ContextNode node = new ContextNode();
        //lastResult as currentParam
        node.setParam(lastResult);
        node.setName(name);
        node.setDebugName(debugname);
        node.setStartTime(System.currentTimeMillis());
        contextStack.push(node);

        contextStack.setIP(getIp());

        contextStack.setStatus(currentStatus);
    }

    private static String ip = null;

    public static String getIp(){
        if(ip == null) {
            InetAddress ia = null;
            if(InstanceIndicator != null) {
                ip = InstanceIndicator.get();
                return ip;
            }
            try {
                ia = InetAddress.getLocalHost();

                String localip = ia.getHostAddress();
                ip = localip;
                return localip;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ip;
    }

    public static void setNameAndStatus(ContextStack contextStack,String name,String status) {
        contextStack.setName(name);
        contextStack.setStatus(status);
    }

    public static <T> void setResultAndStatus(ContextStack context, T t, String statusEnd) {
        context.setResult(t);
        if(!context.getStack().isEmpty()) {
            Long starttime = context.getStack().peek().getStartTime();
            Long costtime = System.currentTimeMillis() - starttime;
            context.getStack().peek().setCostTime(costtime);
        }
        context.setStatus(statusEnd);
    }

    public static <T> T getResult(ContextStack contextStack){
        return (T)contextStack.getResult();
    }

    public static HashMap<String,Object> getClosure(ContextStack contextStack,String key){
        return contextStack.getClosureData(key);
    }

    public static void putClosure(ContextStack contextStack,HashMap<String,Object> data,String key){
        contextStack.setClosureData(key,data);
    }

    public static String getStackIndex(ContextStack stack) {
        return ""+stack.getStack().size();
    }

    public static void setNextStep(ContextStack context, String nextStepId) {
        context.setNextStepId(nextStepId);
    }
}
