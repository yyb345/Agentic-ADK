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

import com.alibaba.langengine.dashscope.embeddings.embedding.DashScopeConstant;
import com.alibaba.langengine.dashscope.embeddings.embedding.EmbeddingAsyncRequest;
import com.alibaba.langengine.dashscope.embeddings.embedding.EmbeddingAsyncResult;
import com.alibaba.langengine.dashscope.embeddings.service.DashScopeAsyncService;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.alibaba.langengine.dashscope.DashScopeConfiguration.DASHSCOPE_API_KEY;
import static com.alibaba.langengine.dashscope.DashScopeConfiguration.DASHSCOPE_API_TIMEOUT;

/**
 * 灵积模型服务批处理Embeddings
 *
 * https://help.aliyun.com/zh/dashscope/developer-reference/text-embedding-async-api-details
 *
 * @author xiaoxuan.lp
 */
public class DashScopeAsyncEmbeddings {

    private DashScopeAsyncService service;

    private String token = DASHSCOPE_API_KEY;

    /**
     * 默认text-embedding-async-v1，通过DashScopeConstant获取各种model类型
     */
    private String model = DashScopeConstant.MODEL_TEXT_EMBEDDING_ASYNC_V1;

    private static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/";

    public DashScopeAsyncEmbeddings() {
        service = new DashScopeAsyncService(DEFAULT_BASE_URL,
                Duration.ofSeconds(Long.parseLong(DASHSCOPE_API_TIMEOUT)),
                true,
                token);
    }

    public DashScopeAsyncEmbeddings(String token) {
        service = new DashScopeAsyncService(DEFAULT_BASE_URL,
                Duration.ofSeconds(Long.parseLong(DASHSCOPE_API_TIMEOUT)),
                true,
                token);
    }

    public EmbeddingAsyncResult createEmbeddingsAsync(String url, String textType) {
        EmbeddingAsyncRequest.EmbeddingAsyncRequestBuilder builder = EmbeddingAsyncRequest.builder()
                .model(model);
        Map<String, Object> input = new HashMap<>();
        input.put("url", url);
        builder.input(input);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("text_type", textType);
        builder.parameters(parameters);
        EmbeddingAsyncRequest embeddingAsyncRequest = builder.build();
        EmbeddingAsyncResult embeddingAsyncResult = service.createEmbeddingsAsync(embeddingAsyncRequest);
        if(!StringUtils.isEmpty(embeddingAsyncResult.getCode())) {
            throw new RuntimeException("createEmbeddingsAsync error:" + embeddingAsyncResult.getCode() + ",msg:" + embeddingAsyncResult.getMessage());
        }
        return embeddingAsyncResult;
    }

    public EmbeddingAsyncResult getEmbeddingTask(String taskId) {
        EmbeddingAsyncResult embeddingAsyncResult = service.getEmbeddingTask(taskId);
        if(!StringUtils.isEmpty(embeddingAsyncResult.getCode())) {
            throw new RuntimeException("getEmbeddingTask error:" + embeddingAsyncResult.getCode() + ",msg:" + embeddingAsyncResult.getMessage());
        }
        return embeddingAsyncResult;
    }
}
