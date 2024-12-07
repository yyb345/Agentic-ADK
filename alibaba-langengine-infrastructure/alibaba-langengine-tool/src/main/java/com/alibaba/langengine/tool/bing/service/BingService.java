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
package com.alibaba.langengine.tool.bing.service;

import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import lombok.Data;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import java.net.Proxy;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Bing Web Search服务
 *
 * @author xiaoxuan.lp
 */
@Data
public class BingService extends RetrofitInitService<BingApi> {

    public BingService(String serverUrl, Duration timeout, boolean authentication, String token) {
        super(serverUrl, timeout, authentication, token);
    }

    public BingService(String serverUrl, Duration timeout, boolean authentication, String token, Proxy proxy) {
        super(serverUrl, timeout, authentication, token, proxy);
    }

    @Override
    public Class<BingApi> getServiceApiClass() {
        return BingApi.class;
    }

    public Map<String, Object> webSearch(String q, Boolean textDecorations, String textFormat, Integer count) {
        return execute(getApi().webSearch(q, textDecorations, textFormat, count));
    }

    public Map<String, Object> imageSearch(String q, String license, String imageType, Integer count) {
        return execute(getApi().imageSearch(q, license, imageType, count));
    }

    public Map<String, Object> suggestion(String q, String mkt) {
        return execute(getApi().suggestion(q, mkt));
    }

    public Map<String, Object> spellCheck(String q, String mkt, String mode) {
        return execute(getApi().spellCheck(q, mkt, mode));
    }

    public Map<String, Object> entitySearch(String q, String mkt) {
        return execute(getApi().entitySearch(q, mkt));
    }

    public Map<String, Object> videoSearch(String q, String pricing, String videoLength, Integer count) {
        return execute(getApi().videoSearch(q, pricing, videoLength, count));
    }

    public OkHttpClient defaultClient(Duration timeout, Proxy proxy) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(100, 1, TimeUnit.SECONDS))
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
        // contribute by dapeng.fdp
        if(proxy != null) {
            builder.proxy(proxy);
        }
        builder.addInterceptor(new AuthenticationInterceptor(getToken()));
        return builder.build();
    }
}
