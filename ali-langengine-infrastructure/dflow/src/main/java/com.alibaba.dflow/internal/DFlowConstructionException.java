package com.alibaba.dflow.internal;

public class DFlowConstructionException extends RuntimeException{
    public DFlowConstructionException(String str){
        super(str);
    }

    public DFlowConstructionException(String str,Throwable t){
        super(str,t);
    }
}
