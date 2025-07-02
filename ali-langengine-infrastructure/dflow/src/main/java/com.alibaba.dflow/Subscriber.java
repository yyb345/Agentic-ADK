package com.alibaba.dflow;

import com.alibaba.dflow.internal.ContextStack;

public interface Subscriber<T> {

    void onNext(String traceId);

    void onError(ContextStack t, Throwable var1);

}


