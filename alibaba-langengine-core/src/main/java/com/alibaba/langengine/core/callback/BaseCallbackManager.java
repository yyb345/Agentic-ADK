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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Base callback manager that can be used to handle callbacks from LangEngine.
 *
 * @author xiaoxuan.lp
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property=JacksonUtils.PROPERTY_CLASS_NAME)
public abstract class BaseCallbackManager implements Serializable {

    /**
     * handlers
     */
    @JsonIgnore
    private List<BaseCallbackHandler> handlers = new ArrayList<>();

    /**
     * run manager
     */
    @JsonIgnore
    private RunManager runManager = new RunManager();

    public void addHandler(BaseCallbackHandler handler) {
        handlers.add(handler);
    }

    public void removeHandler(BaseCallbackHandler handler) {
        handlers.remove(handler);
    }

    /**
     * get a child callback manager
     */
    public BaseCallbackManager getChild() {
        return this;
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