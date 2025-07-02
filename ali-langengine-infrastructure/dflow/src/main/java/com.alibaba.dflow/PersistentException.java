package com.alibaba.dflow;

import com.alibaba.dflow.RetryException;

public class PersistentException extends RetryException {
    public PersistentException(String reason) {
        super(reason);
    }
}
