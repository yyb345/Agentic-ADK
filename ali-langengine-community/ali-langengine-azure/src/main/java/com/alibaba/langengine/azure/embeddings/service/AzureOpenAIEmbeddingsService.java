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
package com.alibaba.langengine.azure.embeddings.service;

import com.alibaba.langengine.core.model.fastchat.embedding.EmbeddingRequest;
import com.alibaba.langengine.core.model.fastchat.embedding.EmbeddingResult;
import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import java.net.Proxy;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author: andrea.phl
 * @create: 2024-01-29 14:41
 **/
public class AzureOpenAIEmbeddingsService extends RetrofitInitService<AzureOpenAIEmbeddingsApi> {

    public AzureOpenAIEmbeddingsService(String serverUrl, Duration timeout, boolean authentication, String token, Proxy proxy) {
        super(serverUrl, timeout, authentication, token, proxy);
    }

    @Override
    public Class<AzureOpenAIEmbeddingsApi> getServiceApiClass() {
        return AzureOpenAIEmbeddingsApi.class;
    }

    public EmbeddingResult createEmbeddings(String deploymentPath, String apiVersion, EmbeddingRequest request) {
        return execute(getApi().createChatCompletion(deploymentPath, apiVersion, request));
    }

    @Override
    public OkHttpClient defaultClient(Duration timeout, Proxy proxy) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(20, 5, TimeUnit.SECONDS))
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (proxy != null) {
            builder.proxy(proxy);
        }
        if (isAuthentication()) {
            builder.addInterceptor(new AuthenticationInterceptor(getToken()));
        }
        return builder.build();
    }

}
