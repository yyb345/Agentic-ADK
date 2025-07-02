package com.alibaba.dflow.func;

import java.util.HashMap;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.InternalHelper;

import io.reactivex.functions.Consumer;

public class ClosureEnabledConsumer extends AbstractClosureEnabledFunction implements Consumer<ContextStack> {
    public ClosureEnabledConsumer(Consumer<ContextStack> innerMapper,DFlow funckey,String subfix) throws InvalidClosureFunctionException {
        super(innerMapper,funckey,subfix);
        realMapper = innerMapper;
        checkClosureIsValid(true);
    }

    @Override
    public void accept(ContextStack t) throws Exception {
        try {
            before(t);
            ((Consumer)realMapper).accept(t);
        }catch (Exception e){
            throw e;
        }finally {
            after(t);
        }
    }
}
