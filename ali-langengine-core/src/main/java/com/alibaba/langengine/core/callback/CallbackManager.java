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

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Callback manager that can be used to handle callbacks from langengine.
 *
 * @author xiaoxuan.lp
 */
@Data
public class CallbackManager extends BaseCallbackManager {

    public CallbackManager() {
    }

    public CallbackManager(List<BaseCallbackHandler> handlers, Map<String, Object> metadata) {
        setHandlers(handlers);
        getRunManager().setMetadata(metadata);
    }

    @Override
    public BaseCallbackManager getChild() {
        CallbackManager manager = new CallbackManager();
        manager.setHandlers(getHandlers());
        manager.setRunManager(getRunManager().getChild());
        return manager;
    }

    @Override
    public void onChainStart(ExecutionContext executionContext) {
        getRunManager().onStart();
        getHandlers().stream().forEach(baseCallbackHandler -> baseCallbackHandler.onChainStart(executionContext));
    }

    @Override
    public void onChainEnd(ExecutionContext executionContext) {
        getHandlers().stream().forEach(baseCallbackHandler -> baseCallbackHandler.onChainEnd(executionContext));
    }

    @Override
    public void onChainError(ExecutionContext executionContext) {
        getHandlers().stream().forEach(baseCallbackHandler -> baseCallbackHandler.onChainError(executionContext));
    }

    @Override
    public void onLlmStart(ExecutionContext executionContext) {
        getRunManager().onStart();
        getHandlers().stream().forEach(baseCallbackHandler -> baseCallbackHandler.onLlmStart(executionContext));
    }

    @Override
    public void onLlmEnd(ExecutionContext executionContext) {
        getHandlers().stream().forEach(baseCallbackHandler -> baseCallbackHandler.onLlmEnd(executionContext));
    }

    @Override
    public void onLlmError(ExecutionContext executionContext) {
        getHandlers().stream().forEach(baseCallbackHandler -> baseCallbackHandler.onLlmError(executionContext));
    }

    @Override
    public void onToolStart(ExecutionContext executionContext) {
        getRunManager().onStart();
        getHandlers().stream().forEach(baseCallbackHandler -> baseCallbackHandler.onToolStart(executionContext));
    }

    @Override
    public void onToolEnd(ExecutionContext executionContext) {
        getHandlers().stream().forEach(baseCallbackHandler -> baseCallbackHandler.onToolEnd(executionContext));
    }

    @Override
    public void onToolError(ExecutionContext executionContext) {
        getHandlers().stream().forEach(baseCallbackHandler -> baseCallbackHandler.onToolError(executionContext));
    }

    @Override
    public void onAgentAction(ExecutionContext executionContext) {
        getHandlers().stream().forEach(baseCallbackHandler -> baseCallbackHandler.onAgentAction(executionContext));
    }

    @Override
    public void onAgentFinish(ExecutionContext executionContext) {
        getHandlers().stream().forEach(baseCallbackHandler -> baseCallbackHandler.onAgentFinish(executionContext));
    }

    @Override
    public void onRetrieverStart(ExecutionContext executionContext) {
        getRunManager().onStart();
        getHandlers().stream().forEach(baseCallbackHandler -> baseCallbackHandler.onRetrieverStart(executionContext));
    }

    @Override
    public void onRetrieverEnd(ExecutionContext executionContext) {
        getHandlers().stream().forEach(baseCallbackHandler -> baseCallbackHandler.onRetrieverEnd(executionContext));
    }

    @Override
    public void onRetrieverError(ExecutionContext executionContext) {
        getHandlers().stream().forEach(baseCallbackHandler -> baseCallbackHandler.onRetrieverError(executionContext));
    }
}