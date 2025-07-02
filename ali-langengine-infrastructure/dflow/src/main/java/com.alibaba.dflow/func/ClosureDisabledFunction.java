package com.alibaba.dflow.func;

import com.alibaba.dflow.DFlow;

import io.reactivex.functions.Function;

public class ClosureDisabledFunction<T,R> extends AbstractClosureEnabledFunction implements Function<T,R>{


    public ClosureDisabledFunction(Function<? super T,? extends R> innerMapper,DFlow funckey,String subfix) throws InvalidClosureFunctionException {
        //funckey is not critical for closuredisable functions
        super(innerMapper,funckey,subfix);
        realMapper = innerMapper;
        checkClosureIsValid(false);
    }

    @Override
    public R apply(T t) throws Exception {
        try {
            return ((Function<? super T, ? extends R>)realMapper).apply(t);
        }catch (Exception e){
            throw e;
        }
    }
}
