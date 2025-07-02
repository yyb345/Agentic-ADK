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

public class DFlowDelay<T> extends DFlow<T> implements BiFunction<String, String, Boolean> {
    static Logger logger = LoggerFactory.getLogger(DFlowDelay.class);

    static DFlowDelayManager dFlowDelayManager;

    public static void setDFlowDelayManager(DFlowDelayManager delayManager) {
        dFlowDelayManager = delayManager;
    }

    long timeout;
    T data;

    public DFlowDelay(long timeout, T t) throws DFlowConstructionException {
        super("timeout"+timeout,t.getClass(),true);
        this.timeout = timeout;
        this.data = t;
        //兼容一下老版本
        if(DFlow.g_strictMode) {
            id( "timeout"+timeout);
        }
    }

    @Override
    public boolean callAfterStackBuild(ContextStack context) throws RetryException, UserException {
        if (dFlowDelayManager != null) {
            try {
                dFlowDelayManager.addTask(getIDName(),context.getId(), timeout);
            } catch (Exception e) {
                error(context,e);
                throw new RetryException("Add delay task failed");
            }
        }else{
            error(context, new UserException(new RuntimeException("dFlowDelayManager is not inited, no delay and run direct")));
            onReturn(context,data);
        }
        return false;
    }

    public static void resume(String idname, String traceId) throws Exception {
        InitEntry.call(idname, null, traceId);
    }

    private void resumeInner(String idname, String traceId) throws UserException {
        ContextStack ctx = DFlow.getStoreage().getContext(traceId);
        if (ctx == null || ctx.getStack().peek() == null) {
            throw (new RuntimeException("When resume delaydflow, traceId:" + traceId + " is not exist"));
        }
        if(!StringUtils.equals(ctx.getStack().peek().getName(),idname)){
            logger.warn("When resume delaydflow, may called duplicated, idname:" + idname + " is not equal to the idname:" + ctx.getStack().peek().getName());
        }
        onReturn(ctx,data);
    }

    @Override
    protected String functionalUniqName(String constructingPosStr) {
        return "timeout"+timeout;
    }
    public PipeLineInfo init(DFlow parentSetter, String pipelineName, int count, String nextFlowName) throws DFlowConstructionException {
        PipeLineInfo info = super.init(parentSetter,pipelineName,count,nextFlowName);
        InitEntry.setCallback(getIDName(),this);
        return info;
    }

    @Override
    public Boolean apply(String stringObjectMap, String traceId) throws Exception {
        resumeInner(getIDName(), traceId);
        return true;
    }

    public abstract static class DFlowDelayManager {
        abstract protected void addTask(String idname, String traceId, long timeout) throws Exception;
        protected void resume(String idname, String traceId) throws Exception {
            DFlowDelay.resume(idname, traceId);
        }
    }

    public interface ResumeCallback {
        void resume(String traceId, String idname);
    }

}