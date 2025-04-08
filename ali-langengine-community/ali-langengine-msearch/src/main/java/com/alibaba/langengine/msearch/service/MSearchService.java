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
package com.alibaba.langengine.msearch.service;

import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import com.alibaba.langengine.msearch.completion.CompletionChunk;
import com.alibaba.langengine.msearch.completion.CompletionRequest;
import com.alibaba.langengine.msearch.completion.CompletionResult;
import io.reactivex.Flowable;
import lombok.Data;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * msearch服务
 *
 * @author xiaoxuan.lp
 */
@Data
public class MSearchService extends RetrofitInitService<MSearchApi> {

    public MSearchService(String serverUrl, Duration timeout, boolean authentication, String token) {
        super(serverUrl, timeout, authentication, token, null);
    }

    @Override
    public Class<MSearchApi> getServiceApiClass() {
        return MSearchApi.class;
    }

    public CompletionResult createCompletion(CompletionRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return execute(getApi().createCompletion(request, headers));
    }

    public Flowable<CompletionChunk> streamCompletion(CompletionRequest request) {
        return stream(getApi().createCompletionStream(request), CompletionChunk.class);
    }
}
