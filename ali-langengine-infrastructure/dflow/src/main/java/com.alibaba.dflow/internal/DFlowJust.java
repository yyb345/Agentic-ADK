package com.alibaba.dflow.internal;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.RetryException;
import com.alibaba.dflow.UserException;
import com.alibaba.fastjson.util.TypeUtils;

public class DFlowJust<R> extends DFlow<R> {
    private R data;
    public DFlowJust(R source) throws DFlowConstructionException {
        super(source.getClass());
        data = source;
        //兼容一下老版本
        if(DFlow.g_strictMode) {
            id("Just");
        }
    }

    @Override
    protected boolean callAfterStackBuild(ContextStack context) throws RetryException, UserException {
        onReturn(context,data);
        return true;
    }

    @Override
    protected String functionalUniqName(String constructingPos) {
        return "Just";
    }
}
