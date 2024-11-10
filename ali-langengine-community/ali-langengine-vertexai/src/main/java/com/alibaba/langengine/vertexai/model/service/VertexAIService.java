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
package com.alibaba.langengine.vertexai.model.service;

import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import com.alibaba.langengine.vertexai.model.completion.CompletionRequest;
import com.alibaba.langengine.vertexai.model.completion.CompletionResult;
import lombok.Data;

import java.net.Proxy;
import java.time.Duration;


/**
 * Vertex AI 服务
 *
 * @author xiaoxuan.lp
 */
@Data
public class VertexAIService extends RetrofitInitService<VertexAIApi> {

    public VertexAIService(String serverUrl, Duration timeout, boolean authentication, String token) {
        this(serverUrl, timeout, authentication, token, null);
    }

    public VertexAIService(String serverUrl, Duration timeout, boolean authentication, String token, Proxy proxy) {
        super(serverUrl, timeout, authentication, token, proxy);
    }

    @Override
    public Class<VertexAIApi> getServiceApiClass() {
        return VertexAIApi.class;
    }

    public CompletionResult createCompletion(String projectId, String modelId, CompletionRequest request) {
        return execute(getApi().createCompletion(projectId, modelId, request));
    }
}
