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
package com.alibaba.langengine.azure.model.service;

import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionChunk;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionResult;
import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import io.reactivex.Flowable;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import java.net.Proxy;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author: andrea.phl
 * @create: 2024-01-29 14:41
 **/
public class AzureOpenAIService extends RetrofitInitService<AzureOpenAIApi> {

    public AzureOpenAIService() {
        super();
    }

    public AzureOpenAIService(String serverUrl, Duration timeout, boolean authentication, String token) {
        super(serverUrl, timeout, authentication, token);
    }

    public AzureOpenAIService(String serverUrl, Duration timeout, boolean authentication, String token, Proxy proxy) {
        super(serverUrl, timeout, authentication, token, proxy);
    }

    @Override
    public Class<AzureOpenAIApi> getServiceApiClass() {
        return AzureOpenAIApi.class;
    }

    public ChatCompletionResult createChatCompletion(String deploymentPath, String apiVersion, ChatCompletionRequest request) {
        try {
            callerClass.set(getClass());
            return execute(getApi().createChatCompletion(deploymentPath, apiVersion, request));
        } finally {
            callerClass.remove();
        }
    }

    public Flowable<ChatCompletionChunk> streamChatCompletion(String deploymentPath, String apiVersion, ChatCompletionRequest request) {
        try {
            callerClass.set(getClass());
            request.setStream(true);
            return stream(getApi().streamChatCompletion(deploymentPath, apiVersion, request),
                    ChatCompletionChunk.class);
        } finally {
            callerClass.remove();
        }

    }

    @Override
    public OkHttpClient defaultClient(Duration timeout, Proxy proxy) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(20, 5, TimeUnit.SECONDS))
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (proxy != null) {
            builder.proxy(proxy);
        }
        if (isDebug()) {
            // 添加日志拦截器
            HttpLoggingInterceptor loggingInterceptor2 = new HttpLoggingInterceptor();
            loggingInterceptor2.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor2);
        }
        if (isAuthentication()) {
            builder.addInterceptor(new AuthenticationInterceptor(getToken()));
        }
        return builder.build();
    }

}
