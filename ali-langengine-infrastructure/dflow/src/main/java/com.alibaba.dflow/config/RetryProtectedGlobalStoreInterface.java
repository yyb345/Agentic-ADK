package com.alibaba.dflow.config;

import java.util.List;

import com.alibaba.dflow.PersistentException;
import com.alibaba.dflow.RetryException;

public class RetryProtectedGlobalStoreInterface implements GlobalStoreInterface {
    GlobalStoreInterface globalStoreInterface;
    public RetryProtectedGlobalStoreInterface(GlobalStoreInterface globalStoreInterface) {
        this.globalStoreInterface = globalStoreInterface;
    }
    @Override
    public Long incr(String key) {
        try {
            return globalStoreInterface.incr(key);
        }catch (Throwable t) {
            throw new PersistentException(t.getMessage());
        }
    }

    @Override
    public Long decr(String key) {
        try {
            return globalStoreInterface.decr(key);
        }catch (Throwable t) {
            throw new PersistentException(t.getMessage());
        }
    }

    @Override
    public String get(String key) {
        try {
            return globalStoreInterface.get(key);
        }catch (Throwable t) {
            throw new PersistentException(t.getMessage());
        }
    }

    @Override
    public void put(String key, String context) {
        try {
            globalStoreInterface.put(key, context);
        } catch (Throwable t) {
            throw new PersistentException(t.getMessage());
        }
    }

    @Override
    public int keepAlive(String key) {
        try {
            return globalStoreInterface.keepAlive(key);
        }catch (Throwable t) {
            throw new PersistentException(t.getMessage());
        }
    }

    @Override
    public int getAlived(String key) {
        try {
            return globalStoreInterface.getAlived(key);
        }catch (Throwable t) {
            throw new PersistentException(t.getMessage());
        }
    }

    @Override
    public List<String> getIPs(String key) {
        try {
            return globalStoreInterface.getIPs(key);
        }catch (Throwable t) {
            throw new PersistentException(t.getMessage());
        }
    }
}
