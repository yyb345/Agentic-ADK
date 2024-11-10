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
package com.alibaba.langengine.claude.model.service;

import com.alibaba.langengine.claude.model.completion.ChatCompletionChunk;
import com.alibaba.langengine.claude.model.completion.ChatCompletionRequest;
import com.alibaba.langengine.claude.model.completion.ChatCompletionResult;
import com.alibaba.langengine.core.model.fastchat.completion.CompletionChunk;
import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import io.reactivex.Flowable;
import lombok.Data;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import java.net.Proxy;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Data
public class ClaudeService extends RetrofitInitService<ClaudeApi> {

    public ClaudeService(String serverUrl, Duration timeout, boolean authentication, String token) {
        super(serverUrl, timeout, authentication, token);
    }

    @Override
    public Class<ClaudeApi> getServiceApiClass() {
        return ClaudeApi.class;
    }

    public ChatCompletionResult createCompletion(ChatCompletionRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("anthropic-version", "2023-06-01");
        return execute(getApi().createCompletion(request, headers));
    }

    public Flowable<ChatCompletionChunk> createCompletionStream(ChatCompletionRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("anthropic-version", "2023-06-01");
        return stream(getApi().createCompletionStream(request, headers), ChatCompletionChunk.class);
    }

    @Override
    public OkHttpClient defaultClient(Duration timeout, Proxy proxy) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .proxy(Proxy.NO_PROXY)
                .connectionPool(new ConnectionPool(100, 1, TimeUnit.SECONDS))
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (super.isDebug()) {
            // 添加日志拦截器
            HttpLoggingInterceptor loggingInterceptor2 = new HttpLoggingInterceptor();
            loggingInterceptor2.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor2);
        }
        builder.addInterceptor(new AuthenticationInterceptor(getToken()));
        return builder.build();
    }
}
