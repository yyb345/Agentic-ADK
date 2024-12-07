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

import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.indexes.BaseRetriever;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.prompt.PromptValue;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.alibaba.langengine.core.runnables.RunnableAgentExecutor.INTERMEDIATE_STEPS_KEY;

/**
 * RunnableHashMap
 *
 * @author xiaoxuan.lp
 */
@Data
public class RunnableHashMap extends HashMap<String, Object> implements RunnableInput, RunnableOutput, RunnableInterface {

    private String name;

    @Override
    public Object invoke(Object input) {
        return invoke(input, null);
    }

    @Override
    public Object invoke(Object input, RunnableConfig config) {
        return invoke(input, config, null);
    }

    private Object invoke(Object input, RunnableConfig config, Consumer chunkConsumer) {
        if(input instanceof String) {
            for (Map.Entry<String, Object> entry : this.entrySet()) {
                if(entry.getValue() instanceof RunnablePassthrough) {
                    entry.setValue(input);
                } else if(entry.getValue() instanceof BaseRetriever) {
                    BaseRetriever retriever = (BaseRetriever) entry.getValue();
                    RunnableRelevantInput runnableRelevantInput = new RunnableRelevantInput();
                    runnableRelevantInput.setQuery(input.toString());
                    runnableRelevantInput.setRecommendCount(retriever.getRecommendCount());
                    RunnableOutput runnableOutput;
                    if(chunkConsumer != null) {
                        runnableOutput = retriever.stream(runnableRelevantInput, config, chunkConsumer);
                    } else {
                        runnableOutput = retriever.invoke(runnableRelevantInput, config);
                    }
                    String context = "";
                    if(runnableOutput instanceof RunnableRelevantOutput) {
                        RunnableRelevantOutput relevantOutput = (RunnableRelevantOutput) runnableOutput;
                        context = relevantOutput.getDocuments().stream().map(Document::getPageContent).collect(Collectors.joining("\n"));
                    }
                    entry.setValue(context);
                } else if(entry.getValue() instanceof RunnableSequence) {
                    invokeRunnableSequence(input, entry, config, chunkConsumer);
                } else if(entry.getValue() instanceof RunnableLambda) {
                    entry.setValue(((RunnableLambda) entry.getValue()).invoke(null, config));
                }
            }
        } else if(input instanceof Map) {
            Map<String, Object> inputMap = (Map<String, Object>) input;
            for (Map.Entry<String, Object> entry : this.entrySet()) {
                if(entry.getValue() instanceof RunnablePassthrough) {
                    entry.setValue(input);
                } else if(entry.getValue() instanceof BaseRetriever) {
                    BaseRetriever retriever = (BaseRetriever) entry.getValue();
                    RunnableRelevantInput runnableRelevantInput = new RunnableRelevantInput();
                    // TODO 暂时写死
                    runnableRelevantInput.setQuery(inputMap.get("question").toString());
                    runnableRelevantInput.setRecommendCount(retriever.getRecommendCount());
                    RunnableOutput runnableOutput = retriever.invoke(runnableRelevantInput, config);
                    String context = "";
                    if(runnableOutput instanceof RunnableRelevantOutput) {
                        RunnableRelevantOutput relevantOutput = (RunnableRelevantOutput) runnableOutput;
                        context = relevantOutput.getDocuments().stream().map(Document::getPageContent).collect(Collectors.joining("\n"));
                    }
                    entry.setValue(context);
                } else if(entry.getValue() instanceof RunnableSequence) {
                    invokeRunnableSequence(input, entry, config, chunkConsumer);
                } else if(entry.getValue() instanceof RunnableAgentLambda) {
                    RunnableAgentLambda runnableAgentLambda = (RunnableAgentLambda)entry.getValue();
                    Object intermediateSteps = runnableAgentLambda.invoke((List<AgentAction>)inputMap.get(INTERMEDIATE_STEPS_KEY), config);
                    entry.setValue(intermediateSteps);
                }else if(entry.getValue() instanceof RunnableLambda) {
                    entry.setValue(((RunnableLambda) entry.getValue()).invoke(null, config));
                }
            }
            this.putAll(inputMap);
        } else if(input instanceof PromptValue) {
            for (Map.Entry<String, Object> entry : this.entrySet()) {
                if(entry.getValue() instanceof RunnableSequence) {
                    invokeRunnableSequence(input, entry, config, chunkConsumer);
                }
            }
        } else if(input instanceof BaseMessage) {
            // LLM/ChatModel调用返回
            for (Map.Entry<String, Object> entry : this.entrySet()) {
                if (entry.getValue() instanceof RunnablePassthrough) {
                    entry.setValue(((BaseMessage) input).getContent());
                }
            }
        }
        return this;
    }

    @Override
    public CompletableFuture invokeAsync(Object input) {
        return invokeAsync(input, null);
    }

    @Override
    public CompletableFuture invokeAsync(Object input, RunnableConfig config) {
        return CompletableFuture.supplyAsync(() -> invoke(input, config));
    }

    @Override
    public CompletableFuture streamLogAsync(Object o, RunnableConfig config, Consumer chunkConsumer) {
        return null;
    }

    @Override
    public CompletableFuture streamLogAsync(Object o, Consumer chunkConsumer) {
        return null;
    }

    @Override
    public Object streamLog(Object o, RunnableConfig config, Consumer chunkConsumer) {
        return null;
    }

    @Override
    public Object streamLog(Object o, Consumer chunkConsumer) {
        return null;
    }

    @Override
    public CompletableFuture streamAsync(Object input, RunnableConfig config, Consumer chunkConsumer) {
        return CompletableFuture.supplyAsync(() -> stream(input, config, chunkConsumer));
    }

    @Override
    public CompletableFuture streamAsync(Object input, Consumer chunkConsumer) {
        return streamAsync(input, chunkConsumer);
    }

    @Override
    public Object stream(Object input, RunnableConfig config, Consumer chunkConsumer) {
        return invoke(input, config, chunkConsumer);
    }

    @Override
    public Object stream(Object input, Consumer chunkConsumer) {
        return stream(input, null, chunkConsumer);
    }

    @Override
    public List batch(List list) {
        return null;
    }

    @Override
    public List batch(List list, RunnableConfig config) {
        return null;
    }

    @Override
    public CompletableFuture<List> batchAsync(List list) {
        return null;
    }

    @Override
    public CompletableFuture<List> batchAsync(List list, RunnableConfig config) {
        return null;
    }

    @Override
    public RunnableInterface withFallbacks(RunnableInterface[] fallbacks) {
        return null;
    }

    @Override
    public String getInputSchema() {
        return null;
//        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String getOutputSchema() {
        return null;
//        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public RunnableInterface withRetry(Integer maxAttemptNumber, Map extraAttributes) {
        return null;
    }

    @Override
    public RunnableInterface bind(Map extraAttributes) {
        return null;
    }

    private void invokeRunnableSequence(Object input, Map.Entry<String, Object> entry, RunnableConfig config, Consumer chunkConsumer) {
        RunnableSequence runnableSequence = (RunnableSequence) entry.getValue();
        RunnableOutput runnableOutput;
        if(chunkConsumer != null) {
            runnableOutput = runnableSequence.stream(input, config, chunkConsumer);
        } else {
            runnableOutput = runnableSequence.invoke(input, config);
        }
        if(runnableOutput instanceof RunnableStringVar) {
            entry.setValue(((RunnableStringVar) runnableOutput).getValue());
        } else {
            entry.setValue(runnableOutput);
        }
    }
}
