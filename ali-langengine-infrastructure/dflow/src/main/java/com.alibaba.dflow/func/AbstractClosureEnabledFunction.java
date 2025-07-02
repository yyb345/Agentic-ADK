package com.alibaba.dflow.func;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.DFlowConstructionException;
import com.alibaba.dflow.internal.InternalHelper;

import io.reactivex.functions.Function;

import io.reactivex.functions.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 可以将匿名函数捕获的闭包变量保存和恢复
 */
public abstract class AbstractClosureEnabledFunction {

    public static class InvalidClosureFunctionException extends DFlowConstructionException{
        public InvalidClosureFunctionException(boolean allowclosure,String name,DFlow flow){
            super(//allowclosure ?
                //"Using not Seriliazible values, make your value implements ValidClosure or move to another function to avoid replacement or context.setAllowAnyClosure() if you confirm closure object can be restore by fastjson:"+name
            //:
            "Can not use closure in the function! May lost in distributed system @"+flow.constructingPos);
        }
    }

    public Object getRealMapper() {
        return realMapper;
    }

    static Logger logger = LoggerFactory.getLogger(AbstractClosureEnabledFunction.class);

    Object realMapper;

    public AbstractClosureEnabledFunction(Object innerMapper, DFlow flow,String subfix){
        realMapper = innerMapper;
        this.flow = flow;
        this.subfix = subfix;
    }
    public AbstractClosureEnabledFunction(Object innerMapper, DFlow flow){
        realMapper = innerMapper;
        this.flow = flow;
        this.subfix = "";
    }

    private DFlow flow;
    private String subfix;

    private String getFunckey(){
        return flow.getIDName()+subfix;
    }

    /**
     * 只有特定的closure变量是可以使用的：明确设置的ValidClosure;创建DFlow上下文的JavaBean（自行注意是单例全局无状态用法）
     * @param allowclosure
     * @throws InvalidClosureFunctionException
     */
    protected void checkClosureIsValid(boolean allowclosure) throws InvalidClosureFunctionException{
        HashMap<String, Object> c = dumpClosure(realMapper);
        if(allowclosure){
            for(Object o : c.values()){
                if(//o instanceof Serializable ||
                    o instanceof ValidClosure){
                   continue;
                }
                if(isInstanceOf(o.getClass(), flow.constructingPos)){
                    logger.warn("closure using constructing instance values(Bean property&func for example), be careful the values are stateless:"+flow.constructingPos.toString());
                    continue;
                }
                throw new InvalidClosureFunctionException(allowclosure,o.getClass().toString(),flow);
            }
        }else{
            if(c.size() > 0){
                throw new InvalidClosureFunctionException(allowclosure,"",flow);
            }
        }
    }

    //属于创建时类
    private boolean isInstanceOf(Class clazz,StackTraceElement s){
        if(clazz == null){
            return false;
        }
        if(s.getClassName().equals(clazz.getName())){
            return true;
        }
        return isInstanceOf(clazz.getSuperclass(),s);
    }


    public void before(ContextStack c){
        //HashMap<String,Object> data = InternalHelper.getClosure(c,getFunckey());
        //loadClosure(data);
    }

    public void after(ContextStack c){
        //InternalHelper.putClosure(c,dumpClosure(),getFunckey());
    }

    public static boolean valid(Object realMapper){
        HashMap<String,Object> c = dumpClosure(realMapper);
        for(Object o : c.values()){
            if(//o instanceof Serializable ||
                o instanceof ValidClosure){
                continue;
            }
            return false;
        }
        return true;
    }

    public static HashMap<String,Object> dumpClosure(Object realMapper){
        HashMap<String,Object> data = new HashMap<>();
        for(Field field: realMapper.getClass().getDeclaredFields()){
            try {
                field.setAccessible(true);
                data.put(field.getName(),field.get(realMapper));
            } catch (IllegalAccessException e) {
                logger.error("dumpClosure failed",e);
            }
        }
        return data;
    }

    private void loadClosure(HashMap<String,Object> data){
        if(data == null){
            return;
        }
        for(Field field: realMapper.getClass().getDeclaredFields()){
            try {
                //如果是可以放在closure中的变量，不处理
                if(data.get(field.getName()) instanceof ValidClosure){
                    continue;
                }
                field.setAccessible(true);
                field.set(realMapper, data.get(field.getName()));
            } catch (IllegalAccessException e) {
                logger.error("dumpClosure failed",e);
            }
        }

    }
}
