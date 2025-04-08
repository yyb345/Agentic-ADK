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
package com.alibaba.langengine.core.runnables;

import com.alibaba.langengine.core.callback.BaseCallbackHandler;
import com.alibaba.langengine.core.callback.ExecutionContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * RunnableStream callback
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class RunnableStreamCallback extends BaseCallbackHandler {

    @Override
    public void onLlmStart(ExecutionContext executionContext) {
        setExecutionContext(executionContext);
        log.info("onLlmStart callback");
//        log.info("onLlmStart traceId:{}, executionContext:{}", getTraceId(), executionContext);
        if(executionContext.getChunkConsumer() != null) {
//            Map<String, Object> steps = new HashMap<>();
//            steps.put("llm", executionContext.getLlm().getLlmFamilyName());
//            steps.put("traceId", getTraceId());
//            steps.put("status", "onLlmStart");
//            executionContext.getChunkConsumer().accept(JSON.toJSONString(steps));

            executionContext.setTraceData(new RunnableTraceData());
            RunnableTraceData traceData = executionContext.getTraceData();
            traceData.setModelType(executionContext.getLlm().getLlmFamilyName());
            traceData.setModelName(executionContext.getLlm().getLlmModelName());
            traceData.setStep("onLlmStart");
            traceData.setStartTime(System.currentTimeMillis());
            executionContext.getChunkConsumer().accept(traceData);
        }
    }

    @Override
    public void onLlmEnd(ExecutionContext executionContext) {
        log.info("onLlmEnd callback");
//        log.info("onLlmEnd traceId:{}, executionContext:{}", getTraceId(), executionContext);
        if(executionContext.getChunkConsumer() != null) {
//            Map<String, Object> steps = new HashMap<>();
//            steps.put("llm", executionContext.getLlm().getLlmFamilyName());
//            steps.put("traceId", getTraceId());
//            steps.put("status", "onLlmEnd");
//            executionContext.getChunkConsumer().accept(JSON.toJSONString(steps));

            if(executionContext.getTraceData() == null) {
                executionContext.setTraceData(new RunnableTraceData());
            }
            RunnableTraceData traceData = executionContext.getTraceData();
            traceData.setModelType(executionContext.getLlm().getLlmFamilyName());
            traceData.setModelName(executionContext.getLlm().getLlmModelName());
            traceData.setStep("onLlmEnd");
            if(traceData.getStartTime() != null) {
                traceData.setExecuteTime(System.currentTimeMillis() - traceData.getStartTime());
            }
            executionContext.getChunkConsumer().accept(traceData);
            executionContext.setTraceData(null);
        }
    }

    @Override
    public void onLlmError(ExecutionContext executionContext) {
        log.info("onLlmError callback");
//        log.info("onLlmError traceId:{}, executionContext:{}", getTraceId(), executionContext);

        if(executionContext.getChunkConsumer() != null) {
            if(executionContext.getTraceData() == null) {
                executionContext.setTraceData(new RunnableTraceData());
            }
            RunnableTraceData traceData = executionContext.getTraceData();
            traceData.setModelType(executionContext.getLlm().getLlmFamilyName());
            traceData.setModelName(executionContext.getLlm().getLlmModelName());
            traceData.setStep("onLlmError");
            traceData.setSuccess(false);
            if(executionContext.getThrowable() != null) {
                traceData.setCode(executionContext.getThrowable().getMessage());
                traceData.setMessage(executionContext.getThrowable().getLocalizedMessage());
            }
            if (traceData.getStartTime() != null) {
                traceData.setExecuteTime(System.currentTimeMillis() - traceData.getStartTime());
            }
            executionContext.getChunkConsumer().accept(traceData);
            executionContext.setTraceData(null);
        }
    }

    @Override
    public void onChainStart(ExecutionContext executionContext) {
//        log.info("onChainStart traceId:{}, executionContext:{}", getTraceId(), executionContext);
//        if(executionContext.getChunkConsumer() != null) {
//            Map<String, Object> steps = new HashMap<>();
//            steps.put("llm", executionContext.getLlm().getLlmFamilyName());
//            steps.put("traceId", getTraceId());
//            steps.put("status", "onChainStart");
//            executionContext.getChunkConsumer().accept(JSON.toJSONString(steps));
//        }
    }

    @Override
    public void onChainEnd(ExecutionContext executionContext) {
//        log.info("onChainEnd traceId:{}, executionContext:{}", getTraceId(), executionContext);
//        if(executionContext.getChunkConsumer() != null) {
//            Map<String, Object> steps = new HashMap<>();
//            steps.put("llm", executionContext.getLlm().getLlmFamilyName());
//            steps.put("traceId", getTraceId());
//            steps.put("status", "onChainEnd");
//            executionContext.getChunkConsumer().accept(JSON.toJSONString(steps));
//        }
    }

    @Override
    public void onChainError(ExecutionContext executionContext) {
//        log.info("onChainError traceId:{}, executionContext:{}", getTraceId(), executionContext);
//        if(executionContext.getChunkConsumer() != null) {
//            Map<String, Object> steps = new HashMap<>();
//            steps.put("llm", executionContext.getLlm().getLlmFamilyName());
//            steps.put("traceId", getTraceId());
//            steps.put("status", "onChainError");
//            executionContext.getChunkConsumer().accept(JSON.toJSONString(steps));
//        }
    }

    @Override
    public void onToolStart(ExecutionContext executionContext) {
        log.info("onToolStart callback");
//        log.info("onToolStart traceId:{}, executionContext:{}", getTraceId(), executionContext);
        if(executionContext.getChunkConsumer() != null) {
//            Map<String, Object> steps = new HashMap<>();
//            steps.put("tool", executionContext.getTool().getName());
//            steps.put("traceId", getTraceId());
//            steps.put("status", "onToolStart");
//            executionContext.getChunkConsumer().accept(JSON.toJSONString(steps));

            executionContext.setTraceData(new RunnableTraceData());
            RunnableTraceData traceData = executionContext.getTraceData();
            traceData.setToolName(executionContext.getTool().getName());
            traceData.setToolDesc(executionContext.getTool().getDescription());
            traceData.setStep("onToolStart");
            traceData.setStartTime(System.currentTimeMillis());
            executionContext.getChunkConsumer().accept(traceData);
        }
    }

    @Override
    public void onToolEnd(ExecutionContext executionContext) {
        log.info("onToolEnd callback");
//        log.info("onToolEnd traceId:{}, executionContext:{}", getTraceId(), executionContext);
        if(executionContext.getChunkConsumer() != null) {
//            Map<String, Object> steps = new HashMap<>();
//            steps.put("tool", executionContext.getTool().getName());
//            steps.put("traceId", getTraceId());
//            steps.put("status", "onToolEnd");
//            executionContext.getChunkConsumer().accept(JSON.toJSONString(steps));

            if(executionContext.getTraceData() == null) {
                executionContext.setTraceData(new RunnableTraceData());
            }
            RunnableTraceData traceData = executionContext.getTraceData();
            traceData.setToolName(executionContext.getTool().getName());
            traceData.setToolDesc(executionContext.getTool().getDescription());
            traceData.setToolResult(executionContext.getToolExecuteResult() != null ? executionContext.getToolExecuteResult().getOutput() : null);
            traceData.setStep("onToolEnd");
            if(traceData.getStartTime() != null) {
                traceData.setExecuteTime(System.currentTimeMillis() - traceData.getStartTime());
            }
            executionContext.getChunkConsumer().accept(traceData);
            executionContext.setTraceData(null);
        }
    }

    @Override
    public void onToolError(ExecutionContext executionContext) {
        log.info("onToolError callback");
//        log.info("onToolError traceId:{}, executionContext:{}", getTraceId(), executionContext);
        if(executionContext.getChunkConsumer() != null) {
//            Map<String, Object> steps = new HashMap<>();
//            steps.put("tool", executionContext.getTool().getName());
//            steps.put("traceId", getTraceId());
//            steps.put("status", "onToolError");
//            executionContext.getChunkConsumer().accept(JSON.toJSONString(steps));

            if(executionContext.getTraceData() == null) {
                executionContext.setTraceData(new RunnableTraceData());
            }
            RunnableTraceData traceData = executionContext.getTraceData();
            traceData.setToolName(executionContext.getTool().getName());
            traceData.setToolDesc(executionContext.getTool().getDescription());
            traceData.setStep("onToolError");
            traceData.setSuccess(false);
            if(executionContext.getThrowable() != null) {
                traceData.setCode(executionContext.getThrowable().getMessage());
                traceData.setMessage(executionContext.getThrowable().getLocalizedMessage());
            }
            if(traceData.getStartTime() != null) {
                traceData.setExecuteTime(System.currentTimeMillis() - traceData.getStartTime());
            }
            executionContext.getChunkConsumer().accept(traceData);
            executionContext.setTraceData(null);
        }
    }

    @Override
    public void onAgentAction(ExecutionContext executionContext) {
//        log.info("onAgentAction traceId:{}, executionContext:{}", getTraceId(), executionContext);
//        if(executionContext.getChunkConsumer() != null) {
//            Map<String, Object> steps = new HashMap<>();
//            steps.put("agentStep", JSON.toJSONString(executionContext.getAgentAction()));
//            steps.put("traceId", getTraceId());
//            steps.put("status", "onAgentAction");
//            executionContext.getChunkConsumer().accept(JSON.toJSONString(steps));
//        }
    }

    @Override
    public void onAgentFinish(ExecutionContext executionContext) {
//        log.info("onAgentFinish traceId:{}, executionContext:{}", getTraceId(), executionContext);
//        if(executionContext.getChunkConsumer() != null) {
//            Map<String, Object> steps = new HashMap<>();
//            steps.put("agentStep", JSON.toJSONString(executionContext.getAgentFinish()));
//            steps.put("traceId", getTraceId());
//            steps.put("status", "onAgentFinish");
//            executionContext.getChunkConsumer().accept(JSON.toJSONString(steps));
//        }
    }

    @Override
    public void onRetrieverStart(ExecutionContext executionContext) {
//        log.info("onRetrieverStart traceId:{}, executionContext:{}", getTraceId(), executionContext);
    }

    @Override
    public void onRetrieverEnd(ExecutionContext executionContext) {
//        log.info("onRetrieverEnd traceId:{}, executionContext:{}", getTraceId(), executionContext);
    }

    @Override
    public void onRetrieverError(ExecutionContext executionContext) {
//        log.info("onRetrieverError traceId:{}, executionContext:{}", getTraceId(), executionContext);
    }
}
