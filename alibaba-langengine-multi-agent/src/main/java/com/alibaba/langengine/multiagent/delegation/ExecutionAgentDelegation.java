/**
 * Copyright (C) 2024 AIDC-AI
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.multiagent.delegation;

import com.alibaba.langengine.agentframework.behavior.AgentExecutionCommandService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.messages.SystemMessage;
import com.alibaba.langengine.core.prompt.PromptConverter;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.runnables.RunnableConfig;
import com.alibaba.langengine.core.runnables.RunnableHashMap;
import com.alibaba.langengine.multiagent.delegation.base.MultiAgentDelegationBase;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import com.alibaba.smart.framework.engine.SmartEngine;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.model.instance.ProcessInstance;
import com.alibaba.smart.framework.engine.service.command.ExecutionCommandService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Slf4j
public class ExecutionAgentDelegation extends MultiAgentDelegationBase<Map<String, Object>> {

    private static final String ROUTING_AGENT_PROMPT = "You are in a role play game. The following roles are available:\n" +
            "{roles}.\n" +
            "Read the following conversation.\n" +
            "Then select the next role from {agentlist} to play. Only return the role." +
            "When you find an answer, verify the answer carefully. Include verifiable evidence in your response if possible.\n" +
            "Reply \"TERMINATE\" in the end when everything is done.";

    @Override
    public Map<String, Object> executeInternal(ExecutionContext executionContext, JSONObject properties, JSONObject request) {
        log.info("LlmCallingDelegation start");
        ProcessInstance processInstance = executionContext.getProcessInstance();
        String activityId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();
        String query = request.getString("query");

        String agentStrategy = properties.getString("agentStrategy");
        String agentType = properties.getString("agentType");
        String agentRoles = properties.getString("agentRoles");
        Integer maxRound = properties.getInteger("maxRound");
        String backstory = properties.getString("backstory");
        String systemPrompt = properties.getString("systemPrompt");

        if(maxRound != null && maxRound > 0) {
            Integer currentRound;
            if(request.get("currentRound") != null) {
                currentRound = (Integer) request.get("currentRound");
            } else {
                currentRound = 0;
            }

            log.info("current round is {}", currentRound);
            if(currentRound >= maxRound) {
                log.info("EntryAgentDelegation finished");
                return null;
            }
        }

        if(!StringUtils.isEmpty(agentStrategy)) {
            if(!StringUtils.isEmpty(systemPrompt)) {
                String rolePrompt = systemPrompt;
                List<BaseMessage> messages = new ArrayList<>();
                messages.add(new SystemMessage(rolePrompt));
                messages.add(new HumanMessage(query));
//                List<BaseMessage> historyMessages = MessageBuildingUtils.buildMessageReturnHistory(systemContext, messages, this, null, new ArrayList<>());
                ChatPromptTemplate prompt = ChatPromptTemplate.fromChatMessages(messages);

//                BaseLanguageModel model = new IdealabChatModel();
//                model.setModel(IdealabModelEnum.GPT_4_8K.getModel());
                BaseLanguageModel model = new ChatModelOpenAI();
                model.setModel(OpenAIModelConstants.GPT_4);
                model.setTemperature(0.7d);
                com.alibaba.langengine.core.runnables.Runnable agent = Runnable.sequence(
                        prompt,
                        model);

                RunnableConfig config = new RunnableConfig();
                Object runnableOutput = agent.invoke(new RunnableHashMap(), config);
                log.info("LlmCallingDelegation agent invoke response:{}", JSON.toJSONString(runnableOutput));
                String answer = null;
                if (runnableOutput instanceof RunnableHashMap) {
                    if (((RunnableHashMap) runnableOutput).get("output") != null) {
                        answer = ((RunnableHashMap) runnableOutput).get("output").toString();
                    } else {
                        answer = JSON.toJSONString(runnableOutput);
                    }
                } else if (runnableOutput instanceof AIMessage) {
                    answer = ((AIMessage) runnableOutput).getContent();
                }

                List<BaseMessage> historyMessages;
                if(request.get("historyMessages") != null) {
                    historyMessages = (List<BaseMessage>) request.get("historyMessages");
                } else {
                    historyMessages = new ArrayList<>();
                }

                historyMessages.add(new AIMessage(answer));
                request.put("historyMessages", historyMessages);

                Integer currentRound = 0;
                if(request.get("currentRound") != null) {
                    currentRound = (Integer) request.get("currentRound");
                }
                request.put("currentRound", currentRound + 1);
                request.put("preAgent", activityId);

                log.error("{}:\n{}\n--------------------------------------------------------------------------------", activityId, answer);
            }

            Map<String, Object> input = new HashMap<>();
            input.put("roles", backstory);
            input.put("agentlist", agentRoles);
            String rolePrompt = PromptConverter.replacePrompt(ROUTING_AGENT_PROMPT, input);
            List<BaseMessage> messages = new ArrayList<>();
            messages.add(new SystemMessage(rolePrompt));
            messages.add(new HumanMessage(query));

            List<BaseMessage> historyMessages;
            if(request.get("historyMessages") != null) {
                historyMessages = (List<BaseMessage>) request.get("historyMessages");
                messages.addAll(historyMessages);
            } else {
                historyMessages = new ArrayList<>();
            }

            ChatPromptTemplate prompt = ChatPromptTemplate.fromChatMessages(messages);

//            BaseLanguageModel model = new IdealabChatModel();
//            model.setModel(IdealabModelEnum.GPT_4_8K.getModel());
            BaseLanguageModel model = new ChatModelOpenAI();
            model.setModel(OpenAIModelConstants.GPT_4);
            model.setTemperature(0.7d);
            com.alibaba.langengine.core.runnables.Runnable agent = Runnable.sequence(
                    prompt,
                    model);

            RunnableConfig config = new RunnableConfig();
            Object runnableOutput = agent.invoke(new RunnableHashMap(), config);
            log.info("ExecutionAgentDelegation agent invoke response:{}", JSON.toJSONString(runnableOutput));
            String answer = null;
            if (runnableOutput instanceof RunnableHashMap) {
                if (((RunnableHashMap) runnableOutput).get("output") != null) {
                    answer = ((RunnableHashMap) runnableOutput).get("output").toString();
                } else {
                    answer = JSON.toJSONString(runnableOutput);
                }
            } else if (runnableOutput instanceof AIMessage) {
                answer = ((AIMessage) runnableOutput).getContent();
            }

            historyMessages.add(new AIMessage(answer));
            request.put("historyMessages", historyMessages);

            Integer currentRound = 0;
            if(request.get("currentRound") != null) {
                currentRound = (Integer) request.get("currentRound");
            }
            request.put("currentRound", currentRound + 1);
            request.put("preAgent", activityId);

            if("TERMINATE".equals(answer)) {
                String jumpActivityId = "end";

                log.error("{} (jump {}):\n--------------------------------------------------------------------------------\n", activityId, jumpActivityId);

                Map<String, Object> response = new LinkedHashMap<>();
                SmartEngine smartEngine = executionContext.getProcessEngineConfiguration().getSmartEngine();
                ExecutionCommandService executionCommandService = smartEngine.getExecutionCommandService();
                AgentExecutionCommandService agentExecutionCommandService = (AgentExecutionCommandService) executionCommandService;
                ProcessInstance newProcessInstance = agentExecutionCommandService.jumpFrom(processInstance, jumpActivityId, null, request, response);
                log.info("EntryAgentDelegation jumpFrom {} response is {}", jumpActivityId, JSON.toJSONString(response));
            } else {
                String jumpActivityId = answer;

                log.error("{} (to {}):\n{}\n--------------------------------------------------------------------------------", activityId, jumpActivityId, answer);

                Map<String, Object> response = new LinkedHashMap<>();
                SmartEngine smartEngine = executionContext.getProcessEngineConfiguration().getSmartEngine();
                ExecutionCommandService executionCommandService = smartEngine.getExecutionCommandService();
                AgentExecutionCommandService agentExecutionCommandService = (AgentExecutionCommandService) executionCommandService;
                ProcessInstance newProcessInstance = agentExecutionCommandService.jumpFrom(processInstance, jumpActivityId, null, request, response);
                log.info("ExecutionAgentDelegation jumpFrom {} response is {}", jumpActivityId, JSON.toJSONString(response));
            }
            Map<String, Object> result = new HashMap<>();
            return result;
        } else {
            String routeAgent = properties.getString("routeAgent");

            String rolePrompt = systemPrompt;
            List<BaseMessage> messages = new ArrayList<>();
            messages.add(new SystemMessage(rolePrompt));
            messages.add(new HumanMessage(query));
//                List<BaseMessage> historyMessages = MessageBuildingUtils.buildMessageReturnHistory(systemContext, messages, this, null, new ArrayList<>());
            ChatPromptTemplate prompt = ChatPromptTemplate.fromChatMessages(messages);

//            BaseLanguageModel model = new IdealabChatModel();
//            model.setModel(IdealabModelEnum.GPT_4_8K.getModel());
            BaseLanguageModel model = new ChatModelOpenAI();
            model.setModel(OpenAIModelConstants.GPT_4);
            model.setTemperature(0.7d);
            com.alibaba.langengine.core.runnables.Runnable agent = Runnable.sequence(
                    prompt,
                    model);

            RunnableConfig config = new RunnableConfig();
            Object runnableOutput = agent.invoke(new RunnableHashMap(), config);
            log.info("LlmCallingDelegation agent invoke response:{}", JSON.toJSONString(runnableOutput));
            String answer = null;
            if (runnableOutput instanceof RunnableHashMap) {
                if (((RunnableHashMap) runnableOutput).get("output") != null) {
                    answer = ((RunnableHashMap) runnableOutput).get("output").toString();
                } else {
                    answer = JSON.toJSONString(runnableOutput);
                }
            } else if (runnableOutput instanceof AIMessage) {
                answer = ((AIMessage) runnableOutput).getContent();
            }

            List<BaseMessage> historyMessages;
            if(request.get("historyMessages") != null) {
                historyMessages = (List<BaseMessage>) request.get("historyMessages");
            } else {
                historyMessages = new ArrayList<>();
            }

            historyMessages.add(new AIMessage(answer));
            request.put("historyMessages", historyMessages);

            Integer currentRound = 0;
            if(request.get("currentRound") != null) {
                currentRound = (Integer) request.get("currentRound");
            }
            request.put("currentRound", currentRound + 1);
            request.put("preAgent", activityId);

            if(!StringUtils.isEmpty(routeAgent)) {
                if("TERMINATE".equals(answer)) {
                    String jumpActivityId = "end";

                    log.error("{} (jump {}):\n--------------------------------------------------------------------------------\n", activityId, jumpActivityId);

                    Map<String, Object> response = new LinkedHashMap<>();
                    SmartEngine smartEngine = executionContext.getProcessEngineConfiguration().getSmartEngine();
                    ExecutionCommandService executionCommandService = smartEngine.getExecutionCommandService();
                    AgentExecutionCommandService agentExecutionCommandService = (AgentExecutionCommandService) executionCommandService;
                    ProcessInstance newProcessInstance = agentExecutionCommandService.jumpFrom(processInstance, jumpActivityId, null, request, response);
                    log.info("EntryAgentDelegation jumpFrom {} response is {}", jumpActivityId, JSON.toJSONString(response));
                } else {
                    log.error("{} (to {}):\n{}\n--------------------------------------------------------------------------------", activityId, routeAgent, answer);

                    Map<String, Object> response = new LinkedHashMap<>();
                    SmartEngine smartEngine = executionContext.getProcessEngineConfiguration().getSmartEngine();
                    ExecutionCommandService executionCommandService = smartEngine.getExecutionCommandService();
                    AgentExecutionCommandService agentExecutionCommandService = (AgentExecutionCommandService) executionCommandService;
                    ProcessInstance newProcessInstance = agentExecutionCommandService.jumpFrom(processInstance, routeAgent, null, request, response);
                    log.info("LlmCallingDelegation jumpFrom {} response is {}", routeAgent, JSON.toJSONString(response));
                }
                Map<String, Object> result = new HashMap<>();
                return result;
            } else {
                log.error("{}:\n{}\n--------------------------------------------------------------------------------", activityId, answer);

                Map<String, Object> input = new HashMap<>();
                input.put("roles", backstory);
                input.put("agentlist", agentRoles);
                rolePrompt = PromptConverter.replacePrompt(ROUTING_AGENT_PROMPT, input);
                messages = new ArrayList<>();
                messages.add(new SystemMessage(rolePrompt));
                messages.add(new HumanMessage(query));

                if(request.get("historyMessages") != null) {
                    historyMessages = (List<BaseMessage>) request.get("historyMessages");
                    messages.addAll(historyMessages);
                } else {
                    historyMessages = new ArrayList<>();
                }

                prompt = ChatPromptTemplate.fromChatMessages(messages);

//                model = new IdealabChatModel();
//                model.setModel(IdealabModelEnum.GPT_4_8K.getModel());
                model = new ChatModelOpenAI();
                model.setModel(OpenAIModelConstants.GPT_4);
                model.setTemperature(0.7d);
                agent = Runnable.sequence(
                        prompt,
                        model);

                config = new RunnableConfig();
                runnableOutput = agent.invoke(new RunnableHashMap(), config);
                log.info("ExecutionAgentDelegation agent invoke response:{}", JSON.toJSONString(runnableOutput));
                answer = null;
                if (runnableOutput instanceof RunnableHashMap) {
                    if (((RunnableHashMap) runnableOutput).get("output") != null) {
                        answer = ((RunnableHashMap) runnableOutput).get("output").toString();
                    } else {
                        answer = JSON.toJSONString(runnableOutput);
                    }
                } else if (runnableOutput instanceof AIMessage) {
                    answer = ((AIMessage) runnableOutput).getContent();
                }

                historyMessages.add(new AIMessage(answer));
                request.put("historyMessages", historyMessages);

                if(request.get("currentRound") != null) {
                    currentRound = (Integer) request.get("currentRound");
                }
                request.put("currentRound", currentRound + 1);
                request.put("preAgent", activityId);

                if("TERMINATE".equals(answer)) {
                    String jumpActivityId = "end";

                    log.error("{} (jump {}):\n--------------------------------------------------------------------------------\n", activityId, jumpActivityId);

                    Map<String, Object> response = new LinkedHashMap<>();
                    SmartEngine smartEngine = executionContext.getProcessEngineConfiguration().getSmartEngine();
                    ExecutionCommandService executionCommandService = smartEngine.getExecutionCommandService();
                    AgentExecutionCommandService agentExecutionCommandService = (AgentExecutionCommandService) executionCommandService;
                    ProcessInstance newProcessInstance = agentExecutionCommandService.jumpFrom(processInstance, jumpActivityId, null, request, response);
                    log.info("EntryAgentDelegation jumpFrom {} response is {}", jumpActivityId, JSON.toJSONString(response));
                } else {
                    String jumpActivityId = answer;

                    log.error("{} (to {}):\n{}\n--------------------------------------------------------------------------------", activityId, jumpActivityId, answer);

                    Map<String, Object> response = new LinkedHashMap<>();
                    SmartEngine smartEngine = executionContext.getProcessEngineConfiguration().getSmartEngine();
                    ExecutionCommandService executionCommandService = smartEngine.getExecutionCommandService();
                    AgentExecutionCommandService agentExecutionCommandService = (AgentExecutionCommandService) executionCommandService;
                    ProcessInstance newProcessInstance = agentExecutionCommandService.jumpFrom(processInstance, jumpActivityId, null, request, response);
                    log.info("ExecutionAgentDelegation jumpFrom {} response is {}", jumpActivityId, JSON.toJSONString(response));
                }
                Map<String, Object> result = new HashMap<>();
                return result;
            }
        }
    }
}
