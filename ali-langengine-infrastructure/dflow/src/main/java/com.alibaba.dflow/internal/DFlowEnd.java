package com.alibaba.dflow.internal;//package com.alibaba.dflow;
//
//
//public class DFlowEnd<T> extends AbstractDFlowWithUpstream<T,T>{
//    public DFlowEnd(DFlow<T> source){
//        super(source);
//    }
//
//    @Override
//    void subscribe(Subscriber channel) {
//        getSource().subscribe(new Subscriber<T>(channel,getName()) {
//            @Override
//            void onNext(ContextStack context, T t) {
//                channel.onNext( context,t);
//            }
//
//            @Override
//            void onError(ContextStack context,Throwable t) {
//                channel.onError(context, t);
//            }
//        });
//    }
//}
