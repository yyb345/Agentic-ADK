package com.alibaba.dflow.func;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.internal.ContextStack;

import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public class ClosureEnabledFunction2<T, R> extends AbstractClosureEnabledFunction
    implements BiFunction<ContextStack, T, R> {
    public ClosureEnabledFunction2(BiFunction<ContextStack, ? super T, ? extends R> innerMapper,DFlow flow,String subfix)
        throws InvalidClosureFunctionException {
        super(innerMapper,flow,subfix);
        realMapper = innerMapper;
        checkClosureIsValid(true);
    }
    public ClosureEnabledFunction2(BiFunction<ContextStack, ? super T, ? extends R> innerMapper,DFlow flow)
        throws InvalidClosureFunctionException {
        this(innerMapper,flow,"");
    }

    public ClosureEnabledFunction2(Function<? super T, ? extends R> innerMapper,DFlow flow,String subfix)
        throws InvalidClosureFunctionException {
        super(innerMapper,flow,subfix);
        realMapper = innerMapper;

        checkClosureIsValid(true);
    }
    public ClosureEnabledFunction2(Function<? super T, ? extends R> innerMapper,DFlow flow)
        throws InvalidClosureFunctionException {
        this(innerMapper,flow,"");
    }


    @Override
    public R apply(ContextStack c, T t) throws Exception {
        try {
            before(c);
            if (realMapper instanceof BiFunction) {
                return ((BiFunction<ContextStack, ? super T, ? extends R>)realMapper).apply(c, t);
            } else {
                return ((Function<? super T, ? extends R>)realMapper).apply(t);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            after(c);
        }

    }
}
