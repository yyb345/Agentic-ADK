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
package com.alibaba.langengine.dashscope.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.MessageConverter;
import com.alibaba.langengine.core.model.ResponseCollector;
import com.alibaba.langengine.core.model.fastchat.completion.chat.*;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionChoice;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.core.model.fastchat.service.DefaultLLMService;
import com.alibaba.langengine.dashscope.DashScopeModelName;
import com.alibaba.langengine.dashscope.model.service.openai.DashScopeOpenAIApi;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.alibaba.langengine.dashscope.DashScopeConfiguration.*;

/**
 * DashScope大模型，兼容OpenAI格式
 * <a href="https://help.aliyun.com/zh/dashscope/developer-reference/qwen-api">...</a>
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class DashScopeOpenAIChatModel extends BaseChatModel<ChatCompletionRequest> {

    /**
     * service
     */
    private DefaultLLMService<DashScopeOpenAIApi> service;

    /**
     * 指明需要调用的模型
     */
    private String model = "qwq-32b";

    /**
     * 工具选择策略，默认为auto
     * "auto": 表示由大模型进行工具策略的选择。
     * "required": 如果您希望无论输入什么问题，Function Calling 都可以进行工具调用，可以设定tool_choice参数为"required"；
     * "none": 如果您希望无论输入什么问题，Function Calling 都不会进行工具调用，可以设定tool_choice参数为"none"；
     * {"type": "function", "function": {"name": "the_function_to_call"}}
     * 如果您希望对于某一类问题，Function Calling 能够强制调用某个工具，可以设定tool_choice参数为{"type": "function", "function": {"name": "the_function_to_call"}}，其中the_function_to_call是您指定的工具函数名称。
     */
    private String toolChoice = "auto";

    /**
     * The maximum number of tokens to generate before stopping.
     * Note that our models may stop before reaching this maximum.
     * This parameter only specifies the absolute maximum number of tokens to generate.
     */
    private Integer maxTokens;

    /**
     * Only sample from the top K options for each subsequent token.
     */
    private Integer topK;

    /**
     * Use nucleus sampling.
     * In nucleus sampling, we compute the cumulative distribution over all the options for each subsequent token in decreasing probability order and cut it off once it reaches a particular probability specified by top_p.
     * You should either alter temperature or top_p, but not both.
     */
    private Double topP;

    /**
     * Amount of randomness injected into the response.
     * Defaults to 1.0. Ranges from 0.0 to 1.0. Use temperature closer to 0.0 for analytical / multiple choice, and closer to 1.0 for creative and generative tasks.
     * Note that even with temperature of 0.0, the results will not be fully deterministic.
     */
    private Double temperature;

    /**
     * Custom text sequences that will cause the model to stop generating.
     */
    private Double frequencyPenalty;

    /**
     * Number between -2.0 and 2.0.
     * Positive values penalize new tokens based on whether they appear in the text so far, increasing the model's likelihood to talk about new topics.
     */
    private Double presencePenalty;

    /**
     * 是否流式增量
     */
    private boolean sseInc = true;

    /**
     * Whether to incrementally stream the response using server-sent events.
     */
    private boolean stream = false;

    private static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/";

    public DashScopeOpenAIChatModel() {
        this(DASHSCOPE_API_KEY);
    }

    public DashScopeOpenAIChatModel(String token) {
        setModel(DashScopeModelName.QWEN_TURBO);
//        setMaxTokens(256);
//        setTopP(0.8d);
        service = new DefaultLLMService(DEFAULT_BASE_URL, Duration.ofSeconds(Long.parseLong(DASHSCOPE_API_TIMEOUT)), true, token, DashScopeOpenAIApi.class);
    }

    public ChatCompletionRequest buildRequest(List<ChatMessage> chatMessages, List<FunctionDefinition> functions, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        ChatCompletionRequest.ChatCompletionRequestBuilder builder = ChatCompletionRequest.builder();
        builder.messages(chatMessages);
        List<ToolDefinition> toolDefinitions = null;
        if(!CollectionUtils.isEmpty(functions)) {
            toolDefinitions = functions.stream().map(e -> {
                ToolDefinition toolDefinition = new ToolDefinition();
                toolDefinition.setFunction(new ToolFunction());
                toolDefinition.getFunction().setName(e.getName());
                toolDefinition.getFunction().setDescription(e.getDescription());
                toolDefinition.getFunction().setParameters(e.getParameters());
                return toolDefinition;
            }).collect(Collectors.toList());
            builder.tools(toolDefinitions);
            builder.toolChoice(getToolChoice());
        }
        log.info("tools is {}", (toolDefinitions != null ? JSON.toJSONString(toolDefinitions) : null));
        return builder.build();
    }

    public BaseMessage runRequest(ChatCompletionRequest request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        AtomicReference<BaseMessage> baseMessage = new AtomicReference<>();

        ChatCompletionResult completionResult = service.execute(service.getApi().createChatCompletion(request));
        log.info("DashScopeOpenAIChatModel completionResult is {}", JSON.toJSONString(completionResult));
        if(completionResult == null) {
            throw new RuntimeException("create chat completion result failed");
        }

        Long totalTokens = 0L;
        if(completionResult.getUsage() != null) {
            totalTokens = completionResult.getUsage().getTotalTokens();
        }
        if(completionResult.getChoices() == null) {
            throw new RuntimeException("create chat completion choices failed");
        }
        Long finalTotalTokens = totalTokens;
        completionResult.getChoices().forEach(e -> {
            ChatMessage chatMessage = e.getMessage();
            if(chatMessage != null) {
                BaseMessage message = MessageConverter.convertChatMessageToMessage(chatMessage);
                message.setTotalTokens(finalTotalTokens);
                message.setOrignalContent(JSON.toJSONString(e));
                String role = chatMessage.getRole();
                String answer = null;
                if(chatMessage.getFunctionCall() != null && chatMessage.getFunctionCall().size() > 0) {
                    if(message instanceof AIMessage) {
                        AIMessage aiMessage = (AIMessage) message;
                        aiMessage.setToolUse(true);
                        Map<String, Object> functionCallMap = new HashMap<>();
                        functionCallMap.put("function_call", chatMessage.getFunctionCall());
                        aiMessage.setAdditionalKwargs(functionCallMap);
                        answer = JSON.toJSONString(functionCallMap);
                    }
                } else if(chatMessage.getToolCalls() != null && chatMessage.getToolCalls().size() > 0) {
                    if(message instanceof AIMessage) {
                        AIMessage aiMessage = (AIMessage) message;
                        aiMessage.setToolUse(true);
                        Map<String, Object> functionCallMap = new HashMap<>();
                        functionCallMap.put("tool_calls", chatMessage.getToolCalls());
                        aiMessage.setAdditionalKwargs(functionCallMap);
                        answer = JSON.toJSONString(functionCallMap);
                    }
                } else {
                    answer = chatMessage.getContent().toString();
                }
                log.info(getModel() + " chat answer is {}", answer);
                if (message != null) {
                    baseMessage.set(message);
                }
            }
        });

        return baseMessage.get();
    }

    public BaseMessage runRequestStream(ChatCompletionRequest request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        AtomicReference<BaseMessage> baseMessage = new AtomicReference<>();
        AtomicReference<ResponseCollector> answerContent = new AtomicReference<>(new ResponseCollector(sseInc));
        AtomicReference<Object> functionCallContent = new AtomicReference<>();
        AtomicReference<ResponseCollector> functionCallNameContent = new AtomicReference<>(new ResponseCollector(sseInc));
        AtomicReference<ResponseCollector> argumentContent = new AtomicReference<>(new ResponseCollector(sseInc));
        AtomicReference<String> role = new AtomicReference<>();
        service.stream(service.getApi().createChatCompletionStream(request), ChatCompletionChunk.class)
                .doOnError(Throwable::printStackTrace)
                .blockingForEach(e -> {
                    log.info("chunk result is {}", JSON.toJSONString(e));
                    if(CollectionUtils.isEmpty(e.getChoices())) {
                        log.error("chunk result choices is empty");
                        return;
                    }

                    ChatCompletionChoice choice = e.getChoices().get(0);
                    if("stop".equals(choice.getFinishReason())
                            || "function_call".equals(choice.getFinishReason())
                            || "tool_calls".equals(choice.getFinishReason())) {
                        return;
                    }
                    ChatMessage chatMessage = choice.getMessage();
                    if(chatMessage != null) {
                        if(!StringUtils.isEmpty(chatMessage.getRole())) {
                            role.set(chatMessage.getRole());
                        }
                        chatMessage.setRole(role.get());
                        BaseMessage message = MessageConverter.convertChatMessageToMessage(chatMessage);
                        if(chatMessage != null) {
                            if(chatMessage.getContent() != null || !StringUtils.isEmpty(chatMessage.getReasoningContent())) {
                                answerContent.get().thinkCollect(chatMessage.getReasoningContent());
                                if(chatMessage.getContent() != null) {
                                    answerContent.get().collect(String.valueOf(chatMessage.getContent()));
                                }
                                String answer = answerContent.get().joining();
                                log.info(getModel() + " chat stream answer is {}", answer);
                                if (message != null) {
                                    message.setContent(answer);
                                    baseMessage.set(message);
                                    if (consumer != null) {
                                        consumer.accept(message);
                                    }
                                }
                            } else if (chatMessage.getFunctionCall() != null && chatMessage.getFunctionCall().size() > 0) {
                                Map<String, Object> functionCallMap = Maps.newHashMap();
                                if (chatMessage.getFunctionCall().get("name") != null) {
                                    functionCallNameContent.get().collect(chatMessage.getFunctionCall().get("name").toString());
                                }
                                if (chatMessage.getFunctionCall().get("arguments") != null) {
                                    argumentContent.get().collect(chatMessage.getFunctionCall().get("arguments").toString());
                                }
                                functionCallMap.put("function_call", new HashMap<String, String>() {{
                                    put("name", functionCallNameContent.get().joining());
                                    put("arguments", argumentContent.get().joining());
                                }});
                                functionCallContent.set(functionCallMap);

                                if (functionCallContent.get() != null) {
                                    if (consumer != null) {
                                        String functionCallContentString = JSON.toJSONString(functionCallContent.get());
                                        log.info(getModel() + " functionCall stream answer is {}", functionCallContentString);
                                        AIMessage aiMessage = new AIMessage();
                                        aiMessage.setAdditionalKwargs((Map<String, Object>) functionCallContent.get());
//                                        aiMessage.setContent(JSON.toJSONString(aiMessage.getAdditionalKwargs()));
                                        consumer.accept(aiMessage);
                                    }
                                }
                            } else if (chatMessage.getToolCalls() != null && chatMessage.getToolCalls().size() > 0) {
                                List<Map<String, Object>> toolCallList = new ArrayList<>();
                                for(Map<String, Object> toolCallMap : chatMessage.getToolCalls()) {
                                    if(toolCallMap.get("function") != null) {
                                        Object functionObj = toolCallMap.get("function");
                                        Map<String, Object> functionMap = (Map<String, Object>) functionObj;
                                        if (functionMap.get("name") != null) {
                                            functionCallNameContent.get().collect(functionMap.get("name").toString());
                                        }
                                        if (functionMap.get("arguments") != null) {
                                            argumentContent.get().collect(functionMap.get("arguments").toString());
                                        }

                                        Map<String, Object> toolCall = new HashMap<>();
                                        toolCall.put("function", new HashMap<String, Object>() {{
                                            put("name", functionCallNameContent.get().joining());
                                            put("arguments", argumentContent.get().joining());
                                        }});
                                        toolCall.put("type", "function");
                                        toolCall.put("id", toolCallMap.get("id") != null ? toolCallMap.get("id").toString() : "tooluse_" + UUID.randomUUID());

                                        toolCallList.add(toolCall);
                                    }
                                }

                                Map<String, Object> toolCalls = new HashMap<>();
                                toolCalls.put("tool_calls", toolCallList);
                                functionCallContent.set(toolCalls);

//                                Map<String, Object> functionCallMap = Maps.newHashMap();
//                                if (chatMessage.getFunctionCall().get("name") != null) {
//                                    functionCallNameContent.get().collect(chatMessage.getFunctionCall().get("name").toString());
//                                }
//                                if (chatMessage.getFunctionCall().get("arguments") != null) {
//                                    argumentContent.get().collect(chatMessage.getFunctionCall().get("arguments").toString());
//                                }
//                                functionCallMap.put("function_call", new HashMap<String, String>() {{
//                                    put("name", functionCallNameContent.get().joining());
//                                    put("arguments", argumentContent.get().joining());
//                                }});
//                                functionCallContent.set(functionCallMap);

                                if (functionCallContent.get() != null) {
                                    if (consumer != null) {
                                        String functionCallContentString = JSON.toJSONString(functionCallContent.get());
                                        log.info(getModel() + " functionCall stream answer is {}", functionCallContentString);
                                        AIMessage aiMessage = new AIMessage();
                                        aiMessage.setAdditionalKwargs((Map<String, Object>) functionCallContent.get());
//                                        aiMessage.setContent(JSON.toJSONString(aiMessage.getAdditionalKwargs()));
                                        consumer.accept(aiMessage);
                                    }
                                }
                            }
                        }
                    }
                });

        if(functionCallContent.get() != null) {
            AIMessage aiMessage = new AIMessage();
            aiMessage.setAdditionalKwargs((Map<String, Object>)functionCallContent.get());
            baseMessage.set(aiMessage);
            log.info("functionCallContent get is {}", JSON.toJSONString(aiMessage));
            return baseMessage.get();
        }

        String responseContent = answerContent.get().joining();

        log.info(getModel() + " final answer:" + responseContent);

        AIMessage aiMessage = new AIMessage();
        aiMessage.setContent(responseContent);
        baseMessage.set(aiMessage);

        return baseMessage.get();
    }

    /**
     * 当前是否是流式模式
     *
     * @param consumer
     * @return
     */
    public boolean currentStream(Consumer<BaseMessage> consumer) {
        return isStream() || (consumer != null);
    }
}
