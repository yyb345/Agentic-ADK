package com.alibaba.dflow.func;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.internal.ContextStack;

import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

public class ClosureEnabledConsumer2<T> extends AbstractClosureEnabledFunction implements BiConsumer<ContextStack,T> {

    public ClosureEnabledConsumer2(BiConsumer<ContextStack,? super T> innerMapper,DFlow funckey,String subfix)
        throws InvalidClosureFunctionException {
        super(innerMapper,funckey,subfix);
        realMapper = innerMapper;
        checkClosureIsValid(true);
    }

    public ClosureEnabledConsumer2(Consumer<? super T> innerMapper,DFlow funckey,String subfix) throws InvalidClosureFunctionException {
        super(innerMapper,funckey,subfix);
        realMapper = innerMapper;
        checkClosureIsValid(true);
    }

    @Override
    public void accept(ContextStack c,T t) throws Exception {
        try {
            before(c);
            if (realMapper instanceof BiConsumer) {
                ((BiConsumer<ContextStack, ? super T>)realMapper).accept(c, t);
            } else {
                ((Consumer<? super T>)realMapper).accept(t);
            }
        }catch (Exception e){
            throw e;
        }finally {
            after(c);
        }
    }
}
