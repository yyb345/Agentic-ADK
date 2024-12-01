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
package com.alibaba.agentmagic.framework.config;

import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.runnables.RunnableStreamCallback;
import com.alibaba.langengine.core.runnables.RunnableTraceData;
import lombok.extern.slf4j.Slf4j;

/**
 * 组件流式Callback重写，目前用于CotCallingDelegation
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class ComponentStreamCallback extends RunnableStreamCallback {

    @Override
    public void onToolError(ExecutionContext executionContext) {
        if(executionContext.getChunkConsumer() != null) {
            if(executionContext.getTraceData() == null) {
                executionContext.setTraceData(new RunnableTraceData());
            }
            RunnableTraceData traceData = executionContext.getTraceData();
            traceData.setToolName(executionContext.getTool().getName());
            traceData.setToolDesc(executionContext.getTool().getDescription());
            traceData.setStep("onToolError");
            traceData.setSuccess(false);
            if(executionContext.getThrowable() != null) {
                if(executionContext.getThrowable() instanceof AgentMagicException) {
                    AgentMagicException agentMagicException = (AgentMagicException)executionContext.getThrowable();
                    traceData.setCode(agentMagicException.getErrorCode());
                    traceData.setMessage(agentMagicException.getErrorMessage());
                } else {
                    traceData.setCode(executionContext.getThrowable().getMessage());
                    traceData.setMessage(executionContext.getThrowable().getLocalizedMessage());
                }
            }
            if(traceData.getStartTime() != null) {
                traceData.setExecuteTime(System.currentTimeMillis() - traceData.getStartTime());
            }
            executionContext.getChunkConsumer().accept(traceData);
            executionContext.setTraceData(null);
        }
    }
}
