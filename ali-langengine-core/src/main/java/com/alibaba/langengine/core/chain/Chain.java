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
package com.alibaba.langengine.core.chain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.alibaba.langengine.core.callback.BaseCallbackManager;
import com.alibaba.langengine.core.callback.CallbackManager;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.runnables.RunnableStreamCallback;
import com.alibaba.langengine.core.config.LangEngineConfiguration;
import com.alibaba.langengine.core.memory.BaseMemory;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.runnables.*;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.util.JacksonUtils;
import com.alibaba.langengine.core.util.MapUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;

/**
 * 所有链都应该实现的基类
 *
 * @author xiaoxuan.lp
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = JacksonUtils.PROPERTY_CLASS_NAME)
public abstract class Chain extends Runnable<RunnableInput, RunnableOutput> {

    /**
     * 大模型对象
     */
    private BaseLanguageModel llm;

    /**
     * 内存
     */
    private BaseMemory memory;

    /**
     * 是否打印
     */
    private boolean verbose;

    /**
     * 回调管理器
     */
    @JsonIgnore
    private BaseCallbackManager callbackManager;

    public BaseCallbackManager getCallbackManager() {
        if(callbackManager == null) {
            callbackManager = LangEngineConfiguration.CALLBACK_MANAGER;
        }
        return callbackManager;
    }

    public void setCallbackManager(BaseCallbackManager callbackManager) {
        if (callbackManager != null && callbackManager.getRunManager() != null) {
            callbackManager.getRunManager().setRunType("chain");
            callbackManager.getRunManager().setName(this.getClass().getSimpleName());
        }
        this.callbackManager = callbackManager;
        if (this.llm != null) {
            this.llm.setCallbackManager(callbackManager != null ? callbackManager.getChild() : null);
        }
    }

    /**
     * 对话聊天 start
     **/

    public String chat(String question) {
        return chat(question, null, null, null);
    }

    public String chat(String question, Map<String, Object> extraAttributes) {
        return chat(question, null, null, extraAttributes);
    }

    public String chat(String question, ExecutionContext executionContext) {
        return chat(question, executionContext, null, null);
    }

    public String chat(String question, ExecutionContext executionContext, Map<String, Object> extraAttributes) {
        return chat(question, executionContext, null, extraAttributes);
    }

    public String chat(String question, ExecutionContext executionContext, Consumer<String> consumer) {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("question", question);
        inputs.put("input", question); // TODO 需要动态填充
        return chat(question, executionContext, consumer, new HashMap<>());
    }

    public String chat(String question, ExecutionContext executionContext, Consumer<String> consumer,
        Map<String, Object> extraAttributes) {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("question", question);
        inputs.put("input", question); // TODO 需要动态填充
        Map<String, Object> repsonse = run(inputs, executionContext, consumer, extraAttributes);
        return (String)repsonse.get("text");
    }


    public CompletableFuture<String> chatAsync(String question) {
        return chatAsync(question, null);
    }

    public CompletableFuture<String> chatAsync(String question, ExecutionContext executionContext) {
        return CompletableFuture.supplyAsync(() -> chat(question, executionContext, null, null));
    }

    /** 对话聊天 end **/

    /**
     * chain运行 start
     **/
    public Map<String, Object> run(Map<String, Object> inputs) {
        return run(inputs, null, null, null);
    }

    public Map<String, Object> run(Map<String, Object> inputs, ExecutionContext executionContext) {
        return run(inputs, executionContext, null, null);
    }

    public Map<String, Object> run(Map<String, Object> inputs, ExecutionContext executionContext, Consumer<String> consumer) {
        return this.run(inputs, executionContext, consumer, null);
    }

    public Map<String, Object> run(Map<String, Object> inputs, Map<String, Object> extraAttributes) {
        return run(inputs, null, null, extraAttributes);
    }

    public Map<String, Object> run(Map<String, Object> inputs, ExecutionContext executionContext,
        Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return run(inputs, false, executionContext, consumer, extraAttributes);
    }

    public Map<String, Object> run(Map<String, Object> inputs, boolean returnOnlyOutputs) {
        return run(inputs, returnOnlyOutputs, null, null, null);
    }

    public Map<String, Object> run(Map<String, Object> inputs, boolean returnOnlyOutputs,
        ExecutionContext executionContext) {
        return run(inputs, returnOnlyOutputs, executionContext, null, null);
    }

    public Map<String, Object> run(Map<String, Object> inputs, boolean returnOnlyOutputs,
        ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        inputs = prepInputs(inputs);
        if (executionContext == null) {
            executionContext = new ExecutionContext();
            executionContext.setInputs(inputs);
            executionContext.setChain(this);
        }
        Map<String, Object> outputs = call(inputs, executionContext, consumer, extraAttributes);
        return prepOutputs(inputs, outputs, returnOnlyOutputs);
    }

    public CompletableFuture<Map<String, Object>> runAsync(Map<String, Object> inputs) {
        return runAsync(inputs, false, null);
    }

    public CompletableFuture<Map<String, Object>> runAsync(Map<String, Object> inputs, boolean returnOnlyOutputs) {
        return runAsync(inputs, returnOnlyOutputs, null);
    }

    public CompletableFuture<Map<String, Object>> runAsync(Map<String, Object> inputs, boolean returnOnlyOutputs,
        ExecutionContext executionContext) {
        return CompletableFuture.supplyAsync(() -> run(inputs, returnOnlyOutputs, executionContext));
    }

    /** chain运行 end **/

    /**
     * chain调用 start
     **/

    public Map<String, Object> call(Map<String, Object> inputs) {
        return call(inputs, null, null, null);
    }

    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext) {
        return call(inputs, executionContext, null, null);
    }

    public Map<String, Object> call(Map<String, Object> inputs, Consumer<String> consumer) {
        return call(inputs, null, consumer, null);
    }

    public abstract Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext,
        Consumer<String> consumer, Map<String, Object> extraAttributes);

    public CompletableFuture<Map<String, Object>> callAsync(Map<String, Object> inputs) {
        return callAsync(inputs, null, null);
    }

    public CompletableFuture<Map<String, Object>> callAsync(Map<String, Object> inputs,
        ExecutionContext executionContext, Map<String, Object> extraAttributes) {
        return CompletableFuture.supplyAsync(() -> call(inputs, executionContext, null, extraAttributes));
    }

    /** chain调用 end **/

    /**
     * 对象序列化
     *
     * @return
     */
    public String serialize() {
        try {
            return JacksonUtils.MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RunnableOutput invoke(RunnableInput runnableInput, RunnableConfig config) {
        if(runnableInput instanceof RunnableHashMap) {
            RunnableHashMap runnableHashMap = (RunnableHashMap) runnableInput;
            Map<String, Object> outputs = call(runnableHashMap);
            if(outputs != null) {
                RunnableHashMap outputMap = new RunnableHashMap();
                outputMap.putAll(outputs);
                return outputMap;
            }
        } else if(runnableInput instanceof BaseMessage) {
            RunnableHashMap runnableHashMap = new RunnableHashMap();
            runnableHashMap.put("input", ((BaseMessage) runnableInput).getContent());
            Map<String, Object> outputs = call(runnableHashMap);
            if(outputs != null) {
                RunnableHashMap outputMap = new RunnableHashMap();
                outputMap.putAll(outputs);
                return outputMap;
            }
        }
        return null;
    }

    @Override
    public RunnableOutput stream(RunnableInput runnableInput, RunnableConfig config, Consumer<Object> chunkConsumer) {
        if(runnableInput instanceof RunnableHashMap) {
            RunnableHashMap runnableHashMap = (RunnableHashMap) runnableInput;
            Consumer<String> consumer = e -> chunkConsumer.accept(e);
            Map<String, Object> outputs = call(runnableHashMap, consumer);
            if(outputs != null) {
                RunnableHashMap outputMap = new RunnableHashMap();
                outputMap.putAll(outputs);
                return outputMap;
            }
        }
        return null;
    }

    @Override
    public RunnableOutput streamLog(RunnableInput runnableInput, RunnableConfig config, Consumer<Object> chunkConsumer) {
        if(callbackManager == null) {
            setCallbackManager(new CallbackManager());
            callbackManager.addHandler(new RunnableStreamCallback());
        }
        return stream(runnableInput, config, chunkConsumer);
    }

    /**
     * chain start
     *
     * @param inputs
     * @param executionContext
     */
    public void onChainStart(Chain chain, Map<String, Object> inputs, ExecutionContext executionContext) {
        if (executionContext == null) {
            executionContext = new ExecutionContext();
        }
        if (getCallbackManager() != null) {
            if (executionContext.isContainChildChain()) {
                executionContext.setChildChain(chain);
                executionContext.setChildInputs(MapUtils.merge(executionContext.getChildInputs(), inputs));
                executionContext.setChildOutputs(null);
            } else {
                executionContext.setChain(chain);
                executionContext.setInputs(MapUtils.merge(executionContext.getInputs(), inputs));
                executionContext.setOutputs(null);
            }

            if (getCallbackManager() != null) {
                getCallbackManager().onChainStart(executionContext);
            }
        }
    }

    /**
     * chain end
     *
     * @param inputs
     * @param outputs
     * @param executionContext
     */
    public void onChainEnd(Chain chain, Map<String, Object> inputs, Map<String, Object> outputs,
        ExecutionContext executionContext) {
        if (executionContext == null) {
            executionContext = new ExecutionContext();
        }
        if (getCallbackManager() != null) {
            if (executionContext.isContainChildChain()) {
                executionContext.setChildChain(chain);
                executionContext.setChildInputs(inputs);
                if (outputs != null) {
                    executionContext.setChildOutputs(outputs);
                } else {
                    executionContext.setChildOutputs(null);
                }
            } else {
                executionContext.setChain(chain);
                executionContext.setInputs(inputs);
                if (outputs != null) {
                    executionContext.setOutputs(outputs);
                } else {
                    executionContext.setOutputs(null);
                }
            }

            if(getCallbackManager() != null) {
                getCallbackManager().onChainEnd(executionContext);
            }
        }
    }

    /**
     * chain error
     *
     * @param inputs
     * @param e
     * @param executionContext
     */
    public void onChainError(Chain chain, Map<String, Object> inputs, Throwable e, ExecutionContext executionContext) {
        if (executionContext == null) {
            executionContext = new ExecutionContext();
        }
        if (getCallbackManager() != null) {
            if (executionContext.isContainChildChain()) {
                executionContext.setChildChain(chain);
                executionContext.setChildInputs(inputs);
                executionContext.setThrowable(e);
                executionContext.setChildOutputs(null);
            } else {
                executionContext.setChain(chain);
                executionContext.setInputs(inputs);
                executionContext.setThrowable(e);
                executionContext.setOutputs(null);
            }

            if(getCallbackManager() != null) {
                getCallbackManager().onChainError(executionContext);
            }
        }
    }

    public abstract List<String> getInputKeys();

    public abstract List<String> getOutputKeys();

    private Map<String, Object> prepInputs(Map<String, Object> inputs) {
        if (memory != null) {
            Map<String, Object> external_context = memory.loadMemoryVariables(inputs);
            inputs.putAll(external_context);
        }
        return inputs;
    }

    private Map<String, Object> prepOutputs(Map<String, Object> inputs,
        Map<String, Object> outputs,
        boolean returnOnlyOutputs) {
        if (memory != null) {
            memory.saveContext(inputs, outputs);
        }
        if (returnOnlyOutputs) {
            return outputs;
        } else {
            Map<String, Object> map = new HashMap<>();
            map.putAll(inputs);
            //            if(outputs != null) {
            map.putAll(outputs);
            //            }
            return map;
        }
    }
}
