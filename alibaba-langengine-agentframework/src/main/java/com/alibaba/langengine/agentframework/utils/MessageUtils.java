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
package com.alibaba.langengine.agentframework.utils;

import com.alibaba.langengine.agentframework.model.domain.ChatAttachment;
import com.alibaba.langengine.agentframework.model.domain.ChatMessage;
import com.alibaba.langengine.core.messages.*;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessageContent;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessageRole;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.langengine.agentframework.model.domain.ChatAttachment.IMAGE_TYPE;

/**
 * 消息生成辅助类
 *
 * @author xiaoxuan.lp
 */
public class MessageUtils {

    private static final String IMAGE_URL = "image_url";

    public static List<BaseMessage> convertChatHistoryToMessageList(List<ChatMessage> chatMessages) {
        List<BaseMessage> baseMessages = new ArrayList<>();
        for (ChatMessage chatMessage : chatMessages) {
            if(ChatMessageRole.USER.value().equals(chatMessage.getRole())) {
                if(chatMessage.getType() != null && chatMessage.getType().equals("multi")) {
                    MultiHumanMessage multiHumanMessage = new MultiHumanMessage();
                    List<ChatMessageContent> contents = new ArrayList<>();
                    ChatMessageContent textContent = new ChatMessageContent();
                    textContent.setType(ChatMessage.CONTENT_TYPE_TEXT);
                    textContent.setText(chatMessage.getContent());
                    contents.add(textContent);

                    if(!CollectionUtils.isEmpty(chatMessage.getChatAttachments())) {
                        for (ChatAttachment chatAttachment : chatMessage.getChatAttachments()) {
                            ChatMessageContent content = new ChatMessageContent();
                            // TODO 需要优化
                            content.setType(getLlmImageType(chatAttachment.getType()));
                            content.setImageUrl(getImageUrl(chatAttachment.getType(), chatAttachment.getProps()));
                            contents.add(content);
                        }
                    }
                    multiHumanMessage.setChatMessageContent(contents);
                    baseMessages.add(multiHumanMessage);
                } else {
                    HumanMessage humanMessage = new HumanMessage();
                    humanMessage.setContent(chatMessage.getContent());
                    baseMessages.add(humanMessage);
                }
            } else if(ChatMessageRole.ASSISTANT.value().equals(chatMessage.getRole())) {
                if(chatMessage.getType() != null && chatMessage.getType().equals("multi")) {
                    MultiAIMessage multiAIMessage = new MultiAIMessage();
                    List<ChatMessageContent> contents = new ArrayList<>();
                    ChatMessageContent textContent = new ChatMessageContent();
                    textContent.setType(ChatMessage.CONTENT_TYPE_TEXT);
                    textContent.setText(chatMessage.getContent());
                    contents.add(textContent);

                    if(!CollectionUtils.isEmpty(chatMessage.getChatAttachments())) {
                        for (ChatAttachment chatAttachment : chatMessage.getChatAttachments()) {
                            ChatMessageContent content = new ChatMessageContent();
                            // TODO 需要优化
                            content.setType(getLlmImageType(chatAttachment.getType()));
                            content.setImageUrl(getImageUrl(chatAttachment.getType(), chatAttachment.getProps()));
                            contents.add(content);
                        }
                    }
                    multiAIMessage.setChatMessageContent(contents);
                    baseMessages.add(multiAIMessage);
                } else {
                    AIMessage aiMessage = new AIMessage();
                    aiMessage.setContent(chatMessage.getContent());
                    baseMessages.add(aiMessage);
                }
            }
        }
        return baseMessages;
    }

    public static String getLlmImageType(String type) {
        if(IMAGE_TYPE.equals(type)) {
            return IMAGE_URL;
        }
        return type;
    }

    public static Map<String, Object> getImageUrl(String type, Map<String, Object> props) {
        Map<String, Object> imageUrlMap = new HashMap<>();
        if(IMAGE_TYPE.equals(type)) {
            return props;
        }
        return imageUrlMap;
    }
}
