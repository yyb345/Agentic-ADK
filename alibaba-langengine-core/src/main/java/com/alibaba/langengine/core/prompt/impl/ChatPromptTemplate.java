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
package com.alibaba.langengine.core.prompt.impl;

import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.MultiAIMessage;
import com.alibaba.langengine.core.messages.MultiHumanMessage;
import com.alibaba.langengine.core.prompt.BaseMessagePromptTemplate;
import com.alibaba.langengine.core.prompt.PromptConverter;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Chat prompt template. This is a prompt that is sent to the user.
 *
 * @author xiaoxuan.lp
 */
@Data
public class ChatPromptTemplate extends BaseChatPromptTemplate {

    /**
     * BaseMessagePromptTemplate, BaseMessage
     */
    private List<Object> messages;

    /**
     * Create a class from a list of messages.
     *
     * @param template
     * @return
     */
    public static ChatPromptTemplate fromTemplate(String template) {
        PromptTemplate promptTemplate = new PromptTemplate();
        promptTemplate.setTemplate(template);

        HumanMessagePromptTemplate message = new HumanMessagePromptTemplate();
        message.setPrompt(promptTemplate);

        List<Object> messages = new ArrayList<>();
        messages.add(message);
        return fromMessages(messages);
    }

    public static ChatPromptTemplate fromMessages(List<Object> messages) {
        ChatPromptTemplate chatPromptTemplate = new ChatPromptTemplate();

        Set<String> inputVars = new HashSet<>();
        for (Object message : messages) {
            if (message instanceof BaseMessagePromptTemplate) {
                BaseMessagePromptTemplate promptTemplate = (BaseMessagePromptTemplate) message;
                if(promptTemplate.getInputVariables() != null) {
                    inputVars.addAll(promptTemplate.getInputVariables());
                }
            }
        }

        chatPromptTemplate.setInputVariables(inputVars.stream().collect(Collectors.toList()));
        chatPromptTemplate.setMessages(messages);
        return chatPromptTemplate;
    }

    public static ChatPromptTemplate fromChatMessages(List<BaseMessage> chatMessages) {
        ChatPromptTemplate chatPromptTemplate = new ChatPromptTemplate();
        chatPromptTemplate.setMessages(chatMessages.stream().map(e -> (Object)e).collect(Collectors.toList()));
        return chatPromptTemplate;
    }


    @Override
    public String getPromptType() {
        return "chat";
    }

    @Override
    public List<BaseMessage> formatMessages(Map<String, Object> args) {
        List<BaseMessage> result = new ArrayList<>();
        for (Object message_template : messages) {
            if (message_template instanceof BaseMessage) {
                BaseMessage baseMessage = (BaseMessage)message_template;
                if(baseMessage instanceof MultiHumanMessage) {
                    MultiHumanMessage multiHumanMessage = (MultiHumanMessage)baseMessage;
                    multiHumanMessage.chatMessageContent.stream().forEach(e -> {
                        if(e.getText() != null) {
                            e.setText(PromptConverter.replacePrompt(e.getText(), args));
                        } else {
                            if("text".equals(e.getType())) {
                                e.setText("");
                            }
                        }
                    });
                } else if(baseMessage instanceof MultiAIMessage) {
                    MultiAIMessage multiAIMessage = (MultiAIMessage)baseMessage;
                    multiAIMessage.chatMessageContent.stream().forEach(e -> {
                        if(e.getText() != null) {
                            e.setText(PromptConverter.replacePrompt(e.getText(), args));
                        } else {
                            if("text".equals(e.getType())) {
                                e.setText("");
                            }
                        }
                    });
                } else {
                    baseMessage.setContent(PromptConverter.replacePrompt(baseMessage.getContent(), args));
                }
                result.add(baseMessage);
            } else if (message_template instanceof BaseMessagePromptTemplate) {
                BaseMessagePromptTemplate promptTemplate = (BaseMessagePromptTemplate) message_template;
                List<BaseMessage> message = promptTemplate.formatMessages(args);
                result.addAll(message);
            } else {
                throw new IllegalArgumentException("Unexpected input: " + message_template);
            }
        }
        return result;
    }
}
