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
package com.alibaba.langengine.huggingface.service;

import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import com.alibaba.langengine.huggingface.completion.CompletionRequest;
import lombok.Data;

import java.net.Proxy;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * HuggingFace服务
 *
 * @author xiaoxuan.lp
 */
@Data
public class HuggingfaceService extends RetrofitInitService<HuggingfaceApi> {

    public HuggingfaceService(String serverUrl, Duration timeout, boolean authentication, String token) {
        this(serverUrl, timeout, authentication, token, null);
    }

    public HuggingfaceService(String serverUrl, Duration timeout, boolean authentication, String token, Proxy proxy) {
        super(serverUrl, timeout, authentication, token, proxy);
    }

    @Override
    public Class<HuggingfaceApi> getServiceApiClass() {
        return HuggingfaceApi.class;
    }

    public List<Map<String, Object>> createListCompletion(String modelId, CompletionRequest request) {
        return execute(getApi().createMapListCompletion(modelId, request));
    }

    public Object createCompletion(String modelId, CompletionRequest request) {
        return execute(getApi().createCompletion(modelId, request));
    }

    public List<Object> createBasicListCompletion(String modelId, CompletionRequest request) {
        return execute(getApi().createBasicListCompletion(modelId, request));
    }
}
