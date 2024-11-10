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

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.outputs.Generation;
import com.alibaba.langengine.core.outputs.LLMResult;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.prompt.BasePromptTemplate;
import com.alibaba.langengine.core.prompt.PromptValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.alibaba.langengine.core.util.Constants.CALLBACK_ERROR_KEY;

/**
 * 接以针对 LLM 运行查询
 *
 * @author xiaoxuan.lp
 */
@Data
public class LLMChain extends Chain {

    /**
     * 提示对象
     */
    private BasePromptTemplate prompt;

    public void setLlm(BaseLanguageModel llm) {
        super.setLlm(llm);
//        if(getCallbackManager() != null) {
//            getLlm().setCallbackManager(getCallbackManager().getChild());
//        }
    }

    private String outputKey = "text";

    public static final List<String> STOP_LIST = Arrays.asList(new String[] { "Human:", "AI:" });

    @Override
    public List<String> getInputKeys() {
        if(prompt == null) {
            return null;
        }
        return prompt.getInputVariables();
    }

    @JsonIgnore
    @Override
    public List<String> getOutputKeys() {
        return Arrays.asList(new String[] { outputKey });
    }

    @Override
    public Map<String, Object> call(Map<String, Object> inputs,
                                    ExecutionContext executionContext,
                                    Consumer<String> consumer,
                                    Map<String, Object> extraAttributes) {
        try {
            if (executionContext == null) {
                executionContext = new ExecutionContext();
            }
            onChainStart(this, inputs, executionContext);

            Map<String, Object> outputs;
            if(executionContext != null
                    && executionContext.isContainChildChain()
                    && executionContext.getChildOutputs() != null) {
                outputs = executionContext.getChildOutputs();
            } else if(executionContext != null
                    && executionContext.getOutputs() != null) {
                outputs = executionContext.getOutputs();
            } else {
                outputs = new HashMap<>();
                LLMResult llmResult = generate(Arrays.asList(new Map[]{inputs}), executionContext, consumer, extraAttributes);
                if (llmResult.getGenerations().size() > 0) {
                    List<Generation> generations = llmResult.getGenerations().get(0);
                    if (generations.size() > 0) {
                        String text = generations.get(0).getText();
                        outputs.put(outputKey, text);
                    }
                }
            }

            onChainEnd(this, inputs, outputs, executionContext);

            return outputs;
        } catch (Throwable e) {
            onChainError(this, inputs, e, executionContext);

            if(getCallbackManager() != null) {
                Map<String, Object> outputs = new HashMap<>();
                // 如果直接放Context，会导致序列化失败，建议放具体的错误原因，同时打上日志
                outputs.put(CALLBACK_ERROR_KEY, e);
                return outputs;
            }
            throw e;
        }
    }

    public Map<String, Object> predict(Map<String, Object> inputs) {
        return run(inputs, null, null);
    }

    public Map<String, Object> predict(Map<String, Object> inputs, Map<String, Object> extraAttributes) {
        return predict(inputs, null, extraAttributes);
    }

    public Map<String, Object> predict(Map<String, Object> inputs, ExecutionContext executionContext, Map<String, Object> extraAttributes) {
        return predict(inputs, null, executionContext, extraAttributes);
    }

    public Map<String, Object> predict(Map<String, Object> inputs, Consumer<String> consumer, ExecutionContext executionContext, Map<String, Object> extraAttributes) {
        return run(inputs, executionContext, consumer, extraAttributes);
    }

    public CompletableFuture<Map<String, Object>> predictAsync(Map<String, Object> inputs) {
        return predictAsync(inputs, null);
    }

    public CompletableFuture<Map<String, Object>> predictAsync(Map<String, Object> inputs, Map<String, Object> extraAttributes) {
        return CompletableFuture.supplyAsync(() -> run(inputs, extraAttributes));
    }

    public CompletableFuture<Map<String, Object>> predictAsync(Map<String, Object> inputs, ExecutionContext executionContext, Map<String, Object> extraAttributes) {
        return CompletableFuture.supplyAsync(() -> run(inputs, executionContext, null, extraAttributes));
    }

    public Object predictAndParse(Map<String, Object> inputs, Map<String, Object> extraAttributes) {
        Map<String, Object> result = predict(inputs, extraAttributes);
        if(prompt.getOutputParser() != null) {
            return prompt.getOutputParser().parse(result.get(outputKey).toString());
        }
        return result;
    }

    public String predictToOutput(Map<String, Object> inputs, ExecutionContext executionContext) {
        return predictToOutput(inputs, executionContext, null);
    }

    public String predictToOutput(Map<String, Object> inputs, ExecutionContext executionContext, Map<String, Object> extraAttributes) {
        return predictToOutput(inputs, null, executionContext, null);
    }

    public String predictToOutput(Map<String, Object> inputs, Consumer<String> consumer, ExecutionContext executionContext, Map<String, Object> extraAttributes) {
        return (String) predict(inputs, consumer, executionContext, extraAttributes).get(outputKey);
    }

    public LLMResult generate(List<Map<String, Object>> inputs, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        List<PromptValue> promptValues = inputs.stream()
                .map(input -> prompt.formatPrompt(input)).collect(Collectors.toList());
        Map<String, Object> input = inputs.get(0);
        List<String> stops;
        if(input.containsKey("stop")) {
            if(input.get("stop") instanceof List) {
                stops = (List)input.get("stop");
            } else {
                stops = Arrays.asList(new String[]{input.get("stop").toString()});
            }
        } else {
            stops = STOP_LIST;
        }
        if(executionContext != null) {
            executionContext.setExecutionType("llm");
        }
        return getLlm().generatePrompt(promptValues, stops, executionContext, consumer, extraAttributes);
    }
}
