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
package com.alibaba.langengine.openai.model;

import com.alibaba.langengine.core.model.BaseLLM;
import com.alibaba.langengine.core.model.ResponseCollector;
import com.alibaba.langengine.core.model.fastchat.completion.CompletionRequest;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.core.model.fastchat.service.FastChatService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.alibaba.langengine.openai.OpenAIConfiguration.*;

/**
 * OpenAIChat大模型（支持gpt3.5以上）
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class ChatOpenAI extends BaseLLM<ChatCompletionRequest> {

    private FastChatService service;

    private String token = OPENAI_API_KEY;

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/";

    public ChatOpenAI() {
        this(OPENAI_API_KEY);
    }

    public ChatOpenAI(String apiKey) {
        this(apiKey, Long.parseLong(OPENAI_AI_TIMEOUT));
    }

    public ChatOpenAI(String apiKey, Long timeout) {
        setModel(OpenAIModelConstants.GPT_35_TURBO);
        setTemperature(0.7d);
        setMaxTokens(256);
        setTemperature(1.0d);
        setFrequencyPenalty(0.0d);
        setPresencePenalty(0.0d);
        String serverUrl = !StringUtils.isEmpty(OPENAI_SERVER_URL) ? OPENAI_SERVER_URL : DEFAULT_BASE_URL;
        service = new FastChatService(serverUrl, Duration.ofSeconds(timeout), true, apiKey);
    }

    /**
     * 为每个提示生成多少完成
     */
    private int n = 1;

    /**
     * 在服务器端生成 best_of 完成并返回“最佳”
     */
    private int bestOf = 1;

    /**
     * user
     */
    private String user;

    /**
     * logitBias
     */
    private Map<String, Integer> logitBias;

    /**
     * 模型是否返回json格式结果
     */
    private boolean jsonMode = false;

    /**
     * 是否流式增量
     */
    private boolean sseInc = false;

    @Override
    public ChatCompletionRequest buildRequest(List<ChatMessage> chatMessages, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        ChatCompletionRequest.ChatCompletionRequestBuilder builder = ChatCompletionRequest.builder();
        builder.messages(chatMessages);
        builder.n(n);
        if(user != null) {
            builder.user(user);
        }
        if(logitBias != null) {
            builder.logitBias(logitBias);
        }
        return builder.build();
    }

    @Override
    public String runRequest(ChatCompletionRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        List<String> answerContentList = new ArrayList<>();

        service.createChatCompletion(request).getChoices().forEach(e -> {
            ChatMessage message = e.getMessage();
            if(message != null) {
                String role = message.getRole();
                String answer = message.getContent().toString();
                log.warn(getModel() + " chat answer:{},{}", role, answer);
                if (answer != null) {
                    answerContentList.add(answer);
                    if (consumer != null) {
                        consumer.accept(answer);
                    }
                }
            }
        });

        String responseContent = answerContentList.stream().collect(Collectors.joining(""));
        return responseContent;
    }

    @Override
    public String runRequestStream(ChatCompletionRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        List<String> answerContentList = new ArrayList<>();
        AtomicReference<ResponseCollector> answerContent = new AtomicReference<>(new ResponseCollector(sseInc));

        service.streamChatCompletion(request)
                .doOnError(Throwable::printStackTrace)
                .blockingForEach(e -> {
                    ChatMessage message = e.getChoices().get(0).getMessage();
                    if(message != null) {
                        String role = message.getRole();
                        if(message.getContent() != null) {
                            String answer = message.getContent().toString();
                            log.warn(getModel() + " chat stream answer:{},{}", role, answer);
                            if (answer != null) {
                                answerContent.get().collect(answer);
                                String response = answerContent.get().joining();
                                answerContentList.add(response);
                                if (consumer != null) {
                                    consumer.accept(response);
                                }
                            }
                        }
                    }
                });

        String responseContent = answerContentList.stream().collect(Collectors.joining(""));
        return responseContent;
    }
}
