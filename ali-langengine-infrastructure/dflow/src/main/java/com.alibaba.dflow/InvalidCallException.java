package com.alibaba.dflow;

import org.springframework.messaging.MessagingException;

public class InvalidCallException extends MessagingException {
   public InvalidCallException(){
       super("DFlow Call is already done");
   }
}
