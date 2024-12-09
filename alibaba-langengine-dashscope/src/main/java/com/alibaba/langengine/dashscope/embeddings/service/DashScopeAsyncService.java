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
package com.alibaba.langengine.dashscope.embeddings.service;

import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import com.alibaba.langengine.dashscope.embeddings.embedding.EmbeddingAsyncRequest;
import com.alibaba.langengine.dashscope.embeddings.embedding.EmbeddingAsyncResult;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class DashScopeAsyncService extends RetrofitInitService<DashScopeAsyncApi> {

    public DashScopeAsyncService(String serverUrl, Duration timeout, boolean authentication, String token) {
        super(serverUrl, timeout, authentication, token);
    }

    @Override
    public Class<DashScopeAsyncApi> getServiceApiClass() {
        return DashScopeAsyncApi.class;
    }

    public EmbeddingAsyncResult createEmbeddingsAsync(EmbeddingAsyncRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-DashScope-Async", "enable");
        return execute(getApi().createEmbeddingsAsync(request, headers));
    }

    public EmbeddingAsyncResult getEmbeddingTask(String taskId) {
        return execute(getApi().getEmbeddingTask(taskId));
    }
}