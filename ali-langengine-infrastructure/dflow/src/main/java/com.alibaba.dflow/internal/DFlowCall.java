package com.alibaba.dflow.internal;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.InitEntry;
import com.alibaba.dflow.InitEntry.Entry;
import com.alibaba.dflow.InvalidCallException;
import com.alibaba.dflow.PipeLineInfo;
import com.alibaba.dflow.RetryException;
import com.alibaba.dflow.UserException;
import com.alibaba.dflow.func.ClosureEnabledConsumer;

import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DFlowCall extends DFlow<String> implements BiFunction<String,String, Boolean> {
    Logger logger = LoggerFactory.getLogger(DFlowCall.class);
    protected String callType;
    private ClosureEnabledConsumer onInit;
    private Entry myEntry;

    public DFlowCall(String callType, Consumer<ContextStack> onInit) throws DFlowConstructionException {
        super(String.class);
        if (onInit != null) {
            this.onInit = new ClosureEnabledConsumer(onInit, this, "");
        }
        this.callType = callType;
        //if(InitEntry.checkCallTypeUsedOnlyOnePosition(callType,getCallingPosition()) ){
        //    InitEntry.setCallback(callType,this);
        //}else{
        //    throw new DFlowConstructionException("Don't use same callType in different place:["+getCallingPosition()+
        //        "] And ["
        //        +InitEntry.getLastPosition(callType)+"]");
        //}
    }

    public DFlowCall(String callType) throws DFlowConstructionException {
        this(callType, null);
    }

    @Override
    public Boolean apply(String stringObjectMap, String traceId) throws Exception {
        //try {
            //如是第一个请求，入口先准备栈重新开始
            if (getStorage().getContext(traceId) == null) {
                call(traceId);
                onReturn(getOrCreateCurrent(traceId), stringObjectMap);
            } else {//如果不是第一个请求，需轮到自己
                ContextStack contextStack = getStorage().getContext(traceId);
                if (!getIDName().equals(contextStack.getName())) {
                    throw new InvalidCallException();
                }

                if (contextStack.isLocal() && !StringUtils.equals(InternalHelper.getIp(), contextStack.getIP())) {
                    InitEntry.transferCall(myEntry.toInitEntryID(), stringObjectMap, traceId);
                    logger.warn(traceId + " Local request Resend to:" + contextStack.getIP());
                    return false;
                }

                onReturn(contextStack, stringObjectMap);
            }

        //} catch (Exception e) {
        //    logger.error("DFlowCall call failed" + traceId, e);
        //
        //    return false;
        //}
        return true;
    }

    @Override
    public boolean callAfterStackBuild(ContextStack context) throws RetryException, UserException {
        if (this.onInit != null) {
            try {
                onInit.accept(context);
            } catch (Exception e) {
                error(context, e);
                return false;
            }
        }
        return false;
    }

    @Override
    protected String functionalUniqName(String constructingPosStr) {
        return "call:" + constructingPosStr;
    }

    @Override
    public PipeLineInfo init(DFlow parentSetter, String pipelineName, int count, String nextFlowName) throws DFlowConstructionException {
        PipeLineInfo info = super.init(parentSetter,pipelineName,count,nextFlowName);
        myEntry = new Entry();
        myEntry.setCallType(callType);
        myEntry.setDflowname(getIDName());
        info.putEntry(callType,myEntry);
        InitEntry.setCallback(myEntry.toInitEntryID(),this);
        return info;
    }
}