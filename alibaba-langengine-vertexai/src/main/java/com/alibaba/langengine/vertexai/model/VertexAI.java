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
package com.alibaba.langengine.vertexai.model;

import com.alibaba.langengine.core.model.BaseLLM;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.vertexai.model.completion.CompletionRequest;
import com.alibaba.langengine.vertexai.model.completion.CompletionResult;
import com.alibaba.langengine.vertexai.model.completion.InstanceField;
import com.alibaba.langengine.vertexai.model.completion.ParameterField;
import com.alibaba.langengine.vertexai.model.service.VertexAIService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.alibaba.langengine.vertexai.VertexaiConfiguration.*;

/**
 * Google Vertex AI
 *
 * https://cloud.google.com/vertex-ai/docs/generative-ai/text/test-text-prompts?hl=zh-cn
 * https://cloud.google.com/vertex-ai/docs/generative-ai/embeddings/get-text-embeddings
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class VertexAI extends BaseLLM<CompletionRequest> {

    private VertexAIService service;

    private String token = VERTEXAI_API_KEY;

    private static final String DEFAULT_BASE_URL = "https://us-central1-aiplatform.googleapis.com/";

    public VertexAI() {
        this(VERTEXAI_API_KEY);
    }

    public VertexAI(String token) {
        setModel("text-bison");
        setTemperature(0.7d);
        setMaxTokens(256);
        String serverUrl = !StringUtils.isEmpty(VERTEXAI_SERVER_URL) ? VERTEXAI_SERVER_URL : DEFAULT_BASE_URL;
        service = new VertexAIService(serverUrl, Duration.ofSeconds(Long.parseLong(VERTEXAI_API_TIMEOUT)), true, token);
    }

    @Override
    public CompletionRequest buildRequest(List<ChatMessage> chatMessages, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        List<InstanceField> instanceFields = new ArrayList<>();
        InstanceField instanceField = new InstanceField();
        instanceField.setPrompt(chatMessages.get(0).getContent().toString());
        instanceFields.add(instanceField);

        ParameterField parameterField = new ParameterField();
        parameterField.setTemperature(getTemperature());
        parameterField.setMaxOutputTokens(getMaxTokens());
        parameterField.setTopP(getTopP());
        parameterField.setTopK(getTopK());

        CompletionRequest.CompletionRequestBuilder builder = CompletionRequest.builder()
                .instances(instanceFields)
                .parameters(parameterField);
        return builder.build();
    }

    @Override
    public String runRequest(CompletionRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        List<String> answerContentList = new ArrayList<>();
        CompletionResult completionResult = service.createCompletion(VERTEXAI_PROJECT_ID, getModel(), request);
        String answer = completionResult.getPredictions().get(0).getContent();
        if (answer != null) {
            answerContentList.add(answer);
        }

        String responseContent = answerContentList.stream().collect(Collectors.joining(""));
        log.warn(getModel() + " answer:" + responseContent);
        return responseContent;
    }

    @Override
    public String runRequestStream(CompletionRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        List<String> answerContentList = new ArrayList<>();
        CompletionResult completionResult = service.createCompletion(VERTEXAI_PROJECT_ID, getModel(), request);
        String answer = completionResult.getPredictions().get(0).getContent();
        if (answer != null) {
            answerContentList.add(answer);
        }

        String responseContent = answerContentList.stream().collect(Collectors.joining(""));
        log.warn(getModel() + " answer:" + responseContent);
        return responseContent;
    }
}
