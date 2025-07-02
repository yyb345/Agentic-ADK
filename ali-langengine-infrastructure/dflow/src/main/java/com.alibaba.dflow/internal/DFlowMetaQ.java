package com.alibaba.dflow.internal;//package com.alibaba.dflow.internal;
//
//import com.alibaba.dflow.DFlow;
//import com.alibaba.dflow.func.AbstractClosureEnabledFunction.InvalidClosureFunctionException;
//import com.alibaba.dflow.func.ClosureDisabledFunction;
//import com.alibaba.dflow.func.ClosureEnabledConsumer2;
//import com.alibaba.dflow.func.ClosureEnabledFunction2;
//
//import io.reactivex.functions.Consumer;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.SubscribableChannel;
//
//public class DFlowMetaQ<T, R> extends AbstractDFlowWithUpstream<T, R> {
//    Logger logger = LoggerFactory.getLogger(DFlowMetaQ.class);
//    private ClosureEnabledConsumer2<T> sender;
//    private ClosureDisabledFunction<Message,Boolean> filter;
//    private ClosureEnabledFunction2<Message,R> resultConvertor;
//    private SubscribableChannel channel;
//    private ClosureDisabledFunction<Message,String> traceIdResolver;
//    public DFlowMetaQ(DFlow<T> source,
//        Consumer<ContextStack> sender,
//        SubscribableChannel channel,
//        io.reactivex.functions.Function<Message,Boolean> filter,
//        io.reactivex.functions.Function<Message,R> resultConvertor,
//        io.reactivex.functions.Function<Message,String> traceIdResolver) throws InvalidClosureFunctionException {
//        super(source);
//        this.sender = new ClosureEnabledConsumer2(sender,this,"-sender");
//        this.filter = new ClosureDisabledFunction<>(filter,this,"-filter");
//        this.channel = channel;
//        this.traceIdResolver = new ClosureDisabledFunction<>(traceIdResolver,this,"-trace");
//        this.resultConvertor = new ClosureEnabledFunction2<Message, R>(resultConvertor,this,"-result");
//        channel.subscribe(message ->{
//            try {
//                if(filter.apply(message)){
//
//                    String traceId = this.traceIdResolver.apply(message);
//                    ContextStack stack = getOrCreateCurrent(traceId);
//                    try {
//                        onReturn(stack, this.resultConvertor.apply(stack,message));
//                    }catch (Exception e){
//                        error(stack,e);
//                    }
//                }
//            } catch (Exception e) {
//                logger.error("Filter failed @"+getName(),e);
//            }
//        });
//
//    }
//
//    @Override
//    protected boolean actualCall(ContextStack contextStack, T r) {
//        try {
//            sender.accept(contextStack,r);
//        } catch (Exception e) {
//            error(contextStack,e);
//            return false;
//        }
//        return true;
//    }
//
//}
