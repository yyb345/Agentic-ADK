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
//package com.alibaba.langengine.multiagent.delegation.base;
//
//import com.alibaba.agentmagic.framework.behavior.AgentExecutionCommandService;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
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
//import com.alibaba.langengine.idealab.model.IdealabChatModel;
//import com.alibaba.langengine.idealab.model.service.IdealabModelEnum;
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
//        ProcessInstance processInstance = executionContext.getProcessInstance();
//        String agentStrategy = properties.getString("agentStrategy");
//        String agentType = properties.getString("agentType");
//        Integer maxRound = properties.getInteger("maxRound");
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
//                input.put("roles", "user_proxy: 你是一个人类代理\n" +
//                        "getWeather: 你是一个天气预报助理，只会搜索城市的天气，你知道今天杭州天气是10摄氏度\n" +
//                        "transportation: 你是一个交通查询小助手，你知道杭州今天的交通是比较拥堵，注意错峰");
//                input.put("agentlist", "[\"user_proxy\",\"getWeather\",\"transportation\"]");
//                String rolePrompt = PromptConverter.replacePrompt(ENTRY_AGENT_PROMPT, input);
//                List<BaseMessage> messages = new ArrayList<>();
//                messages.add(new SystemMessage(rolePrompt));
//                messages.add(new HumanMessage("今天杭州的天气如何，再帮我查下杭州的交通情况"));
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
//                model.setModel(IdealabModelEnum.GPT_4_8K.getModel());
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
//                    Map<String, Object> response = new LinkedHashMap<>();
//                    SmartEngine smartEngine = executionContext.getProcessEngineConfiguration().getSmartEngine();
//                    ExecutionCommandService executionCommandService = smartEngine.getExecutionCommandService();
//                    AgentExecutionCommandService agentExecutionCommandService = (AgentExecutionCommandService) executionCommandService;
//                    ProcessInstance newProcessInstance = agentExecutionCommandService.jumpFrom(processInstance, jumpActivityId, null, request, response);
//                    log.info("EntryAgentDelegation jumpFrom {} response is {}", jumpActivityId, JSON.toJSONString(response));
//                }
//            }
//        }
//        return "";
//    }
//}
