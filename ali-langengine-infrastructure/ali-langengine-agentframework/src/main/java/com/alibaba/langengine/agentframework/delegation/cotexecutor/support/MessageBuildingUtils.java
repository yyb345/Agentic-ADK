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
package com.alibaba.langengine.agentframework.delegation.cotexecutor.support;

import com.alibaba.langengine.agentframework.delegation.FrameworkCotCallingDelegation;
import com.alibaba.langengine.agentframework.utils.MessageUtils;
import com.alibaba.langengine.agentframework.model.domain.ChatAttachment;
import com.alibaba.langengine.agentframework.model.domain.ChatMessage;
import com.alibaba.langengine.agentframework.model.domain.FrameworkSystemContext;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.messages.MultiHumanMessage;
import com.alibaba.langengine.core.messages.SystemMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessageContent;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * chat message构建辅助工具
 *
 * @author xiaoxuan.lp
 */
public class MessageBuildingUtils {

    public static List<BaseMessage> buildMessageReturnHistory(FrameworkSystemContext systemContext, List<BaseMessage> messages, FrameworkCotCallingDelegation delegation, String knowledgeContext) {
        return buildMessageReturnHistory(systemContext, messages, delegation, knowledgeContext, null, null, null);
    }

    public static List<BaseMessage> buildMessageReturnHistory(FrameworkSystemContext systemContext, List<BaseMessage> messages, FrameworkCotCallingDelegation delegation, String knowledgeContext, List<BaseMessage> intermediateMessages) {
        return buildMessageReturnHistory(systemContext, messages, delegation, knowledgeContext, null, intermediateMessages, null);
    }

    public static List<BaseMessage> buildMessageReturnHistory(FrameworkSystemContext systemContext, List<BaseMessage> messages, FrameworkCotCallingDelegation delegation, String knowledgeContext, String systemPrompt, List<BaseMessage> intermediateMessages) {
        return buildMessageReturnHistory(systemContext, messages, delegation, knowledgeContext, systemPrompt, intermediateMessages, null);
    }

    public static List<BaseMessage> buildMessageReturnHistory(FrameworkSystemContext systemContext, List<BaseMessage> messages, FrameworkCotCallingDelegation delegation, String knowledgeContext, String systemPrompt, List<BaseMessage> intermediateMessages, String query) {
        // 获取角色prompt
        String rolePrompt;
        if(!StringUtils.isEmpty(systemPrompt)) {
            rolePrompt = systemPrompt;
        } else {
            rolePrompt = LlmBuildingUtils.buildSystemPrompt(systemContext, delegation, knowledgeContext);
        }
        List<ChatMessage> chatHistory = systemContext.getHistory();
        List<ChatAttachment> chatAttachments = systemContext.getChatAttachments();
        if(StringUtils.isEmpty(query)) {
            query = systemContext.getQuery();
        }

        messages.add(new SystemMessage(rolePrompt));

        List<BaseMessage> historyMessages = null;
        if(!CollectionUtils.isEmpty(chatHistory)) {
            for (ChatMessage chatHistoryItem : chatHistory) {
                if(!CollectionUtils.isEmpty(chatHistoryItem.getChatAttachments())) {
                    chatHistoryItem.setType("multi");
                }
            }
            historyMessages = MessageUtils.convertChatHistoryToMessageList(chatHistory);
            messages.addAll(historyMessages);
        }

        if(!CollectionUtils.isEmpty(intermediateMessages)) {
            messages.addAll(intermediateMessages);
        }

        if(CollectionUtils.isEmpty(chatAttachments)) {
            messages.add(new HumanMessage(query));
        } else {
            MultiHumanMessage multiHumanMessage = new MultiHumanMessage();
            messages.add(multiHumanMessage);

            List<ChatMessageContent> contents = Lists.newArrayList();
            ChatMessageContent content = new ChatMessageContent();
            content.setText(query);
            content.setType(ChatMessage.CONTENT_TYPE_TEXT);
            contents.add(content);
            for (ChatAttachment attachment : chatAttachments) {
                ChatMessageContent chatMessageContent = new ChatMessageContent();
                content.setType(MessageUtils.getLlmImageType(attachment.getType()));
                content.setImageUrl(MessageUtils.getImageUrl(attachment.getType(), attachment.getProps()));
                contents.add(chatMessageContent);
            }
            multiHumanMessage.setChatMessageContent(contents);
            messages.add(multiHumanMessage);
        }

        return historyMessages;
    }
}
