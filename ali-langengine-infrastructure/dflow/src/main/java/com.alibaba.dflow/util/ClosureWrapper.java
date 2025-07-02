package com.alibaba.dflow.util;

import java.lang.reflect.Method;

import com.alibaba.dflow.func.ValidClosure;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

public class ClosureWrapper {

    public static <T> T wraperAsValid(T origin){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(origin.getClass());
        enhancer.setInterfaces(new Class[]{ValidClosure.class});
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy)
                throws Throwable {
                return method.invoke(origin,objects);
            }
        });
        return (T)enhancer.create();
    }
}
