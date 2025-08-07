//package com.alibaba.dflow.internal;
//
//import java.lang.reflect.Type;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.List;
//
//import com.alibaba.dflow.DFlow;
//import com.alibaba.dflow.InitEntry;
//import com.alibaba.dflow.InitEntry.Entry;
//import com.alibaba.dflow.PipeLineInfo;
//import com.alibaba.dflow.RetryException;
//import com.alibaba.dflow.TerminalException;
//import com.alibaba.dflow.UserException;
//import com.alibaba.dflow.func.ClosureEnabledConsumer;
//import com.alibaba.dflow.func.ClosureEnabledFunction2;
//import com.alibaba.dflow.func.SafeObject;
//import com.alibaba.dflow.func.ValidClosure;
//import com.alibaba.dflow.internal.ContextStack.ContextNode;
//
//import io.reactivex.functions.BiFunction;
//import io.reactivex.functions.Consumer;
//import io.reactivex.functions.Function3;
//import org.apache.commons.lang3.NotImplementedException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import static com.alibaba.dflow.internal.ContextStack.STATUS_SUB;
//
//public class DFlowScatter<T,R,P> extends AbstractDFlowWithUpstream<T,P> implements BiFunction<String, String, Boolean> {
//    private final ClosureEnabledFunction2<P, List<DFlow<String>>> mapper;
//    private final Function3<ContextStack, P, R, ? extends R> reducer;
//    Logger logger = LoggerFactory.getLogger(DFlowScatter.class);
//
//    private static final String DFLOWSCATTERERROR = "_DFLOW_SCATTER_ERROR";
//
//    private boolean parallel = true;
//    private String collectCallType;
//
//    public DFlowScatter(String identifier,DFlow<T> source, BiFunction<ContextStack, ? super T, List<DFlow<String>>> innerMapper,  Function3<ContextStack, R, ? extends P> reducer,Type clazz,String id,boolean internalNode,boolean parallel)
//        throws DFlowConstructionException {
//        super(identifier,source,clazz,internalNode);
//        this.mapper = new ClosureEnabledFunction2<P, List<DFlow<String>>>(innerMapper,this,"");
//        this.reducer = reducer;
//        this.parallel = parallel;
//        this.id(id);
//    }
//
//    @Override
//    protected boolean actualCall(ContextStack contextStack, T r) throws RetryException, UserException {
//        try {
//            List<DFlow<? extends R>> lastInnerFlows = mapper.apply(contextStack, r);
//
//            ContextNode node = contextStack.getStack().peek();
//
//            //set child info
//            if(parallel){
//                node.setParallel();
//            }
//            node.setChildTotalSize(lastInnerFlows.size());
//            if(lastInnerFlows.size() <= 3) {
//                String[] childIds = new String[lastInnerFlows.size()];
//                for (int i = 0; i < childIds.length; i++) {
//                    childIds[i] = getChildId(contextStack,i);
//                }
//                node.setChildTask(childIds);
//            }else{
//                String[] childIds = new String[3];
//                childIds[0] = getChildId(contextStack,0);
//                childIds[1] = "...";
//                childIds[0] = getChildId(contextStack,lastInnerFlows.size()-1);
//                node.setChildTask(childIds);
//            }
//
//            if(parallel) {
//                for (DFlow<? extends R> p : lastInnerFlows) {
//                    callInnerFlow(contextStack, p);
//                }
//            }else{
//                throw new NotImplementedException("串行尚未实现");
//            }
//            return true;
//        } catch (Exception e) {
//            error(contextStack, e);
//            return false;
//        }
//    }
//
//    private void callInnerFlow(ContextStack contextStack, DFlow<? extends R> innerFlow) throws Exception {
//        //初始化收集流
//        DFlow<String> collectFlow = new DFlowMap<String>("innerscatterm"+functionalUniqName() ,innerFlow,
//            (con,r)-> {
//
//                InitEntry.call(collectCallType,r,con.get(ParentId));
//                //onReturn(c, result);
//                return "";
//            },clazz
//            ,"innerscatterm"+functionalUniqName(),true);
//        collectFlow.init(null,pipelineName,0,null);
//
//
//
//        //mapper返回的是最终的流节点，要找到起始的流节点开始触发
//        DFlow initInnerFlow = collectFlow;
//        while(initInnerFlow instanceof AbstractDFlowWithUpstream){
//            initInnerFlow = ((AbstractDFlowWithUpstream)initInnerFlow).getSource();
//        }
//
//        //mapper后 context可能有变化，在触发子流前更新
//        DFlow.getStorage().putContext(contextStack.getId(),contextStack);
//
//        //flatmap的下一级节点很有可能未完全初始化完毕，未初始化完时，后续链路都不能保证每台机器都有代码，要local
//        if(!isAllInited(getIDName(),initInnerFlow.getIDName())){
//            contextStack.setLocal();
//            getStorage().putContext(contextStack.getId(),contextStack);
//        }
//        //设置内部流的parentFlowStep,以便检查是不是当前应该处理的节点检查通过。
//        // null也让过先注释掉
//        //initInnerFlow.setParentFlowStep(this.getIDName());
//        //在每个机器都出发前的几次调用应该都在本地完成,DFlow.just必然本地解决,避免初始化参数要获取并传给另一台机器重新去处理
//        initInnerFlow.call(contextStack.getId());
//    }
//
//    private DFlow constructAndGetFirst(DFlow lastFlow) {
//        //初始化内部流，有可能是新的，也有可能是已经全局初始化过的
//        lastInnerFlow.init(null,pipelineName,0,null);
//
//        //mapper返回的是最终的流节点，要找到起始的流节点开始触发
//        DFlow initInnerFlow = lastInnerFlow;
//        while(initInnerFlow instanceof AbstractDFlowWithUpstream){
//            initInnerFlow = ((AbstractDFlowWithUpstream)initInnerFlow).getSource();
//        }
//    }
//
//    private String getChildId(ContextStack contextStack, int i){
//        return contextStack.getId() + "_" + contextStack.getStack().size() + "-" + i;
//    }
//
//    @Override
//    public boolean callAfterStackBuild(ContextStack context) throws RetryException, UserException {
//
//        try {
//
//            context.setStatus(STATUS_SUB);
//            String startId = context.getId();
//            //设置当前进度为multicall， trigger后持久化了但是context没变需要重新读取
//            multiCall.call(startId);
//            context = getStorage().getContext(startId);
//            String[] childTaskIds = new String[triggers.length];
//            for(int i = 0; i < triggers.length; i++){
//                childTaskIds[i] = startId+"_"+ context.getStack().size() +"-"+i;
//            }
//            //取全新的
//            ContextStack innerContext = getStorage().getContext(startId);
//
//            context.setChildTask(childTaskIds);
//            //save
//            getStorage().putContext(startId,context);
//
//
//            for (int i = 0; i < triggers.length; i++) {
//                innerContext.put("startId",startId);
//                //trigger使用新的id，复制context
//                innerContext.setId(childTaskIds[i]);
//                //build new childstack
//                innerContext.setStatus(STATUS_SUB);
//                getStorage().putContext(innerContext.getId(),innerContext);
//
//                //设置当前进度为下一个流的入口，trigger后持久化了但是context没变需要重新读取
//                DFlow initInnerFlow = flows[i];
//                //zip的flow的是最终的流，要找到起始的流开始触发
//                while(initInnerFlow instanceof AbstractDFlowWithUpstream){
//                    initInnerFlow = ((AbstractDFlowWithUpstream)initInnerFlow).getSource();
//                }
//
//                //initInnerFlow.setParentFlowStep(getIDName());
//                initInnerFlow.call(innerContext.getId());
//            }
//
//            for(int i =0; i < triggers.length; i++){
//                ContextStack innerContext2 = getStorage().getContext(childTaskIds[i]);
//
//                if(triggers[i] != null) {
//                    triggers[i].accept(innerContext2);
//                }
//            }
//
//        } catch (Exception e) {
//            error(context, e);
//            return false;
//        }
//        return false;
//    }
//
//    @Override
//    protected String functionalUniqName(String constructingPosStr) {
//        return "scatter:" + constructingPosStr;
//    }
//
//
//
//    @Override
//    public PipeLineInfo init(DFlow parentSetter, String pipelineName, int stepcount, String nextFlowID) throws DFlowConstructionException {
//        PipeLineInfo resultInfo = super.init(parentSetter,pipelineName,stepcount,nextFlowID);
//        this.collectCallType = "scatterback"+getIDName();
//        InitEntry.setCallback(collectCallType,this);
//
//        return resultInfo;
//    }
//
//    /**
//     * Call callback
//     * @param s
//     * @param s2
//     * @return
//     * @throws Exception
//     */
//    @Override
//    public Boolean apply(String s, String s2) throws Exception {
//
//        //TODO counting
//        //TODO merge context
//        List<HashMap<String,Object>> globalsToMerge = new ArrayList<>();
//        //find first branch
//        for(int i = con.getStack().size(); i > 0 ; i--){
//            ContextNode n = (con.getStack().get(i-1));
//            if(n.getChildTask() != null){
//                String[] childids = n.getChildTask();
//                for(int j = 0;j<childids.length;j++){
//                    globalsToMerge.add(getStorage().getContext(childids[j]).getGlobal());
//                }
//                break;
//            }
//        }
//
//        HashMap<String,Object> merged = new HashMap<>();
//
//        globalsToMerge.stream().forEach(map->{
//            for(String key: map.keySet()){
//                if(con.getGlobal().containsKey(key)){
//
//                }else if(!merged.containsKey(key)){
//                    merged.put(key,map.get(key));
//                }else{
//                    int i = 0;
//                    do{
//                        merged.put(key+i,map.get(key));
//                        i++;
//                    }while ((merged.containsKey(key+i)));
//                }
//            }
//        });
//
//        con.getGlobal().putAll(merged);
//
//        for(String s:res){
//            if(s.startsWith(DFLOWSCATTERERROR)){
//                throw new Exception("scatter Failed due to inner flow failed:"+s);
//            }
//        }
//        return null;
//    }
//}
