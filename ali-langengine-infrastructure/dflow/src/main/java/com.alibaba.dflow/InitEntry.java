package com.alibaba.dflow;

import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.dflow.DFlow.CallerMessage;
import com.alibaba.dflow.InitEntry.RequestResender.Callback;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.DFlowConstructionException;
import com.alibaba.fastjson.JSON;

import io.reactivex.functions.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitEntry {
    static Logger logger = LoggerFactory.getLogger(InitEntry.class);

    public static class Entry{
        private String callType;
        private String dflowname;
        private String pipelineName;
        public Entry(){}
        public Entry(String callType){
            this.callType = callType;
        }

        public String getCallType() {
            return callType;
        }

        public void setCallType(String callType) {
            this.callType = callType;
        }

        public String getDflowname() {
            return dflowname;
        }

        public void setDflowname(String dflowname) {
            this.dflowname = dflowname;
        }

        public String toInitEntryID(){return callType+dflowname;}

        public String getPipelineName() {
            return pipelineName;
        }

        public void setPipelineName(String pipelineName) {
            this.pipelineName = pipelineName;
        }

        public String call(String params) throws Exception {
            return DFlow.call(this,params);
        }

        public void call(String params, String taskId) throws Exception {
            DFlow.call(this,params,taskId);
        }
    }

    private static ConcurrentHashMap<String, BiFunction<String, String, Boolean>> callbacks = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> callTypeInDifferentPositions = new ConcurrentHashMap<>();

    public static String getLastPosition(String callType) {
        return callTypeInDifferentPositions.get(callType);
    }

    public static boolean checkCallTypeUsedOnlyOnePosition(String callType, String initPosition) {
        if (initPosition.equals(callTypeInDifferentPositions.get(callType))) {
            return true;
        }

        synchronized (InitEntry.class) {
            if (callTypeInDifferentPositions.contains(callType) &&
                !callTypeInDifferentPositions.get(callType).equals(initPosition)) {
                return false;
            } else {
                callTypeInDifferentPositions.put(callType, initPosition);
                return true;
            }
        }
    }

    private static String DFLOW_INNER_TRANSFER_CALLTYPE = "DFLOW_INNER_TRANSFER_CALLTYPE";

    public static boolean transferDflowInnerCall(CallerMessage message,String ip){
        return gRequestResender.resendCall(ip,DFLOW_INNER_TRANSFER_CALLTYPE, JSON.toJSONString(message),message.getTraceId());
    }
    public static void setDflowInnerTransferCall(BiFunction<String,String,Boolean> callback){
        callbacks.put(DFLOW_INNER_TRANSFER_CALLTYPE,callback);
    }

    public static void setCallback(String callType, BiFunction<String, String, Boolean> callback)
        throws DFlowConstructionException {
        if(DFLOW_INNER_TRANSFER_CALLTYPE.equals(callType)){
            throw new DFlowConstructionException("conflict with inner calltype");
        }
        callbacks.put(callType, callback);
    }
    public static Boolean call(String callType, String params, String id) throws RetryException,Exception {
        return call(callType,params,id,true);
    }
    public static Boolean call(String callType, String params, String id, boolean transfer) throws RetryException,Exception {
        try {
            if (callbacks.get(callType) == null) {

                if (transfer) {
                    transferCall(callType, params, id);
                    return true;
                } else {
                    logger.error("Calltype:" + callType + " not inited either @" + id);
                    return false;
                }
            }
            return callbacks.get(callType).apply(params, id);
        }catch (Throwable t){
            //转发也没有，可能目标ip的机器已经下线，如果要求local，去除local再拯救一下
            ContextStack c = DFlow.getStorage().getContext(id);
            if(c.isLocal()) {
                c.removeLocal();
                DFlow.getStorage().putContext(id, c);
            }else {
                DFlow.globalError(id, t);
            }
            logger.error("DFlow Init entry call failed:"+id);
            throw t;
        }
    }

    public static void transferCall(String callType, String params, String id) throws RetryException,Exception {
        ContextStack stack = DFlow.getStorage().getContext(id);

        if (gRequestResender == null) {
            throw new Exception("DFlow is not inited! InitEntry.setRequestResender earlier");
        } else {
            try {
                if (!gRequestResender.resendCall(stack.getIP(), callType, params, id)) {
                    throw new RetryException("resendFailed,need retry by message@" + id);
                }
            }catch (Exception e){
                logger.error("resendException"+id,e);
                throw new RetryException("resendFailed,need retry by message@" + id);
            }
        }
    }

    private static RequestResender gRequestResender;

    public static void setRequestResender(RequestResender requestResender) {
        gRequestResender = requestResender;
        requestResender.setCallback(new Callback() {
            @Override
            public boolean realCall(String callType, String params, String id) throws Exception {
                return call(callType, params, id, false);
            }
        });
    }

    public interface RequestResender {
        interface Callback {
            boolean realCall(String callType, String params, String id) throws Exception;
        }

        void setCallback(Callback callback);

        boolean resendCall(String ip, String callType, String params, String id);
    }
}