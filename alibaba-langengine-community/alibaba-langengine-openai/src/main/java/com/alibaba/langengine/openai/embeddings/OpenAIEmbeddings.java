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
package com.alibaba.langengine.openai.embeddings;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.model.fastchat.embedding.EmbeddingRequest;
import com.alibaba.langengine.core.model.fastchat.service.FastChatService;
import com.alibaba.langengine.openai.OpenAIConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.alibaba.langengine.openai.OpenAIConfiguration.OPENAI_API_KEY;
import static com.alibaba.langengine.openai.OpenAIConfiguration.OPENAI_SERVER_URL;

/**
 * OpenAI嵌入模型的包装器
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class OpenAIEmbeddings extends Embeddings {

    @JsonIgnore
    private FastChatService service;

    private String token = OPENAI_API_KEY;

    private String model = "text-embedding-ada-002";

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/";

    public OpenAIEmbeddings() {
        String serverUrl = !StringUtils.isEmpty(OPENAI_SERVER_URL) ? OPENAI_SERVER_URL : DEFAULT_BASE_URL;
        service = new FastChatService(serverUrl, Duration.ofSeconds(Long.parseLong(OpenAIConfiguration.OPENAI_AI_TIMEOUT)), true, token);
    }

    public OpenAIEmbeddings(String token) {
        String serverUrl = !StringUtils.isEmpty(OPENAI_SERVER_URL) ? OPENAI_SERVER_URL : DEFAULT_BASE_URL;
        service = new FastChatService(serverUrl, Duration.ofSeconds(Long.parseLong(OpenAIConfiguration.OPENAI_AI_TIMEOUT)), true, token);
    }

    @Override
    public String getModelType() {
        return "1";
    }

    @Override
    public List<Document> embedDocument(List<Document> documents) {
        return getLenSafeEmbeddings(documents);
    }

    @Override
    public List<String> embedQuery(String text, int recommend) {
        Document document = new Document();
        document.setPageContent(text);
        return getLenSafeEmbeddings(Arrays.asList(new Document[] { document })).stream()
                .map(e -> JSON.toJSONString(e.getEmbedding()))
                .collect(Collectors.toList());
    }

    private List<Document> getLenSafeEmbeddings(List<Document> documents) {
        for (Document document : documents) {
            String questionText = document.getPageContent();
            List<String> messages = new ArrayList<>();
            messages.add(questionText);
            EmbeddingRequest.EmbeddingRequestBuilder builder = EmbeddingRequest.builder()
                    .input(messages)
                    .model(getModel());
            EmbeddingRequest embeddingRequest = builder.build();
            service.createEmbeddings(embeddingRequest).getData().forEach(e -> {
                document.setEmbedding(e.getEmbedding());
                document.setIndex(e.getIndex());
//                log.warn("openai embeddings answer:" + JSON.toJSONString(e));
            });
        }
        return documents;
    }
}
