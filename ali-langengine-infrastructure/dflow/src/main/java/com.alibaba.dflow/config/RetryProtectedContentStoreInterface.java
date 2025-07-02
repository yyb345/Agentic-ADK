package com.alibaba.dflow.config;

import java.util.List;

import com.alibaba.dflow.PersistentException;
import com.alibaba.dflow.RetryException;
import com.alibaba.dflow.internal.ContextStack;

public class RetryProtectedContentStoreInterface implements ContextStoreInterface {
    ContextStoreInterface contextStoreInterface;
    public RetryProtectedContentStoreInterface(ContextStoreInterface contextStoreInterface) {
        this.contextStoreInterface = contextStoreInterface;
    }

    @Override
    public ContextStack getContext(String traceId) {
        try{
            return contextStoreInterface.getContext(traceId);
        }catch (Throwable t) {
            throw new PersistentException(t.getMessage());
        }
    }

    @Override
    public void putContext(String key, ContextStack context) throws PersistentException {
        try {
            contextStoreInterface.putContext(key, context);
        }catch (Throwable t) {
            throw new PersistentException(t.getMessage());
        }
    }

    @Override
    public void expireContext(String traceId) {
        try {
            contextStoreInterface.expireContext(traceId);
        } catch (Throwable t) {
            throw new PersistentException(t.getMessage());
        }
    }

    @Override
    public void removeContext(String traceId) {
        try {
            contextStoreInterface.removeContext(traceId);
        } catch (Throwable t) {
            throw new PersistentException(t.getMessage());
        }
    }
}
