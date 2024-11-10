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
package com.alibaba.langengine.dashscope.embeddings;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.dashscope.embeddings.embedding.DashScopeConstant;
import com.alibaba.langengine.dashscope.model.embedding.EmbeddingRequest;
import com.alibaba.langengine.dashscope.model.embedding.EmbeddingResult;
import com.alibaba.langengine.dashscope.model.embedding.EmbeddingText;
import com.alibaba.langengine.dashscope.model.service.DashScopeService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static com.alibaba.langengine.dashscope.DashScopeConfiguration.*;

/**
 * 灵积模型服务Embeddings
 *
 * https://help.aliyun.com/zh/dashscope/developer-reference/text-embedding-quick-start
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class DashScopeEmbeddings extends Embeddings {

    private DashScopeService service;

    private String token = DASHSCOPE_API_KEY;

    /**
     * 默认text-embedding-v1，通过DashScopeConstant获取各种model类型
     */
    private String model = DashScopeConstant.MODEL_TEXT_EMBEDDING_V1;

    private static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/";

    public DashScopeEmbeddings() {
        String serverUrl = !StringUtils.isEmpty(DASHSCOPE_SERVER_URL) ? DASHSCOPE_SERVER_URL : DEFAULT_BASE_URL;
        service = new DashScopeService(serverUrl, Duration.ofSeconds(Long.parseLong(DASHSCOPE_API_TIMEOUT)), true, token);
    }

    public DashScopeEmbeddings(String token) {
        this.token = token;
        String serverUrl = !StringUtils.isEmpty(DASHSCOPE_SERVER_URL) ? DASHSCOPE_SERVER_URL : DEFAULT_BASE_URL;
        service = new DashScopeService(serverUrl, Duration.ofSeconds(Long.parseLong(DASHSCOPE_API_TIMEOUT)), true, token);
    }

    @Override
    public String getModelType() {
        return "4";
    }

    @Override
    public List<Document> embedDocument(List<Document> documents) {
        return getLenSafeEmbeddings(documents, "document");
    }

    @Override
    public List<String> embedQuery(String text, int recommend) {
        Document document = new Document();
        document.setPageContent(text);
        return getLenSafeEmbeddings(Arrays.asList(new Document[] {document}), "query").stream()
            .map(e -> JSON.toJSONString(e.getEmbedding()))
            .collect(Collectors.toList());
    }

    private List<Document> getLenSafeEmbeddings(List<Document> documents, String textType) {
        for (Document document : documents) {
            String questionText = document.getPageContent();
            EmbeddingText text = new EmbeddingText();
            text.setTexts(new ArrayList<>());
            text.getTexts().add(questionText);

            EmbeddingRequest.EmbeddingRequestBuilder builder = EmbeddingRequest.builder()
                .input(text)
                .model(getModel());
            if (!StringUtils.isEmpty(textType)) {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("text_type", textType);
                builder.parameters(parameters);
            }
            EmbeddingRequest embeddingRequest = builder.build();
            EmbeddingResult result = service.createEmbeddings(embeddingRequest);
            result.getOutput().getEmbeddings().forEach(e -> {
                document.setEmbedding(e.getEmbedding());
                document.setIndex(e.getTextIndex());
//                log.info(model + " embeddings answer:" + JSON.toJSONString(e));
            });
        }
        return documents;
    }
}