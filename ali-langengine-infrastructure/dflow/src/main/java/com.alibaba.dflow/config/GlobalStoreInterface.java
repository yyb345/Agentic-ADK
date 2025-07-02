package com.alibaba.dflow.config;

import java.util.List;

import com.alibaba.dflow.internal.ContextStack;

public interface GlobalStoreInterface {
    Long incr(String key);
    Long decr(String key);
    String get(String key);
    void put(String key, String context);
    int keepAlive(String key);
    int getAlived(String key);
    List<String> getIPs(String key);
}
