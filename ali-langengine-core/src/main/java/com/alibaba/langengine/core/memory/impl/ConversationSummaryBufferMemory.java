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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.memory.SummarizerMixin;
import com.alibaba.langengine.core.messages.MessageConverter;
import com.alibaba.langengine.core.model.BaseLLM;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.prompt.BasePromptTemplate;
import com.alibaba.langengine.core.messages.SystemMessage;
import com.alibaba.langengine.core.tokenizers.GPT3Tokenizer;

import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

/**
 * @author aihe.ah
 * @time 2023/11/13
 * 功能说明：
 */
@Data
public class ConversationSummaryBufferMemory extends ConversationBufferMemory {

    private BaseLLM llm;

    private BasePromptTemplate summeryPrompt = SummarizerMixin.prompt;

    /**
     * 最大令牌限制
     */
    private int maxTokenLimit = 2000;

    /**
     * 移动摘要缓冲区
     */
    private String movingSummaryBuffer = "";

    /**
     * 内存关键字
     */
    private String memoryKey = "history";

    @Override
    public Object buffer() {
        return super.buffer();
    }

    /**
     * 总是返回内存变量列表。
     *
     * @return 内存变量的列表
     */
    public List<String> getMemoryVariables() {
        List<String> variables = new ArrayList<>();
        variables.add(memoryKey);
        return variables;
    }

    /**
     * 返回历史缓冲区。
     *
     * @param inputs 输入参数
     * @return 历史缓冲区
     */
    @Override
    public Map<String, Object> loadMemoryVariables(Map<String, Object> inputs) {
        // 加载消息的时候，对之前的消息做一下总结
        prune();
        //List<BaseMessage> buffer = getBuffer();
        List<BaseMessage> firstMessages = new ArrayList<>();
        if (!movingSummaryBuffer.isEmpty()) {
            SystemMessage systemMessage = new SystemMessage();
            systemMessage.setContent(movingSummaryBuffer);
            firstMessages.add(systemMessage);
            getChatMemory().setMessages(firstMessages);
        }

        Map<String, Object> result = new HashMap<>();
        result.put(getMemoryKey(), buffer());
        return result;
    }

    /**
     * 将这次对话的上下文保存到缓冲区中。
     *
     * @param inputs  输入参数
     * @param outputs 输出参数
     */
    @Override
    public void saveContext(Map<String, Object> inputs, Map<String, Object> outputs) {
        super.saveContext(inputs, outputs);
    }

    public String getBufferAsString() {
        return MessageConverter.getBufferString(getChatMemory().getMessages(), getHumanPrefix(), getAiPrefix(),
            getSystemPrefix(), null, getToolPrefix());
    }

    /**
     * 如果缓冲区超过最大令牌限制则进行裁剪。
     */
    public void prune() {
        List<BaseMessage> buffer = getChatMemory().getMessages();
        GPT3Tokenizer tokenizer = new GPT3Tokenizer();
        String bufferString = MessageConverter.getBufferString(buffer, getHumanPrefix(), getAiPrefix(),
            getSystemPrefix(), null, getToolPrefix());
        int currBufferLength = tokenizer.getTokenCount(bufferString);
        while (currBufferLength > this.maxTokenLimit) {
            buffer.remove(0);
            currBufferLength = tokenizer.getTokenCount(MessageConverter.getBufferString(buffer, getHumanPrefix(), getAiPrefix(),
                getSystemPrefix(), null, getToolPrefix()));
        }
        if (CollectionUtils.isEmpty(buffer)) {
            return;
        }
        // This method also assumes the existence of a method to predict a new summary,
        // which would need to be implemented in your Java code base.
        this.movingSummaryBuffer = predictNewSummary(buffer, this.movingSummaryBuffer);
    }

    public String predictNewSummary(List<BaseMessage> messages, String existingSummary) {
        String newLines = MessageConverter.getBufferString(
            messages,
            getHumanPrefix(),
            getAiPrefix(),
            getSystemPrefix(),
            null,
            getToolPrefix()
        );

        LLMChain chain = new LLMChain();
        chain.setLlm(llm);
        chain.setPrompt(summeryPrompt);
        //summary=existing_summary, new_lines=new_lines
        HashMap<String, Object> input = new HashMap<>();
        input.put("summary", existingSummary);
        input.put("new_lines", newLines);
        Map<String, Object> predict = chain.predict(input);
        Object o = predict.get(chain.getOutputKey());
        return (String)o;
    }
}
