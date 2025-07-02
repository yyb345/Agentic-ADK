package com.alibaba.dflow;

/**
 * 专供子流程结束用
 */
public class EndException extends Exception {
   public EndException(String reason){
       super(reason);
   }
}
