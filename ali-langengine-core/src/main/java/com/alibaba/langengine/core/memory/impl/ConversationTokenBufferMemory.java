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
package com.alibaba.langengine.core.memory.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.langengine.core.memory.BaseChatMemory;
import com.alibaba.langengine.core.memory.BaseChatMessageHistory;
import com.alibaba.langengine.core.memory.ChatMessageHistory;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.MessageConverter;
import com.alibaba.langengine.core.tokenizers.GPT3Tokenizer;

import lombok.Data;

/**
 * @author aihe.ah
 * @time 2023/9/13 10:57
 * 功能说明：
 * 如果是集群模式下，要修改下chatMemory的history类型
 */
@Data
public class ConversationTokenBufferMemory extends BaseChatMemory {

    private String humanPrefix = "Human";
    private String aiPrefix = "AI";
    private BaseLanguageModel llm;
    private String memoryKey = "history";
    private int maxTokenLimit = 2000;

    private BaseChatMessageHistory chatMemory = new ChatMessageHistory();

    public Object getBuffer() {

        if (isReturnMessages()) {
            return getBufferAsMessages();
        } else {
            return getBufferAsString();
        }
    }

    public String getBufferAsString() {
        return MessageConverter.getBufferString(getChatMemory().getMessages(), getHumanPrefix(), getAiPrefix(),
            getSystemPrefix(), null, getToolPrefix());
    }

    public List<BaseMessage> getBufferAsMessages() {
        return getChatMemory().getMessages();
    }

    public List<String> getMemoryVariables() {
        List<String> memoryVariables = new ArrayList<>();
        memoryVariables.add(memoryKey);
        return memoryVariables;
    }

    @Override
    public List<String> memoryVariables() {
        return Arrays.asList(new String[] {memoryKey});
    }

    @Override
    public Map<String, Object> loadMemoryVariables(Map<String, Object> inputs) {
        Map<String, Object> map = new HashMap<>();
        map.put(memoryKey, getBuffer());
        return map;
    }

    @Override
    public void saveContext(Map<String, Object> inputs, Map<String, Object> outputs) {
        super.saveContext(inputs, outputs);
        List<BaseMessage> buffer = getChatMemory().getMessages();
        GPT3Tokenizer tokenizer = new GPT3Tokenizer();
        int currBufferLength = tokenizer.getTokenCount(
            MessageConverter.getBufferString(buffer, getHumanPrefix(), getAiPrefix(), getSystemPrefix(), null, getToolPrefix()));

        while (currBufferLength > maxTokenLimit) {
            // Calculate the token count of the first message in the buffer
            int firstBufferLength = tokenizer.getTokenCount(
                MessageConverter.getBufferString(Arrays.asList(buffer.get(0)), getHumanPrefix(),
                    getAiPrefix(), getSystemPrefix(), null, getToolPrefix()));
            // Remove the first message in the buffer
            buffer.remove(0);
            // Subtract the token count of the removed message from the total count
            currBufferLength -= firstBufferLength;
        }
        getChatMemory().setMessages(buffer);
    }
}