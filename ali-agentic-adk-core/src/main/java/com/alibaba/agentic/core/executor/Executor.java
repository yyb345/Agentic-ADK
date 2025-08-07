package com.alibaba.agentic.core.executor;

import io.reactivex.rxjava3.core.Flowable;

/**
 * DESCRIPTION
 * 所有具体调用的基础类
 *
 * @author baliang.smy
 * @date 2025/7/4 17:21
 */
public interface Executor {

    Flowable<Result> invoke(SystemContext systemContext, Request request) throws Throwable;

}
