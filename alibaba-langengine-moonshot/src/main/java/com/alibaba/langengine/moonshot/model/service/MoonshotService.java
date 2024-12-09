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
package com.alibaba.langengine.moonshot.model.service;

import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import com.alibaba.langengine.moonshot.model.completion.CompletionChunk;
import com.alibaba.langengine.moonshot.model.completion.CompletionRequest;
import com.alibaba.langengine.moonshot.model.completion.CompletionResult;
import io.reactivex.Flowable;

import java.net.Proxy;
import java.time.Duration;

public class MoonshotService extends RetrofitInitService<MoonshotApi> {
    @Override
    public Class<MoonshotApi> getServiceApiClass() {
        return MoonshotApi.class;
    }

    public MoonshotService(String serverUrl, Duration timeout, String token, Proxy proxy){
        super(serverUrl, timeout, true, token, proxy);
    }

    public Flowable<CompletionChunk> createCompletionStream(String apiVersion,CompletionRequest request) {
        return stream(getApi().createCompletionStream(apiVersion,request), CompletionChunk.class);
    }

    public CompletionResult createCompletion(String apiVersion,CompletionRequest request) {
        return execute(getApi().createCompletion(apiVersion,request));
    }


}
