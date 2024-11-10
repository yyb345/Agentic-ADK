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
package com.alibaba.langengine.autogen.agentchat;

import com.alibaba.langengine.autogen.Agent;
import com.alibaba.langengine.autogen.agentchat.support.ReplyResult;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 *  A chat manager agent that can manage a group chat of multiple agents.
 *  聊天管理代理，可以管理多个代理的群聊。
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class GroupChatManager extends ConversableAgent {

    private GroupChat groupChat;

    public GroupChatManager(String name, BaseLanguageModel llm) {
        super(name, llm);
    }

    public GroupChatManager(String name, BaseLanguageModel llm, GroupChat groupChat) {
        super(name, llm, "Group chat manager.", Integer.MAX_VALUE, "NEVER");
        setGroupChat(groupChat);
    }

    @Override
    public Object generateReply(List<Map<String, Object>> messages, Agent sender) {
        if (messages == null && sender == null) {
            String errorMsg = "Either messages or sender must be provided.";
            log.error(errorMsg);
            throw new AssertionError(errorMsg);
        }

        if (messages == null) {
            for (Map.Entry entry : getOaiMessages().entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                System.out.println("key:" + key.hashCode() + "value:" + value.hashCode());
                System.out.println("sender:" + sender.hashCode());
            }
            messages = getOaiMessages().get(sender);
        }

        ReplyResult replyResult = runChat(messages, sender);
        if(replyResult.isFinalFlag()) {
            return replyResult.getReply() != null ? replyResult.getReply() : replyResult.getOutput();
        }

        return null;
    }

    private ReplyResult runChat(List<Map<String, Object>> messages, Agent sender) {
        if (messages == null) {
            messages = getOaiMessages().get(sender);
        }
        Map<String, Object> message = messages.get(messages.size() - 1);
        Agent speaker = sender;
        for (int i = 0; i < groupChat.getMaxRound(); i++) {
            if (!message.get("role").equals("function")) {
                message.put("name", speaker.getName());
            }
            groupChat.getMessages().add(message);
            for (Agent agent : groupChat.getAgents()) {
                if (!agent.equals(speaker)) {
                    this.send(message, agent, false, true);
                }
            }
            if (i == groupChat.getMaxRound() - 1) {
                break;
            }
            try {
                speaker = groupChat.selectSpeaker(speaker, this);
                if(speaker == null) {
                    return new ReplyResult(true, super.generateReply(messages, sender));
                }
//                log.error("speaker:" + speaker);
                Object reply = speaker.generateReply(null,this);
                if (reply == null) {
                    break;
                }
                speaker.send(reply, this, null, false);
                message = getLastMessage(speaker);
            } catch (Exception e) {
                if (groupChat.getAgentNames().contains(groupChat.getAdminName())) {
                    speaker = groupChat.getAgentByName(groupChat.getAdminName());
                    Object reply = speaker.generateReply(null,this);
                    if (reply == null) {
                        break;
                    }
                    speaker.send(reply, this, null, false);
                    message = getLastMessage(speaker);
                } else {
                    throw e;
                }
            }
        }
        return new ReplyResult(true, super.generateReply(messages, sender));
    }

    public GroupChat getGroupChat() {
        return groupChat;
    }

    public void setGroupChat(GroupChat groupChat) {
        this.groupChat = groupChat;
    }
}
