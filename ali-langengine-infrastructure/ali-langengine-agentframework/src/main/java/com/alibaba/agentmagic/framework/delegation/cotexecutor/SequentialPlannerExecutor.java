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
package com.alibaba.agentmagic.framework.delegation.cotexecutor;

import com.alibaba.agentmagic.framework.delegation.FrameworkCotCallingDelegation;
import com.alibaba.agentmagic.framework.delegation.cotexecutor.outputparser.CotSequentialPlannerOutputParser;
import com.alibaba.agentmagic.framework.delegation.cotexecutor.support.MessageBuildingUtils;
import com.alibaba.agentmagic.framework.delegation.cotexecutor.support.ToolBuildingUtils;
import com.alibaba.agentmagic.framework.utils.AgentResponseUtils;
import com.alibaba.agentmagic.framework.utils.ThreadLocalUtils;
import com.alibaba.agentmagic.framework.domain.LlmExecuteLog;
import com.alibaba.agentmagic.framework.domain.ToolExecuteLog;
import com.alibaba.agentmagic.framework.utils.IdGeneratorUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.agentframework.model.AgentResult;
import com.alibaba.langengine.agentframework.model.agent.domain.AgentRelation;
import com.alibaba.langengine.agentframework.model.domain.*;
import com.alibaba.langengine.agentframework.model.enums.AgentMagicErrorCode;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.langengine.agentframework.model.service.request.LanguageModelGetRequest;
import com.alibaba.langengine.agentframework.model.service.response.LanguageModelGetResponse;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessageRole;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionDefinition;
import com.alibaba.langengine.core.prompt.PromptConverter;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.core.runnables.*;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.StructuredTool;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * SemanticKernel-SequentialPlanner执行器
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class SequentialPlannerExecutor extends BaseCotExecutor {

    @Override
    public Map<String, Object> callLlm(FrameworkSystemContext systemContext,
                                       FrameworkCotCallingDelegation delegation,
                                       String knowledgeContext) {
        Long startTime = System.currentTimeMillis();
        log.info("SequentialPlannerExecutor startTime:" + startTime);

        Consumer<Object> chunkConsumer = systemContext.getChunkConsumer();
        String sessionId = systemContext.getSessionId();
        String userId = systemContext.getUserId();
        String requestId = systemContext.getRequestId();
        String agentCode = systemContext.getAgentCode();
        AgentRelation agentRelation = systemContext.getAgentRelation();

        List<BaseTool> tools = new ArrayList<>();
        List<FunctionDefinition> functions = new ArrayList<>();
        ToolBuildingUtils.buildTool(systemContext, tools, functions, startTime, delegation, true);
        Map<String, BaseTool> toolMap = new HashMap<>();
        for (BaseTool tool : tools) {
            String skFunction = String.format("%s.%s", tool.getName(), tool.getFunctionName());
            toolMap.put(skFunction, tool);
        }

        List<BaseMessage> messages = new ArrayList<>();
        String rolePrompt = SEQUENTIAL_PLANNER_SYSTEM_PROMPT;

        Map<String, Object> args = new HashMap<>();
        args.put("available_functions", convertSemanticKernelAgentSequentialPlannerTools(tools));
        args.put("input", systemContext.getQuery());
        rolePrompt = PromptConverter.replacePrompt(rolePrompt, args);

        List<BaseMessage> historyMessages = MessageBuildingUtils.buildMessageReturnHistory(systemContext, messages, delegation, knowledgeContext, rolePrompt, null);
        ChatPromptTemplate prompt = ChatPromptTemplate.fromChatMessages(messages);

        LlmTemplateConfig llmTemplateConfig = convertCotLlmTemplateConfig(delegation.getCotLlmTemplateConfig(), false, systemContext, delegation);
        LanguageModelGetRequest request = new LanguageModelGetRequest();
        request.setLlmTemplateConfig(llmTemplateConfig);
        request.setSystemContext(systemContext);
        request.setFlag("cotLlmFetch");
        AgentResult<LanguageModelGetResponse> agentResult = delegation.getLanguageModelService().getLanguageModel(request);
        if(!agentResult.isSuccess()) {
            throw new AgentMagicException(AgentMagicErrorCode.COT_SYSTEM_ERROR, agentResult.getErrorMsg(), systemContext.getRequestId());
        }
        BaseLanguageModel model = agentResult.getData().getLanguageModel();

        RunnableInterface modelBinding =  model.bind(new HashMap<String, Object>() {{
            put("stop", Arrays.asList(new String[] { "\n<!-- END -->" }));
        }});

        CotSequentialPlannerOutputParser outputParser = new CotSequentialPlannerOutputParser();
        outputParser.setToolMap(toolMap);

        RunnableAgent agent = new RunnableAgent(Runnable.sequence(
                prompt,
                modelBinding
        ), outputParser);

        RunnableAgentExecutor agentExecutor = new RunnableAgentExecutor(agent, tools, t -> {
            RunnableHashMap runnableHashMap = new RunnableHashMap();
            List<FunctionDefinition> filterFunctions = functions.stream().filter(func -> t.stream().anyMatch(e -> e.getName().equals(func.getName()))).collect(Collectors.toList());
            runnableHashMap.put("functions", filterFunctions);
            return runnableHashMap;
        });

        // TODO 待优化
//        agentExecutor.setMaxIterations(3);

        if (chunkConsumer != null) {
            String sectionId = IdGeneratorUtils.nextId();
            LlmTemplateConfig finalLlmTemplateConfig = llmTemplateConfig;
            Consumer<Object> agentConsumer = e -> {
                if (e instanceof AIMessage) {
                    AIMessage aiMessage = (AIMessage) e;
                    log.info("aiMessage chunk:" + JSON.toJSONString(aiMessage));

                    String firstCost = null;
                    try {
                        firstCost = ThreadLocalUtils.getFirstCost();
                        if(firstCost == null) {
                            firstCost = AgentResponseUtils.getTimeCost(startTime);
                            ThreadLocalUtils.set(firstCost);
                        }
                    } catch (Exception ex) {
                        log.error("AIMessage ThreadLocalUtils.getFirstCost error:" + firstCost, ex);
                    }

                    String content = null;
                    boolean matchMessageReturn = false;
                    if(("AppEngineChatModel".equals(finalLlmTemplateConfig.getModelTemplate())
                            && ("gpt-4o".equals(finalLlmTemplateConfig.getModelName())
                            || "gpt-35-turbo-16k".equals(finalLlmTemplateConfig.getModelName())
                            || "gpt-35-turbo".equals(finalLlmTemplateConfig.getModelName())
                            || "Marco-Pro-32K".equals(finalLlmTemplateConfig.getModelName())
                            || "Marco-Max-32K".equals(finalLlmTemplateConfig.getModelName())
                    ))
                            || ("IdealabOpenAIChatModel".equals(finalLlmTemplateConfig.getModelTemplate())
                            && ("gpt-4o-0513".equals(finalLlmTemplateConfig.getModelName())))) {
                        log.info("aiMessage.getAdditionalKwargs is " + JSON.toJSONString(aiMessage.getAdditionalKwargs()));
                        if (aiMessage.getAdditionalKwargs() == null) {
                            matchMessageReturn = true;
                            content = aiMessage.getContent();
                        }
                    } else {
                        Pattern pattern = Pattern.compile("Final Answer:\\s*((?:[^\\\\]|\\\\.)*)", Pattern.DOTALL);
                        Matcher matcher = pattern.matcher(aiMessage.getContent());
                        if (matcher.find()) {
                            matchMessageReturn = true;
                            content = matcher.group(1);
                        }
                    }

                    if (matchMessageReturn) {
                        AgentAPIInvokeResponse agentAPIResponse = new AgentAPIInvokeResponse();
                        ChatMessage message = new ChatMessage();
                        agentAPIResponse.getMessage().add(message);

                        String messageId = IdGeneratorUtils.nextId();
                        message.setRole(ChatMessageRole.ASSISTANT.value());
                        message.setType(ChatMessage.TYPE_ANSWER);
                        message.setContent(content);
                        message.setMessageId(messageId);
                        message.setSectionId(sectionId);
                        message.setSessionId(sessionId);
                        message.setContentType(ChatMessage.CONTENT_TYPE_TEXT);
                        message.setSenderId(userId);
                        message.getExtraInfo().setTimeCost(AgentResponseUtils.getTimeCost(startTime));
                        message.getExtraInfo().setFirstCost(firstCost);

                        AgentAPIResult<AgentAPIInvokeResponse> apiResult = AgentAPIResult.success(agentAPIResponse, requestId);

                        delegation.onStreamNext(systemContext, apiResult);
                    }
                } else if(e instanceof RunnableTraceData) {
                    RunnableTraceData traceData = (RunnableTraceData) e;
                    log.info("traceData:" + JSON.toJSONString(traceData));
                    if ("onLlmEnd".equals(traceData.getStep())
                            || "onLlmError".equals(traceData.getStep())) {
                        LlmExecuteLog llmExecuteLog = new LlmExecuteLog();
                        llmExecuteLog.setAgentCode(agentCode);
                        llmExecuteLog.setExecuteTime(traceData.getExecuteTime());
                        llmExecuteLog.setStream("true");
                        llmExecuteLog.setModelName(traceData.getModelName());
                        llmExecuteLog.setModelType(traceData.getModelType());
                        llmExecuteLog.setRequestId(requestId);
                        llmExecuteLog.setSuccess(traceData.getSuccess());
                        llmExecuteLog.setCode(traceData.getCode());
                        llmExecuteLog.setMessage(traceData.getMessage());
                        llmExecuteLog.setUserId(userId);
                        if(ThreadLocalUtils.getFirstCost() != null) {
                            Double firstCost = Double.parseDouble(ThreadLocalUtils.getFirstCost()) * 1000;
                            llmExecuteLog.setFirstCost(firstCost.toString());
                        }
                        llmExecuteLog.doLog();
                    } else if("onToolEnd".equals(traceData.getStep())
                            || "onToolError".equals(traceData.getStep())) {
                        ToolExecuteLog toolExecuteLog = new ToolExecuteLog();
                        toolExecuteLog.setAgentCode(agentCode);
                        toolExecuteLog.setExecuteTime(traceData.getExecuteTime());
                        toolExecuteLog.setToolName(traceData.getToolName());
                        toolExecuteLog.setToolDesc(traceData.getToolDesc());
                        toolExecuteLog.setRequestId(requestId);
                        toolExecuteLog.setSuccess(traceData.getSuccess());
                        toolExecuteLog.setCode(traceData.getCode());
                        toolExecuteLog.setMessage(traceData.getMessage());
                        toolExecuteLog.setUserId(userId);
                        toolExecuteLog.doLog();
                    }
                }
            };

            RunnableConfig config = new RunnableConfig();
            config.setStreamLog(true);
            Object runnableOutput = agentExecutor.stream(new RunnableHashMap(), config, agentConsumer);
            log.info("runnableOutput stream:" + JSON.toJSONString(runnableOutput));

            //是否建议
            if (agentRelation.isLlmSuggestEnabled()) {
                String answer = null;
                if(runnableOutput instanceof RunnableHashMap) {
                    if(((RunnableHashMap) runnableOutput).get("output") != null) {
                        answer = ((RunnableHashMap) runnableOutput).get("output").toString();
                    } else {
                        answer = JSON.toJSONString(runnableOutput);
                    }
                }
                callLlmSuggest(model, historyMessages, answer, systemContext, delegation);
            }

            if(!(runnableOutput instanceof RunnableHashMap)) {
                throw new AgentMagicException(AgentMagicErrorCode.SYSTEM_ERROR,
                        "SequentialPlanner callLlm error, output is not RunnableHashMap",
                        systemContext.getRequestId());
            }
            // 再走一次QA大模型
            List<BaseMessage> qaMessages = new ArrayList<>();
            HumanMessage humanMessage = new HumanMessage();
            humanMessage.setContent(systemContext.getQuery());
            AIMessage aiMessage = new AIMessage();
            aiMessage.setContent(((Map<String, Object>)runnableOutput).get("output").toString());
            List<BaseMessage> qaIntermediateMessages = new ArrayList<>();
            qaIntermediateMessages.add(humanMessage);
            qaIntermediateMessages.add(aiMessage);
            List<BaseMessage> qaHistoryMessages = MessageBuildingUtils.buildMessageReturnHistory(systemContext, qaMessages,
                    delegation, knowledgeContext, null, qaIntermediateMessages, "我知道最后答案了，请给我总结一下");
            ChatPromptTemplate qaPrompt = ChatPromptTemplate.fromChatMessages(qaMessages);
            return invokeLangRunnableChain(qaPrompt, model, llmTemplateConfig, qaHistoryMessages, startTime, systemContext, delegation);

//            if(runnableOutput instanceof AIMessage) {
//                Object finalRunnableOutput = runnableOutput;
//                return new HashMap<String, Object>() {{
//                    put("output", ((AIMessage) finalRunnableOutput).getContent());
//                }};
//            } else {
//                return (Map<String, Object>) runnableOutput;
//            }
        } else {
            Object runnableOutput = agentExecutor.invoke(new RunnableHashMap());
            log.info("runnableOutput invoke:" + JSON.toJSONString(runnableOutput));
            if(runnableOutput instanceof AIMessage) {
                Object finalRunnableOutput = runnableOutput;
                Map<String, Object> result = new HashMap<String, Object>() {{
                    put("output", ((AIMessage) finalRunnableOutput).getContent());
                }};
                return result;
            } else {
                return (Map<String, Object>) runnableOutput;
            }
        }
    }

    private String convertSemanticKernelAgentSequentialPlannerTools(List<BaseTool> tools) {
        List<String> structSchemas = new ArrayList<>();
        Map<String, BaseTool> toolMap = new HashMap<>();
        for (BaseTool tool : tools) {
            if(tool instanceof StructuredTool) {
                String skFunction = String.format("%s.%s", tool.getName(), tool.getFunctionName());
                toolMap.put(skFunction, tool);

                StructuredTool structuredTool = (StructuredTool)tool;
                String structSchema = structuredTool.formatSemantickernelBasicPrompt("inputs");
                structSchemas.add(structSchema);
            }
        }
        return String.join("\n\n", structSchemas);
    }
}
