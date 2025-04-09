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

import com.alibaba.langengine.core.agent.AgentOutputParser;
import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.memory.BaseMemory;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.model.ResponseCollector;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionDefinition;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ToolDefinition;
import com.alibaba.langengine.dashscope.DashScopeModelName;
import com.alibaba.langengine.dashscope.model.agent.DashScopeAPIChainUrlOutputParser;
import com.alibaba.langengine.dashscope.model.agent.DashScopePromptConstants;
import com.alibaba.langengine.dashscope.model.agent.DashScopeStructuredChatOutputParser;
import com.alibaba.langengine.dashscope.model.completion.*;
import com.alibaba.langengine.dashscope.model.service.DashScopeService;
import io.reactivex.Flowable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.alibaba.langengine.dashscope.DashScopeConfiguration.*;

/**
 * DashScope ChatModel大模型，用到chatMessage方式
 * https://help.aliyun.com/zh/dashscope/developer-reference/qwen-api
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class DashScopeChatModel extends BaseChatModel<CompletionRequest> {

    /**
     * service
     */
    private DashScopeService service;

    /**
     * 指明需要调用的模型
     */
    private String model = "qwen-turbo"; // qwen-turbo，qwen-plus，qwen-vl-plus，qwen-vl-max

    /**
     * 是否启动 web 搜索功能，默认为false。
     */
    private boolean enableSearch = false;

    /**
     * 接口输入和输出的信息是否通过绿网过滤，默认不调用绿网。
     */
    private boolean dataInspection = false;

    /**
     * 是否流式增量
     */
    private boolean sseInc = false;

    private static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/";

    public DashScopeChatModel() {
        this(DASHSCOPE_API_KEY);
    }

    public DashScopeChatModel(String token) {
        setModel(DashScopeModelName.QWEN_TURBO);
        setMaxTokens(256);
        setTopP(0.8d);
        String serverUrl = !StringUtils.isEmpty(DASHSCOPE_SERVER_URL) ? DASHSCOPE_SERVER_URL : DEFAULT_BASE_URL;
        service = new DashScopeService(serverUrl, Duration.ofSeconds(Long.parseLong(DASHSCOPE_API_TIMEOUT)), true, token);
    }

    @Override
    public CompletionRequest buildRequest(List<ChatMessage> chatMessages, List<FunctionDefinition> functions, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        Map<String, Object> input = new HashMap<>();
        List<ChatMessage> executeReadyMessage = new ArrayList<>(chatMessages);
        input.put("messages", executeReadyMessage);
        CompletionRequest.CompletionRequestBuilder builder = CompletionRequest.builder()
                .input(input);
        Map<String, Object> parameters = new HashMap<>();
        if (getTopP() != null) {
            parameters.put("top_p", getTopP());
        }
        if (getMaxTokens() != null) {
            parameters.put("max_tokens", getMaxTokens());
        }
        if (enableSearch) {
            parameters.put("enable_search", enableSearch);
        }
        if (dataInspection) {
            parameters.put("dataInspection", "enable");
        }
        if(!CollectionUtils.isEmpty(functions)) {
            parameters.put("functions", functions);
        }
        parameters.put("result_format", "message");
        if (extraAttributes!=null && Objects.nonNull(extraAttributes.get("functions"))) {
            List<ToolDefinition> toolDefinitionList = (List<ToolDefinition>)extraAttributes.get("functions");
            parameters.put("tools",toolDefinitionList);
        }
        builder.parameters(parameters);

        return builder.build();
    }

    @Override
    public BaseMessage runRequest(CompletionRequest request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        AtomicReference<CompletionResult> resultAtomicReference = new AtomicReference<>(new CompletionResult());
        AtomicReference<ResponseCollector> answerContent = new AtomicReference<>(new ResponseCollector(sseInc));
        AtomicReference<BaseMessage> baseMessage = new AtomicReference<>();
        CompletionResult completionResult = getModel().equals("qwen-vl-plus")
                || getModel().equals("qwen-vl-max") ?
                service.createMultimodalGeneration(request) : service.createCompletion(request);
        resultAtomicReference.set(completionResult);

        if(getModel().equals("qwen-vl-plus")
                || getModel().equals("qwen-vl-max")){
            completionResult.getOutput().getChoices().forEach(e -> {
                com.alibaba.langengine.dashscope.model.completion.ChatMessage chatMessage = e.getMessage();
                if (chatMessage != null) {
                    BaseMessage message = convertChatMessageToMessage(chatMessage);
                    String role = chatMessage.getRole();
                    String answer = chatMessage.getContent().toString();
                    log.warn(model + " chat answer:{},{}", role, answer);
                    if (message != null) {
                        baseMessage.set(message);
                    }
                }
            });
        } else {
            completionResult.getOutput().getChoices().forEach(e -> {

                com.alibaba.langengine.dashscope.model.completion.ChatMessage chatMessage = e.getMessage();
                if (chatMessage != null) {
                    BaseMessage message = convertChatMessageToMessage(chatMessage);
                    String role = chatMessage.getRole();
                    String answer = chatMessage.getContent().toString();
                    List<ToolCalls> tool_calls = chatMessage.getTool_calls();
                    log.warn(model + " chat answer:{},{},{}", role, answer,tool_calls);
                    if (message != null) {
                        baseMessage.set(message);
                    }
                }
            });
        }

        return baseMessage.get();
    }

    @Override
    public BaseMessage runRequestStream(CompletionRequest request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        AtomicReference<CompletionResult> resultAtomicReference = new AtomicReference<>(new CompletionResult());
        AtomicReference<ResponseCollector> answerContent = new AtomicReference<>(new ResponseCollector(sseInc));
        AtomicReference<BaseMessage> baseMessage = new AtomicReference<>();

        if(getModel().equals("qwen-vl-plus") || getModel().equals("qwen-vl-max")) {
            Flowable<CompletionChunk> flowable = service.streamMultimodalGeneration(request);
            flowable.doOnError(Throwable::printStackTrace)
                    .blockingForEach(e -> {
                        com.alibaba.langengine.dashscope.model.completion.ChatMessage chatMessage = e.getOutput().getChoices().get(0).getMessage();
                        if(chatMessage != null) {
                            BaseMessage message = convertChatMessageToMessage(chatMessage);
                            String role = chatMessage.getRole();
                            String answer = chatMessage.getContent().toString();
                            log.warn(model + " chat stream answer:{},{}", role, answer);
                            if(message != null) {
                                answerContent.get().collect(message.getContent());
                                String response = answerContent.get().joining();
                                message.setContent(response);
                                baseMessage.set(message);
                                if(consumer != null) {
                                    consumer.accept(message);
                                }
                            }
                        }
                    });
        } else {
            Flowable<CompletionChunk> flowable = service.streamCompletion(request);
            flowable.doOnError(Throwable::printStackTrace)
                    .blockingForEach(res -> {
                        res.getOutput().getChoices().forEach(e -> {
                            com.alibaba.langengine.dashscope.model.completion.ChatMessage chatMessage = e.getMessage();
                            if (chatMessage != null) {
                                BaseMessage message = convertChatMessageToMessage(chatMessage);
                                String role = chatMessage.getRole();
                                String answer = chatMessage.getContent().toString();
                                log.warn(model + " stream answer:{},{}", role, answer);
                                if (message != null) {
                                    answerContent.get().collect(message.getContent());
                                    String response = answerContent.get().joining();
                                    message.setContent(response);
                                    baseMessage.set(message);
                                    if(consumer != null) {
                                        consumer.accept(message);
                                    }
                                }
                            }
                        });
                    });
        }

        return baseMessage.get();
    }

    private static BaseMessage convertChatMessageToMessage(com.alibaba.langengine.dashscope.model.completion.ChatMessage chatMessage) {
        if(chatMessage.getRole().equals("assistant")) {
            AIMessage aiMessage = new AIMessage();
            if(chatMessage.getContent() != null) {
                if(chatMessage.getContent() instanceof List) {
                    List list = (List)chatMessage.getContent();
                    if(list.size() > 0 && list.get(0) instanceof ChatMessageContent) {
                        aiMessage.setContent(((ChatMessageContent)list.get(0)).getText());
                    }
                    else if(list.size() > 0 && list.get(0) instanceof Map) {
                        aiMessage.setContent((String) ((Map) list.get(0)).get("text"));
                    }
                } else if(chatMessage.getContent() instanceof String) {
                    aiMessage.setContent(chatMessage.getContent().toString());
                }
            }
            if (chatMessage.getTool_calls() != null){
                Map<String, Object> additionalKwargs= new HashMap<>();
                additionalKwargs.put("functions", chatMessage.getTool_calls());
                aiMessage.setAdditionalKwargs(additionalKwargs);
            }
            return aiMessage;
        }
        return null;
    }

    @Override
    public String getStructuredChatAgentPrefixPrompt(BaseMemory memory, boolean isCH) {
        return (isCH ? DashScopePromptConstants.PREFIX_CH : DashScopePromptConstants.PREFIX);
    }

    @Override
    public String getStructuredChatAgentInstructionsPrompt(BaseMemory memory, boolean isCH) {
        return (isCH ? DashScopePromptConstants.FORMAT_INSTRUCTIONS_CH : DashScopePromptConstants.FORMAT_INSTRUCTIONS);
    }

    @Override
    public String getStructuredChatAgentSuffixPrompt(BaseMemory memory, boolean isCH) {
        return (isCH ? DashScopePromptConstants.SUFFIX_CH : DashScopePromptConstants.SUFFIX);
    }

    @Override
    public String getToolDescriptionPrompt(BaseMemory memory, boolean isCH) {
        return DashScopePromptConstants.TOOL_DESC;
    }

    @Override
    public AgentOutputParser getStructuredChatOutputParser() {
        return new DashScopeStructuredChatOutputParser();
    }

    @Override
    public AgentOutputParser getAPIChainUrlOutputParser() {
        return new DashScopeAPIChainUrlOutputParser();
    }

}
