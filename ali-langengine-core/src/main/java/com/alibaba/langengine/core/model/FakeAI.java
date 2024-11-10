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
package com.alibaba.langengine.core.model;

import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Fake模拟大语言模型的包装器
 *
 * @author xiaoxuan.lp
 */
@Data
@Slf4j
public class FakeAI extends BaseLLM<ChatCompletionRequest> {

    private String mockMsg = null;

    public FakeAI() {
        super();
    }

    public FakeAI(String mockMsg) {
        this.mockMsg = mockMsg;
    }
    @Override
    public String run(String prompt, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        log.info("fakeAI prompt:\n"+prompt);

        String result = mockMsg == null ? "fakeAI mockResponse:" + prompt + "," + System.currentTimeMillis() : mockMsg;
        if(consumer!= null) {
            consumer.accept(result);
        }
        return result;
    }

    @Override
    public ChatCompletionRequest buildRequest(List<ChatMessage> chatMessages, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return null;
    }

    @Override
    public String runRequest(ChatCompletionRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return "";
    }

    @Override
    public String runRequestStream(ChatCompletionRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return "";
    }
}
