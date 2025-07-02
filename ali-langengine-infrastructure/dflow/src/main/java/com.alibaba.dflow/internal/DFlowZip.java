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

public class DFlowZip<R> extends DFlow<R> implements ValidClosure{
    Logger logger = LoggerFactory.getLogger(DFlowZip.class);

    private static final String DFLOWZIPERROR = "_DFLOW_ZIP_ERROR";

    private DFlow<String>[] flows;
    private Consumer<ContextStack>[] triggers;
    private ClosureEnabledFunction2<String[], ? extends R> zipper;
    private DFlowMultiCall multiCall;

    public DFlowZip(DFlow<String>[] flows, Consumer<ContextStack>[] triggers,BiFunction<ContextStack, String[], ? extends R> zipper,Type clazz) throws DFlowConstructionException {
        super(clazz);
        this.flows = flows;
        this.triggers = new ClosureEnabledConsumer[triggers.length];
        for(int i = 0; i < triggers.length;i++){
            if(triggers[i] != null) {
                this.triggers[i] = new ClosureEnabledConsumer(triggers[i], this, String.valueOf(i));
            }
        }
        this.zipper = new ClosureEnabledFunction2<String[], R>(zipper,this);
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
            getStoreage().putContext(startId,context);
            multiCall.call(startId);
            context = getStoreage().getContext(startId);
            String[] childTaskIds = new String[triggers.length];
            for(int i = 0; i < triggers.length; i++){
                childTaskIds[i] = startId+"_"+ context.getStack().size() +"-"+i;
            }
            //取全新的
            ContextStack innerContext = getStoreage().getContext(startId);

            context.setChildTask(childTaskIds);
            //save
            getStoreage().putContext(startId,context);


            for (int i = 0; i < triggers.length; i++) {
                innerContext.put("startId",startId);
                //trigger使用新的id，复制context
                innerContext.setId(childTaskIds[i]);
                //build new childstack
                innerContext.setStatus(STATUS_SUB);
                getStoreage().putContext(innerContext.getId(),innerContext);

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
                ContextStack innerContext2 = getStoreage().getContext(childTaskIds[i]);

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
        return "zip:" + constructingPosStr +"size"+ flows.length;
    }



    @Override
    public PipeLineInfo init(DFlow parentSetter, String pipelineName, int stepcount, String nextFlowID) throws DFlowConstructionException {
        PipeLineInfo resultInfo = super.init(parentSetter,pipelineName,stepcount,nextFlowID);
        //所有机器数据都是一样的则可以使用safeobject
        final SafeObject<String[]> messages = new SafeObject<>();
        messages.data = new String[flows.length];
        multiCall = new DFlowMultiCall("innerzip"+functionalUniqName(),messages.data,(c)->{},true,"innerzip"+functionalUniqName(),true);

        //初始化收集消息
        for(int i = 0; i < flows.length; i++) {
            messages.data[i] = "zip" + i + functionalUniqName();
        }
        //初始化收集流
        DFlow collectFlow = new DFlowMap<>("innerzipm"+functionalUniqName() ,multiCall,
            (con,r)-> {
                List<String> res = r.getData();

                List<HashMap<String,Object>> globalsToMerge = new ArrayList<>();
                //find first branch
                for(int i = con.getStack().size(); i > 0 ; i--){
                    ContextNode n = (con.getStack().get(i-1));
                    if(n.getChildTask() != null){
                        String[] childids = n.getChildTask();
                        for(int j = 0;j<childids.length;j++){
                            globalsToMerge.add(getStoreage().getContext(childids[j]).getGlobal());
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

                for(String s:res){
                    if(s.startsWith(DFLOWZIPERROR)){
                        throw new Exception("Zip Failed due to inner flow failed:"+s);
                    }
                }
                R result = zipper.apply(con,res.toArray(new String[res.size()]));
                //onReturn(c, result);
                return result;
            },clazz
        ,"innerzipm"+functionalUniqName(),true);
        collectFlow.init(null,pipelineName,stepcount+1,nextFlowID);
        final SafeObject<String> nextFlow = new SafeObject<>();
        nextFlow.data = multiCall.getIDName();

        //初始化内部流
        for(int i = 0; i < flows.length; i++){
            final SafeObject<Integer> index = new SafeObject<>();
            index.data = i;
            new DFlowMap<>("innerzipm" + i + functionalUniqName(),flows[i],(c,r)-> {
                String startId = c.get("startId");
                //Trigger multicall result
                Entry e = new Entry(messages.data[index.data]);
                e.setDflowname(nextFlow.data);
                DFlow.call(e,r,startId);
                throw new EndException("Child process Ended");
            },String.class,"innerzipm" + i + functionalUniqName(),true)
                .onErrorReturn(c-> {
                    String startId = c.get("startId");
                    Entry e = new Entry(messages.data[index.data]);
                    e.setDflowname(nextFlow.data);
                    DFlow.call(e, DFLOWZIPERROR+"_"+messages.data[index.data],startId);
                    throw new EndException("Child process Ended");
                }).init(null,pipelineName,stepcount+1, collectFlow.getIDName());

        }


        return resultInfo;
    }

}
