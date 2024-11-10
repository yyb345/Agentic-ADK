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
package com.alibaba.langengine.gemini.model.service;

import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import com.alibaba.langengine.gemini.model.domain.GenerateContentRequest;
import com.alibaba.langengine.gemini.model.domain.GenerateContentResult;
import io.reactivex.Flowable;

import java.time.Duration;

public class GeminiService extends RetrofitInitService<GeminiApi> {

    public GeminiService(String serverUrl, Duration timeout) {
        super(serverUrl, timeout, false, null);
    }

    @Override
    public Class<GeminiApi> getServiceApiClass() {
        return GeminiApi.class;
    }

    public GenerateContentResult generateContent(String modelName, String token, GenerateContentRequest request) {
        return execute(getApi().generateContent(modelName, token, request));
    }

    public Flowable<GenerateContentResult> generateContentStream(String modelName, String token, GenerateContentRequest request) {
        return stream(getApi().generateContentStream(modelName, token, request), GenerateContentResult.class);
    }
}
