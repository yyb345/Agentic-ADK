/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.core.callback;

import com.alibaba.langengine.core.util.JacksonUtils;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

/**
 * Base callback handler that can be used to handle callbacks from langengine.
 *
 * @author xiaoxuan.lp
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property=JacksonUtils.PROPERTY_CLASS_NAME)
public abstract class BaseCallbackHandler implements Serializable {

    static ThreadLocal<String> _traceContext = new ThreadLocal<>();
    static ThreadLocal<ExecutionContext> _executionContext = new ThreadLocal<>();

    /**
     * 获取跟踪id
     *
     * @return
     */
    public String getTraceId() {
        String traceId = getContext();
        if(traceId == null) {
            traceId = UUID.randomUUID().toString();
        }
        setContext(traceId);
        return traceId;
    }

    /**
     * 设置上下文
     */
    public static void setContext(String context) {
        if (context == null) {
            return;
        }
        _traceContext.set(context);
    }

    /**
     * 获取上下文
     */
    public static String getContext() {
        return _traceContext.get();
    }

    /**
     * 移除上下文
     */
    public static void removeContext(boolean isDeepClear) {
        String c = _traceContext.get();
        try {
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            _traceContext.set(null);
            _traceContext.remove();
        }
    }

    /**
     * 设置上下文
     */
    public static void setExecutionContext(ExecutionContext executionContext) {
        if (executionContext == null) {
            return;
        }
        _executionContext.set(executionContext);
    }

    /**
     * 获取上下文
     */
    public static ExecutionContext getExecutionContext() {
        return _executionContext.get();
    }

    /**
     * 移除上下文
     */
    public static void removeExecutionContext(boolean isDeepClear) {
        ExecutionContext c = _executionContext.get();
        try {
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            _executionContext.set(null);
            _executionContext.remove();
        }
    }

    /**
     * Run when chain starts running.
     *
     * @param executionContext
     */
    public abstract void onChainStart(ExecutionContext executionContext);

    /**
     * Run when chain ends running.
     *
     * @param executionContext
     */
    public abstract void onChainEnd(ExecutionContext executionContext);

    /**
     * Run when chain errors.
     *
     * @param executionContext
     */
    public abstract void onChainError(ExecutionContext executionContext);

    /**
     * Run when LLM starts running.
     *
     * @param executionContext
     */
    public abstract void onLlmStart(ExecutionContext executionContext);

    /**
     * Run when LLM ends running.
     *
     * @param executionContext
     */
    public abstract void onLlmEnd(ExecutionContext executionContext);

    /**
     * Run when LLM ends running.
     *
     * @param executionContext
     */
    public abstract void onLlmError(ExecutionContext executionContext);

    /**
     * Run when tool starts running.
     *
     * @param executionContext
     */
    public abstract void onToolStart(ExecutionContext executionContext);

    /**
     * Run when tool ends running.
     *
     * @param executionContext
     */
    public abstract void onToolEnd(ExecutionContext executionContext);

    /**
     * Run when tool errors.
     *
     * @param executionContext
     */
    public abstract void onToolError(ExecutionContext executionContext);

    /**
     * Run when agent action is received.
     *
     * @param executionContext
     */
    public abstract void onAgentAction(ExecutionContext executionContext);

    /**
     * Run when agent finish is received.
     *
     * @param executionContext
     */
    public abstract void onAgentFinish(ExecutionContext executionContext);

    /**
     * Run when retriever starts running
     *
     * @param executionContext
     */
    public void onRetrieverStart(ExecutionContext executionContext) {

    }

    /**
     * Run when retriever ends running
     *
     * @param executionContext
     */
    public void onRetrieverEnd(ExecutionContext executionContext) {

    }

    /**
     * Run when retriever errors
     *
     * @param executionContext
     */
    public void onRetrieverError(ExecutionContext executionContext) {

    }
}