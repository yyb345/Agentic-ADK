///**
// * Copyright (C) 2024 AIDC-AI
// * <p>
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.alibaba.langengine.multiagent.delegation.base2;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.alibaba.langengine.agentframework.behavior.AgentExecutionCommandService;
//import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
//import com.alibaba.langengine.core.messages.AIMessage;
//import com.alibaba.langengine.core.messages.BaseMessage;
//import com.alibaba.langengine.core.messages.HumanMessage;
//import com.alibaba.langengine.core.messages.SystemMessage;
//import com.alibaba.langengine.core.prompt.PromptConverter;
//import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
//import com.alibaba.langengine.core.runnables.Runnable;
//import com.alibaba.langengine.core.runnables.RunnableConfig;
//import com.alibaba.langengine.core.runnables.RunnableHashMap;
//import com.alibaba.langengine.multiagent.delegation.base.MultiAgentDelegationBase;
//import com.alibaba.smart.framework.engine.SmartEngine;
//import com.alibaba.smart.framework.engine.context.ExecutionContext;
//import com.alibaba.smart.framework.engine.model.instance.ProcessInstance;
//import com.alibaba.smart.framework.engine.service.command.ExecutionCommandService;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.*;
//
//@Slf4j
//public class EntryAgentDelegation extends MultiAgentDelegationBase<String> {
//
//    private static final String ENTRY_AGENT_PROMPT = "You are in a role play game. The following roles are available:\n" +
//            "{roles}.\n" +
//            "Read the following conversation.\n" +
//            "Then select the next role from {agentlist} to play. Only return the role." +
//            "When you find an answer, verify the answer carefully. Include verifiable evidence in your response if possible.\n" +
//            "Reply \"TERMINATE\" in the end when everything is done.";
//
//    @Override
//    public String executeInternal(ExecutionContext executionContext, JSONObject properties, JSONObject request) {
//        log.info("EntryAgentDelegation start");
//        String activityId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();
//        ProcessInstance processInstance = executionContext.getProcessInstance();
//        String query = request.getString("query");
//        String agentStrategy = properties.getString("agentStrategy");
//        String agentType = properties.getString("agentType");
//        Integer maxRound = properties.getInteger("maxRound");
//        String executionAgentPrompt = properties.getString("executionAgentPrompt");
//        if("supervisor".equals(agentStrategy)) {
//            if("llm".equals(agentType)) {
//
//                if(maxRound != null && maxRound > 0) {
//                    if(request.get("currentRound") != null) {
//                        Integer currentRound = (Integer) request.get("currentRound");
//                        log.info("current round is {}", currentRound);
//                        if(currentRound >= maxRound) {
//                            log.info("EntryAgentDelegation finished");
//                            return null;
//                        }
//                    }
//                }
//
//                Map<String, Object> input = new HashMap<>();
//                input.put("roles", executionAgentPrompt);
//                String rolePrompt = PromptConverter.replacePrompt(ENTRY_AGENT_PROMPT, input);
//                List<BaseMessage> messages = new ArrayList<>();
//                messages.add(new SystemMessage(rolePrompt));
//                messages.add(new HumanMessage(query));
//
//                List<BaseMessage> historyMessages;
//                if(request.get("historyMessages") != null) {
//                    historyMessages = (List<BaseMessage>) request.get("historyMessages");
//                    messages.addAll(historyMessages);
//                }
//
////                List<BaseMessage> historyMessages = MessageBuildingUtils.buildMessageReturnHistory(systemContext, messages, this, null, new ArrayList<>());
//                ChatPromptTemplate prompt = ChatPromptTemplate.fromChatMessages(messages);
//
//                BaseLanguageModel model = new IdealabChatModel();
//                model.setModel(IdealabModelEnum.GPT_4_O_0513.getModel());
////                model.setTemperature(0d);
//                Runnable agent = Runnable.sequence(
//                        prompt,
//                        model);
//
//                RunnableConfig config = new RunnableConfig();
//                Object runnableOutput = agent.invoke(new RunnableHashMap(), config);
//                log.info("EntryAgentDelegation agent invoke response:{}", JSON.toJSONString(runnableOutput));
//                String answer = null;
//                if (runnableOutput instanceof RunnableHashMap) {
//                    if (((RunnableHashMap) runnableOutput).get("output") != null) {
//                        answer = ((RunnableHashMap) runnableOutput).get("output").toString();
//                    } else {
//                        answer = JSON.toJSONString(runnableOutput);
//                    }
//                } else if (runnableOutput instanceof AIMessage) {
//                    answer = ((AIMessage) runnableOutput).getContent();
//                }
//
//                if("TERMINATE".equals(answer)) {
//                    String jumpActivityId = "end";
//
//                    log.error("{} (jump {}):\n--------------------------------------------------------------------------------\n", activityId, jumpActivityId);
//
//                    Map<String, Object> response = new LinkedHashMap<>();
//                    SmartEngine smartEngine = executionContext.getProcessEngineConfiguration().getSmartEngine();
//                    ExecutionCommandService executionCommandService = smartEngine.getExecutionCommandService();
//                    AgentExecutionCommandService agentExecutionCommandService = (AgentExecutionCommandService) executionCommandService;
//                    ProcessInstance newProcessInstance = agentExecutionCommandService.jumpFrom(processInstance, jumpActivityId, null, request, response);
//                    log.info("EntryAgentDelegation jumpFrom {} response is {}", jumpActivityId, JSON.toJSONString(response));
//
//                } else {
//                    if (maxRound != null && maxRound > 0) {
//                        Integer currentRound = 0;
//                        if (request.get("currentRound") != null) {
//                            currentRound = (Integer) request.get("currentRound");
//                        }
//                        request.put("currentRound", currentRound + 1);
//                    }
//                    String jumpActivityId = answer;
//
//                    log.error("{} (assign {}):\n--------------------------------------------------------------------------------", activityId, jumpActivityId);
//
//                    Map<String, Object> response = new LinkedHashMap<>();
//                    SmartEngine smartEngine = executionContext.getProcessEngineConfiguration().getSmartEngine();
//                    ExecutionCommandService executionCommandService = smartEngine.getExecutionCommandService();
//                    AgentExecutionCommandService agentExecutionCommandService = (AgentExecutionCommandService) executionCommandService;
//                    ProcessInstance newProcessInstance = agentExecutionCommandService.jumpFrom(processInstance, jumpActivityId, null, request, response);
//                    log.info("EntryAgentDelegation jumpFrom {} response is {}", jumpActivityId, JSON.toJSONString(response));
//                }
//            }
//        } else if("network".equals(agentStrategy)) {
//            if("none".equals(agentType)) {
//
//                if (maxRound != null && maxRound > 0) {
//                    if (request.get("currentRound") != null) {
//                        Integer currentRound = (Integer) request.get("currentRound");
//                        log.info("current round is {}", currentRound);
//                        if (currentRound >= maxRound) {
//                            log.info("EntryAgentDelegation finished");
//                            return null;
//                        }
//                    }
//                }
//
//                String jumpActivityId;
//                String subAgents = (String) properties.getString("subAgents");
//                String[] subAgentArray = subAgents.split(",");
//                if(request.get("preAgent") != null) {
//                    jumpActivityId = (String) request.get("preAgent");
//                    List<String> list = new ArrayList<>(Arrays.asList(subAgentArray));
//                    list.remove(jumpActivityId);
//                    jumpActivityId = list.get(0);
//                } else {
//                    jumpActivityId = subAgentArray[0];
//                }
//
////                    log.error("{} (assign {}):\n--------------------------------------------------------------------------------", activityId, jumpActivityId);
//
//                Map<String, Object> response = new LinkedHashMap<>();
//                SmartEngine smartEngine = executionContext.getProcessEngineConfiguration().getSmartEngine();
//                ExecutionCommandService executionCommandService = smartEngine.getExecutionCommandService();
//                AgentExecutionCommandService agentExecutionCommandService = (AgentExecutionCommandService) executionCommandService;
//                ProcessInstance newProcessInstance = agentExecutionCommandService.jumpFrom(processInstance, jumpActivityId, null, request, response);
//                log.info("EntryAgentDelegation jumpFrom {} response is {}", jumpActivityId, JSON.toJSONString(response));
//            }
//        }
//        return "";
//    }
//}
