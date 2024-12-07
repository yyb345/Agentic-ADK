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
package com.alibaba.langengine.azure.embeddings;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.azure.embeddings.service.AzureOpenAIEmbeddingsService;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.model.fastchat.embedding.EmbeddingRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.alibaba.langengine.azure.AzureConfiguration.*;
import static com.alibaba.langengine.azure.AzureConfiguration.AZURE_OPENAI_AI_TIMEOUT;


/**
 * @author: andrea.phl
 * @create: 2024-01-29 16:52
 **/
@Slf4j
@Data
public class AzureOpenAIEmbeddings extends Embeddings {

    @JsonIgnore
    private AzureOpenAIEmbeddingsService service;

    private String azureApiPrefix = "openai";

    private String azureDeploymentsPrefix = "deployments";

    private String deploymentName;

    private String apiVersion;

    private String model = "text-embedding-ada-002";

    private String user;

    /**
     * 使用无参数构造函数, 需在配置文件中设置:
     * openai_server_url
     * azure_deployment_name
     * openai_api_version
     * openai_api_key
     */
    public AzureOpenAIEmbeddings() {
        this(AZURE_OPENAI_SERVER_URL, AZURE_DEPLOYMENT_NAME, AZURE_OPENAI_API_VERSION);
    }

    public AzureOpenAIEmbeddings(String serverUrl, String deploymentName, String apiVersion) {
        this(serverUrl, deploymentName, apiVersion, AZURE_OPENAI_API_KEY);
    }

    public AzureOpenAIEmbeddings(String serverUrl, String deploymentName, String apiVersion, String apiKey) {
        this(serverUrl, deploymentName, apiVersion, apiKey, Integer.parseInt(AZURE_OPENAI_AI_TIMEOUT));
    }

    public AzureOpenAIEmbeddings(String serverUrl, String deploymentName, String apiVersion, String apiKey, Integer timeout) {
        //将serverUrl中path的部分, 挪到azureApiPrefix中
        if (serverUrl != null) {
            int separatorIndex = serverUrl.indexOf("/", "https://".length());
            if (separatorIndex > 0 && separatorIndex < serverUrl.length() - 1) {
                azureApiPrefix = serverUrl.substring(separatorIndex + 1) + "/" + azureApiPrefix;
                serverUrl = serverUrl.substring(0, separatorIndex);
            }
        }
        service = new AzureOpenAIEmbeddingsService(serverUrl, Duration.ofSeconds(timeout), true, apiKey, null);
        this.deploymentName = deploymentName;
        this.apiVersion = apiVersion;
    }

    @Override
    public String getModelType() {
        return "7";
    }

    @Override
    public List<Document> embedDocument(List<Document> documents) {
        return getLenSafeEmbeddings(documents, "document");
    }

    @Override
    public List<String> embedQuery(String text, int recommend) {
        Document document = new Document();
        document.setPageContent(text);
        return getLenSafeEmbeddings(Collections.singletonList(document), "query")
                .stream().map(e -> JSON.toJSONString(e.getEmbedding()))
                .collect(Collectors.toList());
    }

    private List<Document> getLenSafeEmbeddings(List<Document> documents, String textType) {
        for (Document document : documents) {
            String questionText = document.getPageContent();
            List<String> messages = new ArrayList<>();
            messages.add(questionText);
            EmbeddingRequest.EmbeddingRequestBuilder builder = EmbeddingRequest.builder()
                    .input(messages)
                    .model(getModel());
            EmbeddingRequest embeddingRequest = builder.build();
            service.createEmbeddings(deploymentPath(), apiVersion, embeddingRequest).getData().forEach(e -> {
                document.setEmbedding(e.getEmbedding());
                document.setIndex(e.getIndex());
                log.warn("azure openai embeddings answer:" + JSON.toJSONString(e));
            });
        }
        return documents;
    }

    private String deploymentPath() {
        return azureApiPrefix + "/" + azureDeploymentsPrefix + "/" + deploymentName;
    }

}
