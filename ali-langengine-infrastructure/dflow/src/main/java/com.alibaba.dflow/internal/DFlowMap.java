package com.alibaba.dflow.internal;


import java.lang.reflect.Type;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.RetryException;
import com.alibaba.dflow.UserException;
import com.alibaba.dflow.func.ClosureEnabledFunction2;

import io.reactivex.functions.BiFunction;

public class DFlowMap<T, R> extends AbstractDFlowWithUpstream<T,R> {
    private ClosureEnabledFunction2<T,R> mapper;

    DFlowMap(String identifier,DFlow<T> source,BiFunction<ContextStack, ? super T, ? extends R> mapper,Type clazz,String id,boolean internalNode)
        throws DFlowConstructionException {
        super(identifier,source,clazz,internalNode);
        this.mapper = new ClosureEnabledFunction2<T, R>(mapper,this,"");
        this.id(id);
    }

    DFlowMap(String identifier,DFlow<T> source,BiFunction<ContextStack, ? super T, ? extends R> mapper,Type clazz)
        throws DFlowConstructionException {
        super(identifier,source,clazz);
        this.mapper = new ClosureEnabledFunction2<T, R>(mapper,this,"");
    }

    public DFlowMap(DFlow<T> source, io.reactivex.functions.Function<? super T, ? extends R> mapper,Type clazz)
        throws DFlowConstructionException {
        super(source,clazz);
        this.mapper = new ClosureEnabledFunction2<T, R>(mapper,this,"");
    }
    public DFlowMap(DFlow<T> source, BiFunction<ContextStack, ? super T, ? extends R> mapper,Type clazz)
        throws DFlowConstructionException {
        super(source,clazz);
        this.mapper = new ClosureEnabledFunction2<T, R>(mapper,this,"");
    }

    @Override
    protected boolean actualCall(ContextStack contextStack, T t) throws RetryException, UserException {
        try {
            R r = mapper.apply(contextStack,t);
            onReturn(contextStack,r);
        } catch (Exception e) {
            error(contextStack,e);
            return false;
        }
        return true;
    }

    @Override
    protected String functionalUniqName(String constructingPosStr) {
        return "Map:"+ constructingPosStr;// +FunctionUtil.getAnymouseFuncName(mapper.getRealMapper());
    }
}
