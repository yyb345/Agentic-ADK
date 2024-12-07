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
package com.alibaba.langengine.tool.google.service;

import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import lombok.Data;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import java.net.Proxy;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Google Web Search服务
 *
 * @author xiaoxuan.lp
 */
@Data
public class GoogleService extends RetrofitInitService<GoogleApi> {

    public GoogleService() {
        super();
    }

    public GoogleService(String serverUrl, Duration timeout, boolean authentication, String token) {
        super(serverUrl, timeout, authentication, token);
    }

    public GoogleService(String serverUrl, Duration timeout, boolean authentication, String token, Proxy proxy) {
        super(serverUrl, timeout, authentication, token, proxy);
    }

    @Override
    public Class<GoogleApi> getServiceApiClass() {
        return GoogleApi.class;
    }

    public OkHttpClient defaultClient(Duration timeout, Proxy proxy) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(100, 1, TimeUnit.SECONDS))
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
        // contribute by dapeng.fdp
        if(proxy != null) {
            builder.proxy(proxy);
        }
        return builder.build();
    }

    public Map<String, Object> customSearch(String q, Integer num, String googleCseId, String googleApiKey) {
        return execute(getApi().customSearch(q, num, googleCseId, googleApiKey));
    }
}
