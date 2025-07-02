package com.alibaba.dflow.internal;

import java.lang.reflect.Type;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.PipeLineInfo;
import com.alibaba.dflow.RetryException;
import com.alibaba.dflow.UserException;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;

import org.springframework.util.Assert;

/**
 * 有上级输入的流
 * @param <T>
 * @param <R>
 */
public abstract class AbstractDFlowWithUpstream<T,R> extends DFlow<R> {
    private DFlow<T> source;
    public AbstractDFlowWithUpstream(DFlow<T> source,Type clazz) throws DFlowConstructionException {
        super(clazz);
        this.source = source;
    }
    public AbstractDFlowWithUpstream(String identifier, DFlow<T> source,Type clazz) throws DFlowConstructionException {
        super(identifier,clazz);
        this.source = source;
    }
    public AbstractDFlowWithUpstream(String identifier, DFlow<T> source,Type clazz,boolean internalNode) throws DFlowConstructionException {
        super(identifier,clazz,internalNode);
        this.source = source;
    }
    public DFlow<T> getSource() {
        return source;
    }

    /**
     * 辅助函数解开上一级传入的参数
     * @param contextStack
     * @param r
     * @return
     */
    protected abstract boolean actualCall(ContextStack contextStack, T r) throws RetryException, UserException;

    @Override
    public boolean callAfterStackBuild(ContextStack context) throws RetryException, UserException {

        //assert new stack is created
        Assert.isTrue(context.getName().equals(getIDName()),"Current stack is not built on onTrigger");
        T result = TypeUtils.cast(context.getParam(),source.clazz, ParserConfig.getGlobalInstance());
        // TODO Array & Generic not support yet
        //T result = JSON.parseObject(context.getParam().toString(),source.clazz);
        return actualCall(context, result);
    }

    @Override
    public PipeLineInfo init(DFlow nextFlow,String pipelineNmae,int count,String nextFlowName) throws DFlowConstructionException {
        PipeLineInfo thisInfo = super.init(nextFlow,pipelineNmae,count,nextFlowName);
        PipeLineInfo lastInfo = source.init(this,pipelineNmae,count+1,this.getIDName());
        return PipeLineInfo.merge(thisInfo,lastInfo);
    }

}
