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
package com.alibaba.langengine.core.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.agent.AgentOutputParser;
import com.alibaba.langengine.core.agent.structured.StructuredChatOutputParser;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.config.LangEngineConfiguration;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionDefinition;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.memory.BaseMemory;
import com.alibaba.langengine.core.outputs.context.LlmResultHolder;
import com.alibaba.langengine.core.outputs.Generation;
import com.alibaba.langengine.core.outputs.LLMResult;
import com.alibaba.langengine.core.prompt.PromptValue;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * LLM包装器应接受提示并返回一个字符串
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public abstract class BaseLLM<T extends ChatCompletionRequest> extends BaseLanguageModel<String> {

    @Override
    public LLMResult generatePrompt(List<PromptValue> prompts, List<FunctionDefinition> functions, List<String> stops, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        List<String> promptStrings = prompts.stream()
                .map(promptValue -> promptValue.toString()).collect(Collectors.toList());
        return generate(promptStrings, stops, executionContext, consumer, extraAttributes);
    }

    public LLMResult generate(List<String> prompts, List<String> stops, ExecutionContext executionContext,
                              Consumer<String> consumer, Map<String, Object> extraAttributes) {
        String promptStrings = prompts.stream().collect(Collectors.joining("\n"));
        log.info("prompts:" + promptStrings);
        log.info("stops:" + JSON.toJSONString(stops));

        if(executionContext == null) {
            executionContext = new ExecutionContext();
        }
        try {
            onLlmStart(this, prompts, executionContext, consumer);

            LLMResult llmResult;
            if (executionContext != null && executionContext.getLlmResult() != null) {
                llmResult = executionContext.getLlmResult();
            } else {
                String llmString = "";
                if (stops != null && stops.size() > 0) {
                    llmString = JSON.toJSONString(stops);
                }
                llmResult = new LLMResult();
                List<List<Generation>> generationsList = new ArrayList<>();
                if (LangEngineConfiguration.CurrentCache != null) {
                    for (int i = 0; i < prompts.size(); i++) {
                        String prompt = prompts.get(i);
                        List<Generation> cacheVal = LangEngineConfiguration.CurrentCache.get(prompt, llmString);
                        if (CollectionUtils.isEmpty(cacheVal)) {
                            cacheVal = LangEngineConfiguration.CurrentCache.get(executionContext, prompt, llmString);
                        }
                        if (cacheVal != null) {
                            generationsList.add(cacheVal);
                        }
                    }
                }

                if (generationsList.size() > 0) {
                    llmResult.setGenerations(generationsList);
                } else {
                    llmResult.setGenerations(generationsList);
                    for (String prompt : prompts) {
                        List<Generation> generations = new ArrayList<>();
                        generationsList.add(generations);
                        Generation generation = new Generation();
                        generations.add(generation);

                        //大模型调用
                        String responseContent = run(prompt, stops, consumer, extraAttributes);
                        generation.setText(responseContent);
                        generation.setGenerationInfo(LlmResultHolder.getResult());

                        if (LangEngineConfiguration.CurrentCache != null) {
                            LangEngineConfiguration.CurrentCache.update(prompt, llmString, generations);
                            LangEngineConfiguration.CurrentCache.update(executionContext, prompt, llmString, generations);
                        }
                    }
                    llmResult.setLlmOutput(LlmResultHolder.getResult());
                    llmResult.setGenerations(generationsList);
                }
            }

            onLlmEnd(this, prompts, llmResult, executionContext, consumer);

            return llmResult;
        } catch (Throwable e) {
            onLlmError(this, prompts, e, executionContext, consumer);
            throw e;
        } finally {
            LlmResultHolder.clear();
        }
    }

    public String predict(String text, List<String> stops, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        LLMResult llmResult = generate(Arrays.asList(new String[] {text}), stops, executionContext, consumer, extraAttributes);
        return llmResult.getGenerations().get(0).get(0).getText();
    }

    /**
     * 大模型运行调用
     *
     * @param prompt
     * @param stops
     * @return
     */
    public String run(String prompt, List<String> stops, Map<String, Object> extraAttributes) {
        return run(prompt, stops, null, extraAttributes);
    }

    /**
     * run
     *
     * @param prompt
     * @param stops
     * @param consumer
     * @param extraAttributes
     * @return
     */
    public String run(String prompt, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        List<ChatMessage> chatMessages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRole("user");
        chatMessage.setContent(prompt);
        chatMessages.add(chatMessage);

        log.info("model is {}", getLlmModelName());
        log.info("chatMessages is {}", (chatMessages != null ? JSON.toJSONString(chatMessages) : null));

        T request = buildRequest(chatMessages, stops, consumer, extraAttributes);
        request.setModel(request.getModel() != null ? request.getModel() : getModel());
        request.setTemperature(request.getTemperature() != null ? request.getTemperature() : getTemperature());
        request.setMaxTokens(request.getMaxTokens() != null ? request.getMaxTokens() : getMaxTokens());
        request.setTopK(request.getTopK() != null ? request.getTopK() : getTopK());
        request.setTopP(request.getTopP() != null ? request.getTopP() : getTopP());
        request.setFrequencyPenalty(request.getFrequencyPenalty() != null ? request.getFrequencyPenalty() : getFrequencyPenalty());
        request.setPresencePenalty(request.getPresencePenalty() != null ? request.getPresencePenalty() : getPresencePenalty());
        request.setMessages(!CollectionUtils.isEmpty(request.getMessages()) ? request.getMessages() : chatMessages);
        request.setStop(!CollectionUtils.isEmpty(request.getStop()) ? request.getStop() : stops);
        request.setStream(currentStream(consumer));

        log.info("request is {}", JSON.toJSONString(request));
        if(!currentStream(consumer)) {
            return runRequest(request, stops, consumer, extraAttributes);
        } else {
            return runRequestStream(request, stops, consumer, extraAttributes);
        }
    }

    /**
     * 构建请求对象
     *
     * @param chatMessages
     * @param stops
     * @param consumer
     * @param extraAttributes
     * @return
     */
    public abstract T buildRequest(List<ChatMessage> chatMessages,
                                   List<String> stops,
                                   Consumer<String> consumer,
                                   Map<String, Object> extraAttributes);

    /**
     * 非流式请求
     *
     * @param request
     * @param stops
     * @param consumer
     * @param extraAttributes
     * @return
     */
    public abstract String runRequest(T request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes);

    /**
     * 流式请求
     *
     * @param request
     * @param stops
     * @param consumer
     * @param extraAttributes
     * @return
     */
    public abstract String runRequestStream(T request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes);

    public String getStructuredChatAgentPrefixPrompt(BaseMemory memory, boolean isCH) {
        return isCH ? com.alibaba.langengine.core.agent.structured2.PromptConstants.PREFIX_CH :
            com.alibaba.langengine.core.agent.structured2.PromptConstants.PREFIX;
    }

    public String getStructuredChatAgentSuffixPrompt(BaseMemory memory, boolean isCH) {
        return (memory != null ? (isCH ? com.alibaba.langengine.core.agent.structured2.PromptConstants.SUFFIX_MEMORY_CH
            :
                com.alibaba.langengine.core.agent.structured2.PromptConstants.SUFFIX_MEMORY)
            : (isCH ? com.alibaba.langengine.core.agent.structured2.PromptConstants.SUFFIX_CH :
                com.alibaba.langengine.core.agent.structured2.PromptConstants.SUFFIX));
    }

    public String getStructuredChatAgentInstructionsPrompt(BaseMemory memory, boolean isCH) {
        return isCH ? com.alibaba.langengine.core.agent.structured2.PromptConstants.FORMAT_INSTRUCTIONS_CH :
            com.alibaba.langengine.core.agent.structured2.PromptConstants.FORMAT_INSTRUCTIONS;
    }

    public AgentOutputParser getStructuredChatOutputParser() {
        return new StructuredChatOutputParser();
    }

    public AgentOutputParser getAPIChainUrlOutputParser() {
        return new AgentOutputParser() {
            @Override
            public Object parse(String text) {
                return text;
            }
        };
    }

    public String getToolDescriptionPrompt(BaseMemory memory, boolean isCH) {
        return "{name_for_model}: {description_for_model}, args: {parameters}";
    }

//    public CompletableFuture<LLMResult> generateAsync(List<String> prompts, List<String> stops, Map<String, Object> extraAttributes) {
//        return CompletableFuture.supplyAsync(() -> generate(prompts, stops, null, null, extraAttributes));
//    }
}
