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
//public class ExecutionAgentDelegation extends MultiAgentDelegationBase<Map<String, Object>> {
//
//    @Override
//    public Map<String, Object> executeInternal(ExecutionContext executionContext, JSONObject properties, JSONObject request) {
//        log.info("LlmCallingDelegation start");
//        ProcessInstance processInstance = executionContext.getProcessInstance();
//        String activityId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();
//        String query = request.getString("query");
//        String refEntryAgent = properties.getString("refEntryAgent");
//        String agentType = properties.getString("agentType");
//        String systemPrompt = properties.getString("systemPrompt");
//
//        if("llm".equals(agentType)) {
//            String rolePrompt = systemPrompt;
//            List<BaseMessage> messages = new ArrayList<>();
//            messages.add(new SystemMessage(rolePrompt));
//            messages.add(new HumanMessage(query));
////                List<BaseMessage> historyMessages = MessageBuildingUtils.buildMessageReturnHistory(systemContext, messages, this, null, new ArrayList<>());
//            ChatPromptTemplate prompt = ChatPromptTemplate.fromChatMessages(messages);
//
//            BaseLanguageModel model = new IdealabChatModel();
//            model.setModel(IdealabModelEnum.GPT_4_8K.getModel());
////            model.setTemperature(0d);
//            Runnable agent = Runnable.sequence(
//                    prompt,
//                    model);
//
//            RunnableConfig config = new RunnableConfig();
//            Object runnableOutput = agent.invoke(new RunnableHashMap(), config);
//            log.info("LlmCallingDelegation agent invoke response:{}", JSON.toJSONString(runnableOutput));
//            String answer = null;
//            if (runnableOutput instanceof RunnableHashMap) {
//                if (((RunnableHashMap) runnableOutput).get("output") != null) {
//                    answer = ((RunnableHashMap) runnableOutput).get("output").toString();
//                } else {
//                    answer = JSON.toJSONString(runnableOutput);
//                }
//            } else if (runnableOutput instanceof AIMessage) {
//                answer = ((AIMessage) runnableOutput).getContent();
//            }
//
//            if(activityId.equals("getWeather")) {
//                answer = "今天杭州的天气为10摄氏度";
//            } else if(activityId.equals("transportation")) {
//                answer = "今天杭州交通比较拥堵，注意错峰";
//            }
//
//            List<BaseMessage> historyMessages;
//            if(request.get("historyMessages") != null) {
//                historyMessages = (List<BaseMessage>) request.get("historyMessages");
//            } else {
//                historyMessages = new ArrayList<>();
//            }
//
//            if("agent_guess_number".equals(answer)) {
//                historyMessages.add(new HumanMessage(answer));
//                request.put("historyMessages", historyMessages);
//            } else {
//                historyMessages.add(new AIMessage(answer));
//                request.put("historyMessages", historyMessages);
//            }
//
//            Integer currentRound = 0;
//            if(request.get("currentRound") != null) {
//                currentRound = (Integer) request.get("currentRound");
//            }
//            request.put("currentRound", currentRound + 1);
//            request.put("preAgent", activityId);
//
//            log.error("{} (to {}):\n{}\n--------------------------------------------------------------------------------", activityId, refEntryAgent, answer);
//
//
//            Map<String, Object> response = new LinkedHashMap<>();
//            SmartEngine smartEngine = executionContext.getProcessEngineConfiguration().getSmartEngine();
//            ExecutionCommandService executionCommandService = smartEngine.getExecutionCommandService();
//            AgentExecutionCommandService agentExecutionCommandService = (AgentExecutionCommandService) executionCommandService;
//            ProcessInstance newProcessInstance = agentExecutionCommandService.jumpFrom(processInstance, refEntryAgent, null, request, response);
//            log.info("LlmCallingDelegation jumpFrom {} response is {}", refEntryAgent, JSON.toJSONString(response));
//        }
//
//        Map<String, Object> result = new HashMap<>();
//        return result;
//    }
//}
