package com.alibaba.dflow;

public class RetryException extends RuntimeException {
   public RetryException(String reason){
       super(reason);
   }
}
