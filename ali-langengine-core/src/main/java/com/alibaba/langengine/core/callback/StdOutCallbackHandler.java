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
import lombok.extern.slf4j.Slf4j;

/**
 * StdOut callback handler
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class StdOutCallbackHandler extends BaseCallbackHandler {

    @Override
    public void onLlmStart(ExecutionContext executionContext) {
        setExecutionContext(executionContext);
        log.info("onLlmStart traceId:{}, executionContext:{}", getTraceId(), executionContext);

    }

    @Override
    public void onLlmEnd(ExecutionContext executionContext) {
        log.info("onLlmEnd traceId:{}, executionContext:{}", getTraceId(), executionContext);
    }

    @Override
    public void onLlmError(ExecutionContext executionContext) {
        log.info("onLlmError traceId:{}, executionContext:{}", getTraceId(), executionContext);
    }

    @Override
    public void onChainStart(ExecutionContext executionContext) {
        log.info("onChainStart traceId:{}, executionContext:{}", getTraceId(), executionContext);
    }

    @Override
    public void onChainEnd(ExecutionContext executionContext) {
        log.info("onChainEnd traceId:{}, executionContext:{}", getTraceId(), executionContext);
    }

    @Override
    public void onChainError(ExecutionContext executionContext) {
        log.info("onChainError traceId:{}, executionContext:{}", getTraceId(), executionContext);
    }

    @Override
    public void onToolStart(ExecutionContext executionContext) {
        log.info("onToolStart traceId:{}, executionContext:{}", getTraceId(), executionContext);
    }

    @Override
    public void onToolEnd(ExecutionContext executionContext) {
        log.info("onToolEnd traceId:{}, executionContext:{}", getTraceId(), executionContext);
    }

    @Override
    public void onToolError(ExecutionContext executionContext) {
        log.info("onToolError traceId:{}, executionContext:{}", getTraceId(), executionContext);
    }

    @Override
    public void onAgentAction(ExecutionContext executionContext) {
        log.info("onAgentAction traceId:{}, executionContext:{}", getTraceId(), executionContext);
    }

    @Override
    public void onAgentFinish(ExecutionContext executionContext) {
        log.info("onAgentFinish traceId:{}, executionContext:{}", getTraceId(), executionContext);
    }

    @Override
    public void onRetrieverStart(ExecutionContext executionContext) {
        log.info("onRetrieverStart traceId:{}, executionContext:{}", getTraceId(), executionContext);
    }

    @Override
    public void onRetrieverEnd(ExecutionContext executionContext) {
        log.info("onRetrieverEnd traceId:{}, executionContext:{}", getTraceId(), executionContext);
    }

    @Override
    public void onRetrieverError(ExecutionContext executionContext) {
        log.info("onRetrieverError traceId:{}, executionContext:{}", getTraceId(), executionContext);
    }
}
