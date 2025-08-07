package com.alibaba.dflow.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.InitEntry;
import com.alibaba.dflow.InitEntry.Entry;
import com.alibaba.dflow.InvalidCallException;
import com.alibaba.dflow.PipeLineInfo;
import com.alibaba.dflow.RetryException;
import com.alibaba.dflow.UserException;
import com.alibaba.dflow.func.ClosureEnabledConsumer;
import com.alibaba.dflow.internal.DFlowMultiCall.InnerList;

import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function3;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DFlowMultiCall extends DFlow<InnerList> implements Function3<Integer, String, String, Boolean> {

    public static HashSet<String> nonInheritGlobalKeys = new HashSet<>();
    static {
        nonInheritGlobalKeys.add("_FINISHED");
        nonInheritGlobalKeys.add("_NEXTSTEP");
    }
    public static class InnerList{
        private List<String> data;

        public List<String> getData() {
            return data;
        }

        public void setData(List<String> data) {
            this.data = data;
        }
    }
    Logger logger = LoggerFactory.getLogger(DFlowMultiCall.class);
    private boolean andMerge = true;

    private String[] callTypes;
    private Entry[] myEntrys;

    private ClosureEnabledConsumer onInit;

    DFlowMultiCall(String identifier,String[] callTypes) throws DFlowConstructionException {
        this(identifier, callTypes,null);
    }

    public DFlowMultiCall(String[] callTypes) throws DFlowConstructionException {
        this(getCallingPosition(),callTypes,null);
    }
    public DFlowMultiCall(String[] callTypes,Consumer<ContextStack> onInit) throws DFlowConstructionException {
        this(getCallingPosition(),callTypes,onInit);
    }
    public DFlowMultiCall(String identifier, String[] callTypes,Consumer<ContextStack> onInit) throws DFlowConstructionException {
        this(getCallingPosition(),callTypes,onInit,true,null,false);
    }
    public DFlowMultiCall(String identifier, String[] callTypes,Consumer<ContextStack> onInit,boolean andMerge,String id, boolean internalNode) throws DFlowConstructionException {
        super(identifier, InnerList.class, internalNode);
        id(id);
        if(onInit != null) {
            this.onInit = new ClosureEnabledConsumer(onInit, this, "init");
        }
        this.callTypes = callTypes;
        this.andMerge = andMerge;
        //    final int index = i;
        //    if(InitEntry.checkCallTypeUsedOnlyOnePosition(callTypes[i],getCallingPosition())){
        //        InitEntry.setCallback(callTypes[i],(map,traceid)->{
        //            return apply(index,map,traceid);
        //        });
        //    }else{
        //        throw new DFlowConstructionException("Don't use same callType in different place:["+getCallingPosition()+
        //            "] And ["
        //            +InitEntry.getLastPosition(callTypes[i])+"]");
        //    }
        //}
    }


    private void arrive(ContextStack contextStack, String data, int i) throws RetryException, UserException {
        String key = buildCounterKey(contextStack);
        //String v = getGlobalStorage().get(key);
        Long total = 0L;
        //if (v != null) {
        //    total = Long.valueOf(v);
        //    ;
        //}
        String subValue = getGlobalStorage().get(key + i);
        if (subValue == null || "null".equals(subValue)) {
            total = getGlobalStorage().incr(key);
        }else{
            logger.info("multiCall already:" + contextStack.getId()+";" + i +";"+ getGlobalStorage().get(key + i) );
        }

        if(andMerge) {
            getGlobalStorage().put(key + i, data);

            if (total == callTypes.length) {
                ArrayList<String> res = new ArrayList();

                for (int j = 0; j < callTypes.length; j++) {
                    res.add(getGlobalStorage().get(key + j));
                }
                InnerList in = new InnerList();
                in.setData(res);
                cleanAndContinue(contextStack,in);
                logger.warn("Muticall Already finished!:"+contextStack.getId()+";index="+i);
            }else{
                logger.warn("Muticall not finished Yet!:"+contextStack.getId()+";index="+i+";total="+total+" out of "+callTypes.length);
            }
        }else{
            if(total != 1){
                logger.warn("Muticall Already finished!:"+contextStack.getId()+";"+i+";"+data);
                //后来的增加计数后要清掉
                getGlobalStorage().decr(key);
                return;
            }
            InnerList in = new InnerList();
            List<String> re = new ArrayList<>(callTypes.length);
            for(int j = 0; j < callTypes.length; j ++){
                if(j == i){
                    re.add(j,data);
                }else{
                    re.add(j,null);
                }
            }
            in.setData(re);
            cleanAndContinue(contextStack,in);
        }
    }

    private String buildCounterKey(ContextStack contextStack) {
        return getIDName() + contextStack.getId();
    }

    private void cleanAndContinue(ContextStack contextStack, InnerList in) throws UserException {
        String key = buildCounterKey(contextStack);
        boolean fail = false;
        //clean is better but have to add interface to old
        while (getGlobalStorage().decr(key) != -1L)
        {}
        if (0 != (getGlobalStorage().incr(key))) {
            fail = true;
        }
        for (int i = 0; i < callTypes.length; i++) {
            getGlobalStorage().put(key + i, "null");
        }

        if(fail) {
            logger.warn("Multi call clean failed, may fail when reuse multicall in one session @" + getIDName());
        }

        onReturn(contextStack,in);
    }

    @Override
    public Boolean apply(Integer index,String stringObjectMap,String traceId) throws Exception {
        //try {
            //如是第一个请求，入口先准备栈重新开始
            if(getStorage().getContext(traceId) == null) {
                call(traceId);
                arrive(getOrCreateCurrent(traceId),stringObjectMap,index);
            }else{//如果不是第一个请求，需轮到自己
                ContextStack contextStack = getStorage().getContext(traceId);
                //  DFlowZip和DFlowOr实现直接未持久化地 用各种节点组装，导致MultiCall时的name不对了，暂不检查，交给下一个节点做判重处理。
                //if(!getIDName().equals(contextStack.getName())){
                //    logger.warn("This step is already finished!@"+traceId);
                //    return false;
                //}

                if(contextStack.isLocal() && !StringUtils.equals(InternalHelper.getIp(),contextStack.getIP())){
                    InitEntry.transferCall(myEntrys[index].toInitEntryID(),stringObjectMap,traceId);
                    logger.warn(traceId+ " Local request Resend to:"+contextStack.getIP()+"@"+traceId);
                    return false;
                }


                arrive(contextStack,stringObjectMap,index);
            }

        //}catch (Exception e){
        //    logger.error(traceId+ " DFlowCall call failed",e);
        //    return false;
        //}
        return true;
    }

    @Override
    public boolean callAfterStackBuild(ContextStack context) throws RetryException, UserException {
        if(this.onInit != null){
            try {
                onInit.accept(context);
            } catch (Exception e) {
                error(context,e);
                return false;
            }
        }
        return false;
    }

    @Override
    protected String functionalUniqName(String constructingPosStr) {
        return "multicall:"+ constructingPosStr;
    }

    @Override
    public PipeLineInfo init(DFlow parentSetter, String pipelineName, int count, String nextFlowName) throws DFlowConstructionException {
        PipeLineInfo info = super.init(parentSetter ,pipelineName,count,nextFlowName);
        myEntrys = new Entry[callTypes.length];
        for(int i = 0; i < callTypes.length;i++) {
            final int index = i;
            myEntrys[i] = new Entry();
            myEntrys[i].setCallType(callTypes[i]);
            myEntrys[i].setDflowname(getIDName());
            info.putEntry(callTypes[i],myEntrys[i] );
            InitEntry.setCallback(myEntrys[i].toInitEntryID(),(map,traceid)->{
                            return apply(index,map,traceid);
                        });
        }
        return info;
    }

}
