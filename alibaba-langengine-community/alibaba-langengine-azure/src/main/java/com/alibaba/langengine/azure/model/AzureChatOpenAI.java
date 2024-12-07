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
package com.alibaba.langengine.azure.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.azure.model.service.AzureOpenAIService;
import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.MessageConverter;
import com.alibaba.langengine.core.model.fastchat.completion.chat.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.alibaba.langengine.azure.AzureConfiguration.*;

/**
 * @author: andrea.phl
 * @create: 2024-01-25 16:34
 **/
@Slf4j
@Data
public class AzureChatOpenAI extends BaseChatModel<ChatCompletionRequest> {

    private AzureOpenAIService service;

    private String azureApiPrefix = "openai";

    private String azureDeploymentsPrefix = "deployments";

    private String deploymentName;

    private String apiVersion;

    /**
     * 使用无参数构造函数, 需在配置文件中设置:
     * azure_openai_server_url
     * azure_deployment_name
     * azure_openai_api_version
     * azure_openai_api_key
     */
    public AzureChatOpenAI() {
        this(AZURE_OPENAI_SERVER_URL, AZURE_DEPLOYMENT_NAME, AZURE_OPENAI_API_VERSION);
    }

    public AzureChatOpenAI(String serverUrl, String deploymentName, String apiVersion) {
        this(serverUrl, deploymentName, apiVersion, AZURE_OPENAI_API_KEY);
    }

    public AzureChatOpenAI(String serverUrl, String deploymentName, String apiVersion, String apiKey) {
        this(serverUrl, deploymentName, apiVersion, apiKey, Integer.parseInt(AZURE_OPENAI_AI_TIMEOUT));
    }

    public AzureChatOpenAI(String serverUrl, String deploymentName, String apiVersion, String apiKey, Integer timeout) {
        //将serverUrl中path的部分, 挪到azureApiPrefix中
        if (serverUrl != null) {
            int separatorIndex = serverUrl.indexOf("/", "https://".length());
            if (separatorIndex > 0 && separatorIndex < serverUrl.length() - 1) {
                azureApiPrefix = serverUrl.substring(separatorIndex + 1) + "/" + azureApiPrefix;
                serverUrl = serverUrl.substring(0, separatorIndex);
            }
        }
        service = new AzureOpenAIService(serverUrl, Duration.ofSeconds(timeout), true, apiKey);
        this.deploymentName = deploymentName;
        this.apiVersion = apiVersion;
    }

    /**
     * 要为每个输入消息生成的聊天完成选项数。
     */
    private Integer n;

    /**
     * 修改指定令牌在完成中出现的可能性。 接受 json 对象，该对象将标记（由 tokenizer 中的标记 ID 指定）映射到从 -100 到 100 的相关偏差值。 在数学上，采样之前会将偏差添加到由模型生成的 logit 中。 具体效果因模型而异，但 -1 和 1 之间的值会减少或增加选择的可能性；-100 或 100 等值会导致相关令牌的禁止或独占选择。
     */
    private Map<String, Integer> logitBias;

    /**
     * 表示最终用户的唯一标识符，可帮助 Azure OpenAI 监视和检测滥用行为。
     */
    private String user;

    private String deploymentPath() {
        return azureApiPrefix + "/" + azureDeploymentsPrefix + "/" + deploymentName;
    }

    @Override
    public ChatCompletionRequest buildRequest(List<ChatMessage> chatMessages, List<FunctionDefinition> functions, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        ChatCompletionRequest.ChatCompletionRequestBuilder builder = ChatCompletionRequest.builder()
                .messages(chatMessages);
        builder.n(n);
        builder.user(user);
        builder.logitBias(logitBias);
        builder.functions(functions);
        return builder.build();
    }

    @Override
    public BaseMessage runRequest(ChatCompletionRequest request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        AtomicReference<BaseMessage> baseMessage = new AtomicReference<>();

        ChatCompletionResult chatCompletion = service.createChatCompletion(deploymentPath(), apiVersion, request);
        chatCompletion.getChoices().forEach(e -> {
            ChatMessage chatMessage = e.getMessage();
            if (chatMessage != null) {
                BaseMessage message = MessageConverter.convertChatMessageToMessage(chatMessage);
                String answer;
                String role = chatMessage.getRole();
                if (chatMessage.getFunctionCall() != null && !chatMessage.getFunctionCall().isEmpty()) {
                    Map<String, Object> functionCallMap = new HashMap<>();
                    functionCallMap.put("function_call", chatMessage.getFunctionCall());
                    answer = JSON.toJSONString(functionCallMap);
                } else {
                    answer = chatMessage.getContent().toString();
                }
                log.warn(deploymentName + " chat answer:{},{}", role, answer);
                if (message != null) {
                    baseMessage.set(message);
                }
            }
        });

        return baseMessage.get();
    }

    @Override
    public BaseMessage runRequestStream(ChatCompletionRequest request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        AtomicReference<BaseMessage> baseMessage = new AtomicReference<>();

        service.streamChatCompletion(deploymentPath(), apiVersion, request)
                .doOnError(Throwable::printStackTrace)
                .blockingForEach(e -> {
                    List<ChatCompletionChoice> choices = e.getChoices();
                    //azure的openai流式, 第一个返回的choices是空的
                    if (!choices.isEmpty()) {
                        ChatMessage chatMessage = choices.get(0).getMessage();
                        if (chatMessage != null) {
                            BaseMessage message = MessageConverter.convertChatMessageToMessage(chatMessage);
                            String role = chatMessage.getRole();
                            //最后一个返回的content可能是null
                            if (chatMessage.getContent() != null) {
                                String answer = message.getContent().toString();
                                log.warn(deploymentName + " chat stream answer:{},{}", role, answer);
                                if (answer != null) {
    //                                        answerContentList.add(answer);
                                    baseMessage.set(message);
                                    if (consumer != null) {
                                        consumer.accept(message);
                                    }
                                }
                            }
                        }
                    }
                });

        return baseMessage.get();
    }
}
