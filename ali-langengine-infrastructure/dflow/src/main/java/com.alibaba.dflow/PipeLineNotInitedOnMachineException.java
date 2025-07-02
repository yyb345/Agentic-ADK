package com.alibaba.dflow;

import org.springframework.messaging.MessagingException;

public class PipeLineNotInitedOnMachineException extends MessagingException {
   public PipeLineNotInitedOnMachineException(){
       super("DFlow of this step is not inited on the machine");
   }
}
