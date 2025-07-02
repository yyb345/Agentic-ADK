package com.alibaba.dflow.config;

import java.util.function.Consumer;

import com.alibaba.dflow.UserException;
import com.alibaba.dflow.internal.ContextStack;

public interface StepHandler {
    int order();
    void onStepBefore(ContextStack c);

    void onStepAfter(ContextStack c);

    default void wrap(ContextStack contextStack,ThrowingConsumer<ContextStack> c) throws UserException{
        c.accept(contextStack);
    }
}
