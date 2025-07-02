package com.alibaba.dflow.func;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.internal.ContextStack;

import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public class ClosureEnabledFunction<T> extends AbstractClosureEnabledFunction
    implements Function<ContextStack, T> {
    public ClosureEnabledFunction(Function<ContextStack,? extends T> innerMapper,DFlow flow,String subfix)
        throws InvalidClosureFunctionException {
        super(innerMapper,flow,subfix);
        realMapper = innerMapper;
        checkClosureIsValid(true);
    }

    @Override
    public T apply(ContextStack c) throws Exception {
        try {
            before(c);
            return ((Function<ContextStack,? extends T>)realMapper).apply(c);
        } catch (Exception e) {
            throw e;
        } finally {
            after(c);
        }
    }

}
