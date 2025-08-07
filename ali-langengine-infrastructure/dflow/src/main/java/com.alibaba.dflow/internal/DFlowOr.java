package com.alibaba.dflow.internal;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.EndException;
import com.alibaba.dflow.InitEntry.Entry;
import com.alibaba.dflow.PipeLineInfo;
import com.alibaba.dflow.RetryException;
import com.alibaba.dflow.TerminalException;
import com.alibaba.dflow.UserException;
import com.alibaba.dflow.func.ClosureEnabledConsumer;
import com.alibaba.dflow.func.ClosureEnabledFunction2;
import com.alibaba.dflow.func.SafeObject;
import com.alibaba.dflow.func.ValidClosure;
import com.alibaba.dflow.internal.ContextStack.ContextNode;

import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.alibaba.dflow.internal.ContextStack.STATUS_SUB;
import static com.alibaba.dflow.internal.DFlowMultiCall.nonInheritGlobalKeys;

public class DFlowOr<R> extends DFlow<R> implements ValidClosure{
    Logger logger = LoggerFactory.getLogger(DFlowOr.class);

    private static final String DFLOWZIPERROR = "_DFLOW_ZIP_ERROR";

    private DFlow<String>[] flows;
    private Consumer<ContextStack>[] triggers;
    private ClosureEnabledFunction2<OrResult, ? extends R> resultJudger;
    private DFlowMultiCall multiCall;

    public DFlowOr(DFlow<String>[] flows, Consumer<ContextStack>[] triggers,BiFunction<ContextStack, OrResult, ? extends R> resultJudger,Type clazz) throws DFlowConstructionException {
        super(clazz);
        this.flows = flows;
        this.triggers = new ClosureEnabledConsumer[triggers.length];
        for(int i = 0; i < triggers.length;i++){
            if(triggers[i] != null) {
                this.triggers[i] = new ClosureEnabledConsumer(triggers[i], this, String.valueOf(i));
            }
        }
        this.resultJudger = new ClosureEnabledFunction2<OrResult, R>(resultJudger,this);
        if(triggers.length != flows.length){
            throw new DFlowConstructionException("flow and trigger not match");
        }
    }

    @Override
    public boolean callAfterStackBuild(ContextStack context) throws RetryException, UserException {

        try {

            context.setStatus(STATUS_SUB);
            String startId = context.getId();
            //设置当前进度为multicall， trigger后持久化了但是context没变需要重新读取
            getStorage().putContext(startId,context);
            multiCall.call(startId);
            context = getStorage().getContext(startId);
            String[] childTaskIds = new String[triggers.length];
            for(int i = 0; i < triggers.length; i++){
                childTaskIds[i] = startId+"_"+ context.getStack().size() +"-"+i;
            }

            //先取全新的一份
            ContextStack innerContext = getStorage().getContext(startId);

            //先save 在触发
            context.setChildTask(childTaskIds);
            //save
            getStorage().putContext(startId,context);

            for (int i = 0; i < triggers.length; i++) {
                innerContext.put("startId",startId);
                //trigger使用新的id，复制context
                innerContext.setId(childTaskIds[i]);
                //build new childstack
                innerContext.setStatus(STATUS_SUB);
                getStorage().putContext(innerContext.getId(),innerContext);

                //设置当前进度为下一个流的入口，trigger后持久化了但是context没变需要重新读取
                DFlow initInnerFlow = flows[i];
                //zip的flow的是最终的流，要找到起始的流开始触发
                while(initInnerFlow instanceof AbstractDFlowWithUpstream){
                    initInnerFlow = ((AbstractDFlowWithUpstream)initInnerFlow).getSource();
                }

                //initInnerFlow.setParentFlowStep(getIDName());
                initInnerFlow.call(innerContext.getId());
            }

            for(int i =0; i < triggers.length; i++){
                ContextStack innerContext2 = getStorage().getContext(childTaskIds[i]);

                if(triggers[i] != null) {
                    triggers[i].accept(innerContext2);
                }
            }

        } catch (Exception e) {
            error(context, e);
            return false;
        }
        return false;
    }

    @Override
    public DFlow<R> id(String id) throws DFlowConstructionException {
        return super.id(id+"$"+triggers.length);
    }

    @Override
    protected String functionalUniqName(String constructingPosStr) {
        return "or:" + constructingPosStr +"size"+ flows.length;
    }



    @Override
    public PipeLineInfo init(DFlow parentSetter,String pipelineName, int stepcount, String nextFlowID) throws DFlowConstructionException {
        PipeLineInfo resultInfo = super.init(parentSetter,pipelineName,stepcount,nextFlowID);
        //所有机器数据都是一样的则可以使用safeobject
        final SafeObject<String[]> messages = new SafeObject<>();
        messages.data = new String[flows.length];
        multiCall = new DFlowMultiCall("innerop"+functionalUniqName(),messages.data,(c)->{},false,"innerop"+functionalUniqName(),true);
        //初始化收集消息
        for(int i = 0; i < flows.length; i++) {
            messages.data[i] = "or" + i + functionalUniqName();
        }
        //初始化收集流
        DFlow collectFlow = new DFlowMap<>("innerorm"+functionalUniqName() ,multiCall,
            (con,r)-> {
                List<String> res = r.getData();

                List<HashMap<String,Object>> globalsToMerge = new ArrayList<>();
                //find first branch
                for(int i = con.getStack().size(); i > 0 ; i--){
                    ContextNode n = (con.getStack().get(i-1));
                    if(n.getChildTask() != null){
                        String[] childids = n.getChildTask();
                        for(int j = 0;j<childids.length;j++){
                            ContextStack s = getStorage().getContext(childids[j]);
                            if(s != null) {
                                globalsToMerge.add(s.getGlobal());
                            }
                        }
                        break;
                    }
                }

                HashMap<String,Object> merged = new HashMap<>();

                globalsToMerge.stream().forEach(map->{
                    for(String key: map.keySet()){
                        if(con.getGlobal().containsKey(key) || nonInheritGlobalKeys.contains(key)){

                        }else if(!merged.containsKey(key)){
                            merged.put(key,map.get(key));
                        }else{
                            int i = 0;
                            do{
                                merged.put(key+i,map.get(key));
                                i++;
                            }while ((merged.containsKey(key+i)));
                        }
                    }
                });

                con.getGlobal().putAll(merged);

                for(int i = 0; i < res.size(); i++){
                    String s = res.get(i);
                    if(!s.startsWith(DFLOWZIPERROR)){
                        OrResult orResult = new OrResult();
                        orResult.setData(s);
                        orResult.setIndex(i);
                        R result = resultJudger.apply(con,orResult);
                        return result;
                    }
                }
                throw new Exception("Or Failed due to inner flow all failed");
            },clazz
        ,"innerorm"+functionalUniqName(),true);
        collectFlow.init(null,pipelineName,stepcount+1,nextFlowID);

        final SafeObject<String> nextFlow = new SafeObject<>();
        nextFlow.data = multiCall.getIDName();

        for(int i = 0; i < flows.length; i++){
            final SafeObject<Integer> index = new SafeObject<>();
            index.data = i;
            new DFlowMap<>("innerorm" + i + functionalUniqName(),flows[i],(c,r)-> {
                String startId = c.get("startId");
                //TODO merge global value, confliction warning
                //Trigger multicall result
                Entry e = new Entry(messages.data[index.data]);
                e.setDflowname(nextFlow.data);
                DFlow.call(e,r,startId);
                throw new EndException("Child process Ended");
            },String.class,"innerorm" + i + functionalUniqName(),true)
                .onErrorReturn(c-> {
                    String startId = c.get("startId");
                    Entry e = new Entry(messages.data[index.data]);
                    e.setDflowname(nextFlow.data);
                    DFlow.call(e, DFLOWZIPERROR+"_"+messages.data[index.data],startId);
                    throw new EndException("Child process Ended");
                })
                .init(null,pipelineName,stepcount+1,collectFlow.getIDName());
        }

        return resultInfo;
    }

    public static class OrResult{
        private String data;
        private Integer index;

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }
    }
}
