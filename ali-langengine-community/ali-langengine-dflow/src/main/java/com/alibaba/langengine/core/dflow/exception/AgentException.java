package com.alibaba.langengine.core.dflow.exception;

import com.alibaba.dflow.UserException;

public class AgentException extends UserException {
    public AgentException(String message){
        super(new RuntimeException(message));
    }
}
