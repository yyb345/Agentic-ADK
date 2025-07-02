package com.alibaba.dflow.internal;//package com.alibaba.dflow.internal;
//
//import io.reactivex.functions.Function;
//
//import com.alibaba.dflow.DFlow;
//import com.alibaba.dflow.func.AbstractClosureEnabledFunction.InvalidClosureFunctionException;
//import com.alibaba.dflow.func.ClosureDisabledFunction;
//import com.alibaba.dflow.func.ClosureEnabledConsumer;
//import com.alibaba.dflow.func.ClosureEnabledFunction2;
//
//import io.reactivex.functions.Consumer;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.SubscribableChannel;
//
//public class DFlowMetaQAdapter<R> extends DFlow<R> {
//    Logger logger = LoggerFactory.getLogger(DFlowMetaQAdapter.class);
//    private ClosureEnabledConsumer sender;
//    private ClosureDisabledFunction<Message,Boolean> filter;
//    private ClosureEnabledFunction2<Message,R> resultConvertor;
//    private SubscribableChannel channel;
//    public DFlowMetaQAdapter(
//        Consumer<ContextStack> sender,
//        SubscribableChannel channel,
//        Function<Message,Boolean> filter,
//        Function<Message,R> resultConvertor,
//        Function<Message,String> traceIdResolver) throws InvalidClosureFunctionException {
//        this.sender = new ClosureEnabledConsumer(sender,this,"-sender");
//        this.filter = new ClosureDisabledFunction<>(filter,this,"-filter");
//        this.channel = channel;
//        this.resultConvertor = new ClosureEnabledFunction2<Message, R>(resultConvertor,this,"-result");
//        channel.subscribe(message ->{
//            boolean isMine = false;
//            try {
//                isMine = filter.apply(message);
//
//            if(isMine){
//                String traceId = null;
//                traceId = new ClosureDisabledFunction<>(traceIdResolver,DFlowMetaQAdapter.this,"-trace").apply(message);
//                ContextStack context = getOrCreateCurrent(traceId);
//                try {
//                    onReturn(context,this.resultConvertor.apply(context,message));
//                } catch (Exception e) {
//                    error(context,e);
//                }
//
//            }
//            } catch (Exception e) {
//                logger.error("metaq initer failed@"+getName(),e);
//            }
//
//        });
//
//    }
//
//    @Override
//    public boolean callAfterStackBuild(ContextStack context) {
//        try {
//            sender.accept(context);
//        } catch (Exception e) {
//            error(context,e);
//            return false;
//        }
//        return true;
//    }
//
//}
