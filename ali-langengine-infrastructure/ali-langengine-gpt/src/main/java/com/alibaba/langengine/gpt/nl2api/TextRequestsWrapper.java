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
package com.alibaba.langengine.gpt.nl2api;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections.MapUtils;

@Data
@Slf4j
public class TextRequestsWrapper {

    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient().newBuilder().connectTimeout(30L,
        TimeUnit.SECONDS).readTimeout(60L, TimeUnit.SECONDS).writeTimeout(60L, TimeUnit.SECONDS).build();

    private Map<String, String> headers;

    public TextRequestsWrapper(Map<String, String> headers) {
        this.headers = headers;
    }

    public String get(String url) {
        Headers header;
        if (MapUtils.isNotEmpty(headers)) {
            Headers.Builder builder = new Headers.Builder();
            headers.entrySet().forEach(entry -> builder.add(entry.getKey(), entry.getValue()));
            header = builder.build();
        } else {
            header = new Headers.Builder().build();
        }
        Request request = new Request.Builder()
            .url(url)
            .headers(header)
            .build();

        Response response = null;
        try {
            response = OK_HTTP_CLIENT.newCall(request).execute();
            return response.body().string();
        } catch (Exception e) {
            log.error("TextRequestsWrapper get error", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    log.warn("close response error", e);
                }
            }
        }
        return null;
    }
}
