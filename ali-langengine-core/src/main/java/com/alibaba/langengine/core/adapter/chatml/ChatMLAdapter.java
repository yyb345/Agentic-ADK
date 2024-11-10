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
package com.alibaba.langengine.core.adapter.chatml;

import com.alibaba.langengine.core.adapter.ChatPromptAdapter;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.memory.BaseChatMessageHistory;
import com.alibaba.langengine.core.memory.BaseChatMessageHistory.MessageHistoryWrapper;
import com.alibaba.langengine.core.memory.BaseMemory;
import com.alibaba.langengine.core.messages.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatMLAdapter implements ChatPromptAdapter {
    /**
     * 暂时会全局增加chatml格式的stop，但是这个一般不会影响别的case
     * @param llm
     * @return
     */
    @Override
    public LLMChain wrap(LLMChain llm) {
        if(LLMChain.STOP_LIST.contains("<|im_end|>")) {
            LLMChain.STOP_LIST.add("<|im_end|>");
        }
        return llm;
    }

    @Override
    public BaseMemory wrap(BaseMemory memory){
        memory.setSystemPrefix("<|im_start|>system\n");
        memory.setAiPrefix("<|im_start|>assistant\n");
        memory.setHumanPrefix("<|im_start|>user\n");
        return memory;
    }

    /**
     * 防止注入
     * @param input
     * @return
     */
    public String escape(String input){
        try {
            return input.replace("<|im_start|>", "").replace("<|im_end|>", "");
        } catch (Exception e) {
            log.error("escape error", e);
            return input;
        }
    }

    @Override
    public BaseChatMessageHistory wrap(BaseChatMessageHistory history) {
        if(history.getMessageHistoryWrapper() == null) {
            history.setMessageHistoryWrapper(new MessageHistoryWrapper() {
                @Override
                public void modifyMessage(BaseMessage message) {
                    if(message.getContent() != null && message.getContent().startsWith("<|im_start|>")){
                        return;
                    }
                    if(message instanceof HumanMessage) {
                        message.setContent("<|im_start|>user\n"+message.getContent()+"\n<|im_end|>");
                    } else if (message instanceof AIMessage) {
                        message.setContent("<|im_start|>assistant\n"+message.getContent()+"\n<|im_end|>");
                    } else if(message instanceof SystemMessage){
                        message.setContent("<|im_start|>system\n"+message.getContent()+"\n<|im_end|>");
                    } else if(message instanceof ChatMessage){
                        message.setContent("<|im_start|>"+((ChatMessage)message).getRole()+"\n"+message.getContent()+"\n<|im_end|>");
                    }
                }
            });
        }
        return history;
    }
}
