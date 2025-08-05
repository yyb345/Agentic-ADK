package com.alibaba.dflow.internal;

import java.lang.reflect.Type;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.RetryException;
import com.alibaba.dflow.UserException;
import com.alibaba.dflow.func.ClosureEnabledFunction2;

import io.reactivex.functions.BiFunction;

import static com.alibaba.dflow.internal.ContextStack.STATUS_SUB;

public class DFlowFlatmap<T, R> extends AbstractDFlowWithUpstream<T,R> {

    private ClosureEnabledFunction2<? super T, ? extends DFlow<? extends R>>  mapper;
    public DFlowFlatmap(DFlow<T> source,BiFunction<ContextStack, ? super T, ? extends DFlow<? extends R>> innerMapper,Type clazz)
        throws DFlowConstructionException {
        super(source,clazz);
        this.mapper = new ClosureEnabledFunction2<T, DFlow<? extends R>>(innerMapper,this,"");
    }
    public DFlowFlatmap(DFlow<T> source,
        io.reactivex.functions.Function<? super T, ? extends DFlow<? extends R>> innerMapper,Type clazz)
        throws DFlowConstructionException {
        super(source,clazz);
        this.mapper = new ClosureEnabledFunction2<T, DFlow<? extends R>>(innerMapper,this,"");
    }

    @Override
    protected String functionalUniqName(String constructingPosStr) {
        return "flatMap:"+ constructingPosStr;
    }

    /**
     * flatMap节点的实际调用会创建新节点或者复用已经有的代码节点
     * DFlow.just(1).flatMap(x->if(x==1) DFlow.just(2).flatMap(y->DFlow.just(x)) else DFlow.just(3).flatMap(y->DFlow.just(y)))
     * @param contextStack
     * @param r
     * @return
     */
    @Override
    protected boolean actualCall(ContextStack contextStack, T r) throws RetryException, UserException {
        try {

            DFlow<? extends R> lastInnerFlow = mapper.apply(contextStack, r);

            //初始化内部流，有可能是新的，也有可能是已经全局初始化过的
            lastInnerFlow.init(null,pipelineName,0,nextStepId);


            //mapper返回的是最终的流节点，要找到起始的流节点开始触发
            DFlow initInnerFlow = lastInnerFlow;
            while(initInnerFlow instanceof AbstractDFlowWithUpstream){
                initInnerFlow = ((AbstractDFlowWithUpstream)initInnerFlow).getSource();
            }

            contextStack.setStatus(STATUS_SUB);
            //mapper后 context可能有变化，在触发子流前更新
            DFlow.getStorage().putContext(contextStack.getId(),contextStack);

            //flatmap的下一级节点很有可能未完全初始化完毕，未初始化完时，后续链路都不能保证每台机器都有代码，要local
            if(!isAllInited(getIDName(),initInnerFlow.getIDName())){
                contextStack.setLocal();
                getStorage().putContext(contextStack.getId(),contextStack);
            }
            //设置内部流的parentFlowStep,以便检查是不是当前应该处理的节点检查通过。
            // null也让过先注释掉
            //initInnerFlow.setParentFlowStep(this.getIDName());
            //在每个机器都出发前的几次调用应该都在本地完成,DFlow.just必然本地解决,避免初始化参数要获取并传给另一台机器重新去处理
            initInnerFlow.call(contextStack.getId());

        } catch (Exception e) {
            error(contextStack,e);
            return false;
        }
        return true;
    }

}
