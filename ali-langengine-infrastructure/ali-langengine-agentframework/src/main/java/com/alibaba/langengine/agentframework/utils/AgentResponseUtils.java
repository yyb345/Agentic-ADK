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

import com.alibaba.langengine.agentframework.model.domain.ChatMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessageRole;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AgentResponseUtils {

    public static List<ChatMessage> buildChatMessageListFromOneAnswer(String answer, String sessionId, String userId, Long startTime,
                                                                      String answerType, String contentType) {
        List<ChatMessage> chatMessages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage();
        chatMessages.add(chatMessage);
        chatMessage.setRole(ChatMessageRole.ASSISTANT.value());
        chatMessage.setType(answerType);
        chatMessage.setContent(answer);
        chatMessage.setContentType(contentType);
        chatMessage.setMessageId(IdGeneratorUtils.nextId());
        chatMessage.setSectionId(IdGeneratorUtils.nextId());
        chatMessage.setSessionId(sessionId);
        chatMessage.setSenderId(userId);
        chatMessage.getExtraInfo().setTimeCost(getTimeCost(startTime));
        return chatMessages;
    }

    public static List<ChatMessage> buildChatMessageListFromOneAnswer(String answer, String sessionId, String userId, Long startTime) {
        return buildChatMessageListFromOneAnswer(answer, sessionId, "", userId, startTime);
    }

    public static List<ChatMessage> buildChatMessageListFromOneAnswer(String answer, String sessionId, String firstCost, String userId, Long startTime) {
        List<ChatMessage> chatMessages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage();
        chatMessages.add(chatMessage);
        chatMessage.setRole(ChatMessageRole.ASSISTANT.value());
        chatMessage.setType(ChatMessage.TYPE_ANSWER);
        chatMessage.setContent(answer);
        chatMessage.setContentType(ChatMessage.CONTENT_TYPE_TEXT);
        chatMessage.setMessageId(IdGeneratorUtils.nextId());
        chatMessage.setSectionId(IdGeneratorUtils.nextId());
        chatMessage.setSessionId(sessionId);
        chatMessage.setSenderId(userId);
        chatMessage.getExtraInfo().setTimeCost(getTimeCost(startTime));
        if(StringUtils.isNotEmpty(firstCost)) {
            chatMessage.getExtraInfo().setFirstCost(firstCost);
        }
        return chatMessages;
    }

    public static List<ChatMessage> buildChatMessageListFromOneAnswer(String answer, String messageId, String sectionId, String sessionId, String userId, Long startTime) {
        List<ChatMessage> chatMessages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage();
        chatMessages.add(chatMessage);
        chatMessage.setRole(ChatMessageRole.ASSISTANT.value());
        chatMessage.setType(ChatMessage.TYPE_ANSWER);
        chatMessage.setContent(answer);
        chatMessage.setContentType(ChatMessage.CONTENT_TYPE_TEXT);
        chatMessage.setMessageId(messageId);
        chatMessage.setSectionId(sectionId);
        chatMessage.setSessionId(sessionId);
        chatMessage.setSenderId(userId);
        chatMessage.getExtraInfo().setTimeCost(getTimeCost(startTime));
        return chatMessages;
    }

    public static String getTimeCost(Long startTime) {
        if(startTime == null) {
            return null;
        }
        Long endTime = System.currentTimeMillis();
        long elapsedMillis = endTime - startTime;
        double elapsedSeconds = elapsedMillis / 1000.0;
        return String.format("%.3f", elapsedSeconds);
    }

    public static Long getTimeCostLong(Long startTime) {
        if(startTime == null) {
            return null;
        }
        Long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
}
