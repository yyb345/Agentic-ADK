package com.alibaba.agentic.core.executor;

import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/8 14:54
 */
@Service
@Slf4j
public class DelegationExecutor {

    private final static List<Callback> beforeCallbacks = new ArrayList<>();

    private final static List<Callback> afterCallbacks = new ArrayList<>();

    /**
     * 调用主入口
     *
     * @param request
     * @return
     */
    public static Flowable<Result> invoke(SystemContext systemContext, Request request) {
        boolean needAfterCallback = InvokeMode.SYNC.equals(systemContext.getInvokeMode());
        ExecutorChain executorChain = new ExecutorChain(systemContext, beforeCallbacks.toArray(new Callback[0]),
                needAfterCallback ? afterCallbacks.toArray(new Callback[0]) : null);
        executorChain.execute(systemContext, request, new Result());
        return executorChain.getResult();
    }

    /**
     * 异步结果处理主入口
     *
     * @param systemContext
     * @param request
     * @param result
     * @return
     */
    public static Result receive(SystemContext systemContext, Request request, Result result) {
        ExecutorChain executorChain = new ExecutorChain(systemContext, null, afterCallbacks.toArray(new Callback[0]));
        executorChain.receive(systemContext, request, result);
        return result;
    }

    @Resource
    public void setCallbacks(Optional<List<Callback>> callbacks) {
        callbacks.ifPresent(list -> {
            for (Callback callback : list) {
                if (Callback.TYPE.before.equals(callback.getType())) {
                    beforeCallbacks.add(callback);
                    continue;
                }
                if (Callback.TYPE.after.equals(callback.getType())) {
                    afterCallbacks.add(callback);
                }
            }
        });
    }

}
