package com.alibaba.dflow.config;

import com.alibaba.dflow.PersistentException;
import com.alibaba.dflow.internal.ContextStack;

public interface ContextStoreInterface{
    ContextStack getContext(String traceId);
    void putContext(String key, ContextStack context) throws PersistentException;
    void expireContext(String traceId);
    void removeContext(String traceId);
}
