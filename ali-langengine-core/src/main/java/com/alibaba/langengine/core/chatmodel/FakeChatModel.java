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
package com.alibaba.langengine.core.chatmodel;

import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionDefinition;
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
public class FakeChatModel extends BaseChatModel<ChatCompletionRequest> {

    private String mockMsg = null;

    public FakeChatModel() {
        super();
    }

    public FakeChatModel(String mockMsg) {
        this.mockMsg = mockMsg;
    }

    @Override
    public BaseMessage run(List<BaseMessage> messages, List<FunctionDefinition> functions, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        String prompt = messages.get(0).getContent();
        log.info("fakeAI prompt:\n" + prompt);

        String result = mockMsg == null ? "fakeAI mockResponse:" + prompt + "," + System.currentTimeMillis() : mockMsg;
        AIMessage aiMessage = new AIMessage();
        if(consumer!= null) {
            aiMessage.setContent(result);
            consumer.accept(aiMessage);
        }
        return aiMessage;
    }

    @Override
    public ChatCompletionRequest buildRequest(List<ChatMessage> chatMessages, List<FunctionDefinition> functions, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        return null;
    }

    @Override
    public BaseMessage runRequest(ChatCompletionRequest request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        return null;
    }

    @Override
    public BaseMessage runRequestStream(ChatCompletionRequest request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        return null;
    }


}
