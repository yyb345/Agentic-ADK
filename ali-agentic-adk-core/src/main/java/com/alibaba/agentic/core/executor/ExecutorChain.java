/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.core.executor;

import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/8 11:20
 */
@Slf4j
public class ExecutorChain implements CallbackChain {


    private final Executor executor;

    private final Callback[] beforeCallbacks;

    private final Callback[] afterCallbacks;

    private int pos = 0;

    private int bn = 0;

    private int an = 0;

    private boolean invoked = false;

    private Request request;

    private Flowable<Result> result;

    private Result callBackResult;


    public ExecutorChain(SystemContext systemContext, Callback[] beforeCallbacks, Callback[] afterCallbacks) {
        this.executor = systemContext.getExecutor();
        this.beforeCallbacks = beforeCallbacks;
        this.afterCallbacks = afterCallbacks;
        if (beforeCallbacks != null) {
            this.bn = beforeCallbacks.length;
        }
        if (afterCallbacks != null) {
            this.an = afterCallbacks.length;
        }
    }

    public Flowable<Result> getResult() {
        return result;
    }


    @Override
    public void execute(SystemContext systemContext, Request request, Result result) {
        this.request = request;
        this.result = Flowable.fromCallable(() -> result);
        innerExecute(systemContext);
    }


    @Override
    public void receive(SystemContext systemContext, Request request, Result result) {
        this.request = request;
        this.callBackResult = result;
        innerReceive(systemContext);
    }


    /**
     * sync调用， beforeCallbacks -> execute -> afterCallbacks
     * 其他调用， beforeCallbacks -> execute
     */
    private void innerExecute(SystemContext systemContext) {
        if (pos < bn && !invoked) {
            Callback callback = beforeCallbacks[pos++];
            try {
                this.result.subscribe(result -> callback.execute(systemContext, this.request, result, this));
            } catch (Throwable throwable) {
                this.result = Flowable.fromCallable(() -> Result.fail(throwable));
            }
            return;
        }
        if (!invoked) {
            try {
                this.result = executor.invoke(systemContext, this.request);
                log.info("executor: {}, pos:{}, invoke success", this.executor, pos);
            } catch (Throwable throwable) {
                log.error("executor: {}, pos:{}, invoke error", this.executor, pos);
                this.result = Flowable.fromCallable(() -> Result.fail(throwable));
            }
            invoked = true;
            pos = 0;
        }
        if (pos < an) {
            Callback callback = afterCallbacks[pos++];
            try {
                this.result.subscribe(result -> callback.execute(systemContext, this.request, result, this));
            } catch (Throwable throwable) {
                this.result = Flowable.fromCallable(() -> Result.fail(throwable));
            }
        }
    }

    /**
     * 异步回调，先执行afterCallbacks -> 再执行receive
     */
    private void innerReceive(SystemContext systemContext) {
        if (pos < an) {
            Callback callback = afterCallbacks[pos++];
            try {
                callback.receive(systemContext, this.request, callBackResult, this);
            } catch (Throwable throwable) {
                this.result = Flowable.fromCallable(() -> Result.fail(throwable));
            }
        }
    }
}
