package com.alibaba.dflow.func;

import com.alibaba.dflow.DFlow;

import io.reactivex.functions.Consumer;

public class ClosureDisabledConsumer<T> extends AbstractClosureEnabledFunction implements Consumer<T> {
    public ClosureDisabledConsumer(Consumer<? super T> innerMapper,DFlow funckey) throws InvalidClosureFunctionException {
        super(innerMapper,funckey);
        realMapper = innerMapper;
        checkClosureIsValid(false);
    }

    @Override
    public void accept(T t) throws Exception {
        ((Consumer)realMapper).accept(t);
    }
}
