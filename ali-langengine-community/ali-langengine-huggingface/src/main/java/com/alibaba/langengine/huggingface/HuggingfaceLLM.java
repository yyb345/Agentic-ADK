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
package com.alibaba.langengine.huggingface;

import com.alibaba.langengine.core.model.BaseLLM;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.huggingface.completion.CompletionRequest;
import com.alibaba.langengine.huggingface.service.HuggingfaceService;
import lombok.Data;

import java.net.Proxy;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.alibaba.langengine.huggingface.HuggingfaceConfiguration.HUGGINGFACE_API_KEY;
import static com.alibaba.langengine.huggingface.HuggingfaceConfiguration.HUGGINGFACE_API_TIMEOUT;

@Data
public abstract class HuggingfaceLLM extends BaseLLM<CompletionRequest> {

    private HuggingfaceService service;

    private String token = HUGGINGFACE_API_KEY;

    private static final String DEFAULT_BASE_URL = "https://api-inference.huggingface.co/";

    public HuggingfaceLLM() {
        this(null);
    }

    public HuggingfaceLLM(Proxy proxy) {
        service = new HuggingfaceService(DEFAULT_BASE_URL, Duration.ofSeconds(Long.parseLong(HUGGINGFACE_API_TIMEOUT)), true, token, proxy);
    }

    public HuggingfaceLLM(String serverUrl, Integer timeout) {
        this(serverUrl, timeout, null);
    }

    public HuggingfaceLLM(String serverUrl, Integer timeout, Proxy proxy) {
        service = new HuggingfaceService(serverUrl, Duration.ofSeconds(timeout), true, token, proxy);
    }

    @Override
    public CompletionRequest buildRequest(List<ChatMessage> chatMessages, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return null;
    }

    @Override
    public String runRequest(CompletionRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return "";
    }

    @Override
    public String runRequestStream(CompletionRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return "";
    }
}
