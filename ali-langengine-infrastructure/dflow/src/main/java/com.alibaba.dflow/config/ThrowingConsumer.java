package com.alibaba.dflow.config;

import com.alibaba.dflow.UserException;

public interface ThrowingConsumer<T> {
    void accept(T t) throws UserException;
}