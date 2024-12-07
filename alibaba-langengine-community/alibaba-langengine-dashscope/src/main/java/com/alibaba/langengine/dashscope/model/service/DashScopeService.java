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
package com.alibaba.langengine.dashscope.model.service;

import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import com.alibaba.langengine.dashscope.model.completion.CompletionChunk;
import com.alibaba.langengine.dashscope.model.completion.CompletionRequest;
import com.alibaba.langengine.dashscope.model.completion.CompletionResult;
import com.alibaba.langengine.dashscope.model.embedding.EmbeddingRequest;
import com.alibaba.langengine.dashscope.model.embedding.EmbeddingResult;
import com.alibaba.langengine.dashscope.model.image.DashImageQueryResult;
import com.alibaba.langengine.dashscope.model.image.DashImageRequest;
import com.alibaba.langengine.dashscope.model.image.DashImageResult;
import io.reactivex.Flowable;
import lombok.Data;

import java.net.Proxy;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 灵积模型服务
 *
 * @author xiaoxuan.lp
 */
@Data
public class DashScopeService extends RetrofitInitService<DashScopeApi> {

    public DashScopeService(String serverUrl, Duration timeout, boolean authentication, String token) {
        this(serverUrl, timeout, authentication, token, null);
    }

    public DashScopeService(String serverUrl, Duration timeout, boolean authentication, String token, Proxy proxy) {
        super(serverUrl, timeout, authentication, token, proxy);
    }

    @Override
    public Class<DashScopeApi> getServiceApiClass() {
        return DashScopeApi.class;
    }

    public CompletionResult createCompletion(CompletionRequest request) {
        Map<String, String> headers = new HashMap<>();
        if(request.isDataInspection()) {
            headers.put("X-DashScope-DataInspection", "enable");
        }
        return execute(getApi().createCompletion(request, headers));
    }

    public Flowable<CompletionChunk> streamCompletion(CompletionRequest request) {
        request.setStream(true);
        Map<String, String> headers = new HashMap<>();
        headers.put("X-DashScope-SSE", "enable");
        if(request.isDataInspection()) {
            headers.put("X-DashScope-DataInspection", "enable");
        }
        return stream(getApi().createCompletionStream(request, headers), CompletionChunk.class);
    }

    public EmbeddingResult createEmbeddings(EmbeddingRequest request) {
        return execute(getApi().createEmbeddings(request));
    }

    public CompletionResult createMultimodalGeneration(CompletionRequest request) {
        Map<String, String> headers = new HashMap<>();
        if(request.isDataInspection()) {
            headers.put("X-DashScope-DataInspection", "enable");
        }
        return execute(getApi().createMultimodalGeneration(request, headers));
    }

    public Flowable<CompletionChunk> streamMultimodalGeneration(CompletionRequest request) {
        request.setStream(true);
        Map<String, String> headers = new HashMap<>();
        headers.put("X-DashScope-SSE", "enable");
        if(request.isDataInspection()) {
            headers.put("X-DashScope-DataInspection", "enable");
        }
        return stream(getApi().createMultimodalGenerationStream(request, headers), CompletionChunk.class);
    }

    public DashImageResult createTextToImage(DashImageRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-DashScope-Async", "enable");
        return execute(getApi().createTextToImage(request.toJsonRequest(), headers));
    }

    public DashImageQueryResult queryImage(String taskId) {
        return execute(getApi().queryImageFile(taskId));
    }
}
