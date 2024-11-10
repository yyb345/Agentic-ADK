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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A group chat class that contains the following data fields:
 * - agents: a list of participating agents.
 * - messages: a list of messages in the group chat.
 * - max_round: the maximum number of rounds.
 * - admin_name: the name of the admin agent if there is one. Default is "Admin".
 * KeyBoardInterrupt will make the admin agent take over.
 * - func_call_filter: whether to enforce function call filter. Default is True.
 * When set to True and when a message is a function call suggestion,
 * the next speaker will be chosen from an agent which contains the corresponding function name
 * in its `function_map`.
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class GroupChat {

    private List<Agent> agents;
    private List<Map<String, Object>> messages;
    private int maxRound = 10;
    private String adminName = "Admin";
    private boolean funcCallFilter = true;

    public List<String> getAgentNames() {
        List<String> agentNames = new ArrayList<>();
        for (Agent agent : agents) {
            agentNames.add(agent.getName());
        }
        return agentNames;
    }

    public void reset() {
        messages.clear();
    }

    public Agent getAgentByName(String name) {
        int index = getAgentNames().indexOf(name);
        if (index >= 0) {
            return agents.get(index);
        }
        return null;
    }

    public Agent nextAgent(Agent agent, List<Agent> agents) {
        if (agents.equals(this.agents)) {
            int index = getAgentNames().indexOf(agent.getName());
            return agents.get((index + 1) % agents.size());
        } else {
            int offset = getAgentNames().indexOf(agent.getName()) + 1;
            for (int i = 0; i < this.agents.size(); i++) {
                Agent nextAgent = this.agents.get((offset + i) % this.agents.size());
                if (agents.contains(nextAgent)) {
                    return nextAgent;
                }
            }
        }
        return null;
    }

    public String selectSpeakerMsg(List<Agent> agents) {
        StringBuilder message = new StringBuilder();
        message.append("You are in a role play game. The following roles are available:\n");
        message.append(participantRoles()).append(".\n");
        message.append("Read the following conversation.\n");
        message.append("Then select the next role from ");
        message.append("[\"" + agents.stream().map(e -> e.getName()).collect(Collectors.joining("\",\"")) +  "\"]");
        message.append(" to play. Only return the role.");
        return message.toString();
    }

    public Agent selectSpeaker(Agent lastSpeaker, ConversableAgent selector) {
        if (this.funcCallFilter && !this.messages.isEmpty() && this.messages.get(this.messages.size() - 1).get("function_call") != null) {
            List<Agent> agents = new ArrayList<>();
            for (Agent agent : this.agents) {
                if (agent.canExecuteFunction((String) ((Map<String, Object>) this.messages.get(this.messages.size() - 1).get("function_call")).get("name"))) {
                    agents.add(agent);
                }
            }
            if (agents.size() == 1) {
                return agents.get(0);
            } else if (agents.isEmpty()) {
                agents = new ArrayList<>();
                for (Agent agent : this.agents) {
                    if (agent.getFunctionMap() != null) {
                        agents.add(agent);
                    }
                }
                if (agents.size() == 1) {
                    return agents.get(0);
                } else if (agents.isEmpty()) {
                    throw new IllegalArgumentException("No agent can execute the function " + this.messages.get(this.messages.size() - 1).get("name") + ". Please check the function_map of the agents.");
                }
            }
        } else {
            List<Agent> agents = this.agents;
            int nAgents = agents.size();
            if (nAgents < 3) {
                log.warn("GroupChat is underpopulated with " + nAgents + " agents. Direct communication would be more efficient.");
            }
        }

        selector.updateSystemMessage(selectSpeakerMsg(agents));
//        Map<String, Object> systemMessages = new HashMap<>();
//        systemMessages.put("role", "system");
//        systemMessages.put("content", "Read the above conversation. Then select the next role from " + agents.stream().map(Agent::getName).collect(Collectors.toList()) + " to play. Only return the role.");

        List<Map<String, Object>> allMessages = new ArrayList<>(this.messages);
//        allMessages.add(systemMessages);

        Boolean finalMsg;
        Object name;
        try {
            ReplyResult replyResult = selector.generateOaiReply(allMessages, null);
            finalMsg = replyResult.isFinalFlag();
            name = replyResult.getReply();
            if(name.toString().trim().endsWith("TERMINATE")) {
                return null;
            }
        } catch (Exception e) {
            log.warn("GroupChat selectSpeaker failed to resolve the next speaker's name. Speaker selection will default to the next speaker in the list. This is because the speaker selection OAI call returned:\n" + e.getMessage());
            return this.nextAgent(lastSpeaker, agents);
        }

        if (!finalMsg) {
            return this.nextAgent(lastSpeaker, agents);
        }

        try {
            Agent agent = getAgentByName((String) name);
            if(agent == null) {
                return nextAgent(lastSpeaker, agents);
            }
            return agent;
        } catch (IllegalArgumentException e) {
            log.warn("GroupChat selectSpeaker failed to resolve the next speaker's name. Speaker selection will default to the next speaker in the list. This is because the speaker selection OAI call returned:\n" + name);
            return nextAgent(lastSpeaker, agents);
        }
    }

    private String participantRoles() {
        StringBuilder roles = new StringBuilder();
        for (Agent agent : agents) {
            if (agent.getSystemMessage().trim().isEmpty()) {
                log.warn(String.format("The agent '%s' has an empty system_message, and may not work well with GroupChat.", agent.getName()));
            }
            roles.append(agent.getName()).append(": ").append(agent.getSystemMessage()).append("\n");
        }
        return roles.toString();
    }
}
