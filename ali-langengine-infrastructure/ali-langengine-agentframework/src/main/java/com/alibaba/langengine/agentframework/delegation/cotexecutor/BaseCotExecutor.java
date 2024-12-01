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
package com.alibaba.langengine.agentframework.delegation.cotexecutor;

import com.alibaba.langengine.agentframework.delegation.cotexecutor.outputparser.BotChatOutputParser;
import com.alibaba.langengine.agentframework.delegation.cotexecutor.tools.ComponentTool;
import com.alibaba.langengine.agentframework.delegation.FrameworkCotCallingDelegation;
import com.alibaba.langengine.agentframework.delegation.cotexecutor.support.MessageBuildingUtils;
import com.alibaba.langengine.agentframework.delegation.cotexecutor.support.StreamMessageUtils;
import com.alibaba.langengine.agentframework.delegation.cotexecutor.support.ToolBuildingUtils;
import com.alibaba.langengine.agentframework.delegation.constants.SystemConstant;
import com.alibaba.langengine.agentframework.delegation.provider.DelegationHelper;
import com.alibaba.langengine.agentframework.domain.ProcessExecuteLog;
import com.alibaba.langengine.agentframework.domain.TemplateTypeEnums;
import com.alibaba.langengine.agentframework.utils.AgentResponseUtils;
import com.alibaba.langengine.agentframework.utils.ThreadLocalUtils;
import com.alibaba.langengine.agentframework.delegation.constants.CotCallingConstant;
import com.alibaba.langengine.agentframework.domain.LlmExecuteLog;
import com.alibaba.langengine.agentframework.domain.ToolExecuteLog;
import com.alibaba.langengine.agentframework.utils.IdGeneratorUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.agentframework.model.AgentResult;
import com.alibaba.langengine.agentframework.model.agent.domain.AgentRelation;
import com.alibaba.langengine.agentframework.model.agent.domain.ComponentCallingInput;
import com.alibaba.langengine.agentframework.model.constant.ErrorCodeConstants;
import com.alibaba.langengine.agentframework.model.domain.*;
import com.alibaba.langengine.agentframework.model.enums.AgentMagicErrorCode;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.langengine.agentframework.model.service.LanguageModelService;
import com.alibaba.langengine.agentframework.model.service.ToolCallingService;
import com.alibaba.langengine.agentframework.model.service.request.LanguageModelGetRequest;
import com.alibaba.langengine.agentframework.model.service.request.LanguageModelSuggestGetRequest;
import com.alibaba.langengine.agentframework.model.service.response.LanguageModelGetResponse;
import com.alibaba.langengine.agentframework.model.service.response.LanguageModelSuggestGetResponse;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.FunctionMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessageRole;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionDefinition;
import com.alibaba.langengine.core.prompt.PromptConverter;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.core.runnables.*;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.tokenizers.QwenTokenizer;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * cot执行器基类
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public abstract class BaseCotExecutor implements CotCallingConstant {

    private static final QwenTokenizer tokenizer = new QwenTokenizer();

    /**
     * 执行agent
     *
     * @param systemContext
     * @param delegation
     * @return
     */
    public Map<String, Object> invokeAgent(FrameworkSystemContext systemContext, FrameworkCotCallingDelegation delegation) {
        String query = systemContext.getQuery();
        String requestId = systemContext.getRequestId();
        Consumer<Object> chunkConsumer = systemContext.getChunkConsumer();

        // 判断会话内容为空的异常情况
        if(StringUtils.isEmpty(query)) {
            String errorDetail = "query is empty";
            AgentAPIResult agentAPIResult = AgentAPIResult.fail(ErrorCodeConstants.APPLICATION_ERROR,
                    AgentMagicErrorCode.COT_SYSTEM_ERROR,
                    errorDetail,
                    requestId);
            if(chunkConsumer != null) {
                // 发送流式消息
                delegation.onStreamNext(systemContext, agentAPIResult);
            }
            throw new AgentMagicException(AgentMagicErrorCode.COT_SYSTEM_ERROR, errorDetail, requestId);
        }

        Boolean isInit = systemContext.getIsInit();

        // 第一步，先判断是否返回欢迎语
        if(isInit) {
            return StreamMessageUtils.sendInitStreamMessage(systemContext);
        }
        // 第二步，如果存在knowledgeList，先进行知识库检索
        String knowledgeContext = StreamMessageUtils.sendKnowledgeRetrievalStreamMessage(systemContext, delegation);

        // 第三步，开始规划并执行
        return callLlm(systemContext, delegation, knowledgeContext);
    }

    public void callLlmSuggest(BaseLanguageModel model,
                               List<BaseMessage> historyMessages,
                               String answer,
                               FrameworkSystemContext systemContext,
                               FrameworkCotCallingDelegation delegation) {
        LanguageModelService languageModelService = delegation.getLanguageModelService();
        ProcessExecuteLog processExecuteLog = this.enrichProcessExecuteLog(systemContext);
        try {

            AgentRelation relation = systemContext.getAgentRelation();
            String query = systemContext.getQuery();
            String requestId = systemContext.getRequestId();
            Consumer<Object> chunkConsumer = systemContext.getChunkConsumer();
            String sessionId = systemContext.getSessionId();
            String userId = systemContext.getUserId();

            LanguageModelSuggestGetRequest request = new LanguageModelSuggestGetRequest();
            AgentResult<LanguageModelSuggestGetResponse> agentResult = languageModelService.getSuggestLanguageModel(request);
            if(!agentResult.isSuccess()) {
                throw new AgentMagicException(AgentMagicErrorCode.COT_SYSTEM_ERROR, agentResult.getErrorMsg(), requestId);
            }
            BaseLanguageModel suggestLanguageModel = agentResult.getData().getLanguageModel();

            Long startTime = System.currentTimeMillis();
            List<BaseMessage> messages = new ArrayList<>();

            String input;
            if (relation.isLlmSelfDefineSuggestPrompt() && StringUtils.isNotBlank(relation.getLlmSuggestPrompt())) {
                input = CotCallingConstant.LLM_SUGGEST_PROMPT_PREFIX + relation.getLlmSuggestPrompt() + CotCallingConstant.LLM_SUGGEST_PROMPT_SUFFIX;
            } else {
                input = LLM_SUGGEST_PROMPT;
            }
            log.info("callLlmSuggest, input: {}", input);
            String userPrompt = PromptConverter.replacePrompt(input, new HashMap<String, Object>() {{
                put("input", query);
                put("answer", answer);
            }});
            log.info("callLlmSuggest, requestId, {}, userPrompt: {}", requestId, userPrompt);
//        messages.add(new SystemMessage(systemPrompt));
            if (!CollectionUtils.isEmpty(historyMessages)) {
                messages.addAll(historyMessages);
            }
            messages.add(new HumanMessage(userPrompt));

            ChatPromptTemplate prompt = ChatPromptTemplate.fromChatMessages(messages);

            Runnable agent = Runnable.sequence(
                    prompt,
                    suggestLanguageModel);
            String sectionId = IdGeneratorUtils.nextId();

            // TODO 支持 llmExecuteLog
            processExecuteLog.setRequest(JSON.toJSONString(this.getResponseForSuggestLlm(relation, input)));
            processExecuteLog.setQuery(userPrompt);

            Object runnableOutput = agent.invoke(new RunnableHashMap());
            log.info("llmSuggest response:" + JSON.toJSONString(runnableOutput));

            processExecuteLog.setResponse(JSON.toJSONString(runnableOutput));

            if (chunkConsumer != null) {
                String content;
                if (runnableOutput instanceof AIMessage) {
                    Object finalRunnableOutput = runnableOutput;
                    content = ((AIMessage) finalRunnableOutput).getContent();
                } else {
                    content = JSON.toJSONString(runnableOutput);
                }
                if(StringUtils.isNotBlank(content)) {
                    processExecuteLog.setCostToken(String.valueOf(tokenizer.getTokenCount(content)));
                }

                //解析content，例如：
                //以下是根据用户兴趣点推荐的3个具有区分度的不同问题:\n1. Can you create a loading animation for the app with a dog theme?\n2. What color scheme would complement the logo for the app interface?\n3. Do you have any suggestions for a catchy slogan to go with the cute dog app
                List<String> suggestContents = delegation.extractQuestions(content);
                if (suggestContents.size() > 0) {
                    AgentAPIInvokeResponse agentAPIResponse = new AgentAPIInvokeResponse();
                    ChatMessage message = new ChatMessage();
                    agentAPIResponse.getMessage().add(message);

                    String messageId = IdGeneratorUtils.nextId();
                    message.setRole(ChatMessageRole.ASSISTANT.value());
                    message.setType(ChatMessage.TYPE_FOLLOW_UP);
                    message.setContent(JSON.toJSONString(suggestContents));
                    message.setMessageId(messageId);
                    message.setSectionId(sectionId);
                    message.setSessionId(sessionId);
                    message.setContentType(ChatMessage.CONTENT_TYPE_TEXT);
                    message.setSenderId(userId);
                    message.getExtraInfo().setTimeCost(AgentResponseUtils.getTimeCost(startTime));
                    message.getExtraInfo().setAgentAnswer(answer);
                    AgentAPIResult<AgentAPIInvokeResponse> apiResult = AgentAPIResult.success(agentAPIResponse, requestId);

                    delegation.onStreamNext(systemContext, apiResult);
                } else {
                    log.warn("Not any suggests, the content is:" + content);
                }
                processExecuteLog.setSuccess(true);
                this.recordProcessExecuteLog(processExecuteLog);
            }
        } catch (Throwable e) {
            log.error("callLlmSuggest error", e);
            // 如果用户推荐异常，则忽略返回
            processExecuteLog.setSuccess(false);
            processExecuteLog.setErrorCode("SYSTEM ERROR");
            processExecuteLog.setErrorDetail(e.getMessage());
            this.recordProcessExecuteLog(processExecuteLog);
        }
    }

    private Map<String, Object> getResponseForSuggestLlm(AgentRelation relation, String input) {
        Map<String, Object> result = new HashMap<>();
        boolean selfDefineSuggestPrompt = relation.isLlmSelfDefineSuggestPrompt();
        result.put("selfDefineSuggestPrompt", selfDefineSuggestPrompt);
        if(selfDefineSuggestPrompt) {
            String suggestPrompt = relation.getLlmSuggestPrompt();
            result.put("suggestPrompt", suggestPrompt);
        }
        result.put("input", input);
        return result;
    }

    private ProcessExecuteLog enrichProcessExecuteLog(FrameworkSystemContext systemContext) {
        ProcessExecuteLog processExecuteLog = new ProcessExecuteLog();

        ExecutionContext context = systemContext.getExecutionContext();
        JSONObject request = DelegationHelper.getRequest(context);
        String agentCode = DelegationHelper.getSystemString(request, SystemConstant.AGENT_CODE_KEY);
        String tenantCode = DelegationHelper.getSystemString(request, SystemConstant.TENANT_CODE_KEY);
        String sessionId = DelegationHelper.getSystemString(request, SystemConstant.SESSION_ID_KEY);
        String processDefineId = context.getProcessDefinition().getId();
        String processDefineVersion = context.getProcessDefinition().getVersion();
        String processDefineName = context.getProcessDefinition().getName();
        String processInstanceId = context.getProcessInstance().getInstanceId();
        Boolean stream = DelegationHelper.getSystem(request, SystemConstant.CHUNK_CONSUMER_KEY) != null;
        String requestId = DelegationHelper.getSystemString(request, SystemConstant.REQUEST_ID_KEY);
        String parentTemplateType = TemplateTypeEnums.cot.getCode();

        processExecuteLog.setAgentCode(agentCode);
        processExecuteLog.setAgentName("llm_suggest");
        processExecuteLog.setSessionId(sessionId);
        processExecuteLog.setProcessDefineId(processDefineId);
        processExecuteLog.setProcessDefineVersion(processDefineVersion);
        processExecuteLog.setProcessDefineName(processDefineName);
        processExecuteLog.setProcessInstanceId(processInstanceId);
        processExecuteLog.setStream(stream);
        processExecuteLog.setRequestId(requestId);
        processExecuteLog.setParentTemplateType(parentTemplateType);
        processExecuteLog.setTemplateType("llm_suggest");
        processExecuteLog.setActivityType(TemplateTypeEnums.llm_suggest.getActivityType());
        processExecuteLog.setActivityName(TemplateTypeEnums.llm_suggest.getName());
        processExecuteLog.setTenantCode(tenantCode);

        processExecuteLog.setStartTime(System.currentTimeMillis());

        return processExecuteLog;
    }

    private void recordProcessExecuteLog(ProcessExecuteLog processExecuteLog) {
        processExecuteLog.setEndTime(System.currentTimeMillis());
        long executeTime = (processExecuteLog.getEndTime() - processExecuteLog.getStartTime()) / 1000;
        processExecuteLog.setExecuteTime(executeTime);
        ProcessExecuteLog.stop(processExecuteLog);
    }

    public LlmTemplateConfig convertCotLlmTemplateConfig(String templateConfigString, boolean ignore, FrameworkSystemContext systemContext, FrameworkCotCallingDelegation delegation) {
        AgentRelation agentRelation = systemContext.getAgentRelation();
        if (!ignore && agentRelation != null && agentRelation.getLlmTemplateConfig() != null) {
            // 判断如果有用户配置，直接返回用户配置信息
            LlmTemplateConfig userLlmTemplateConfig = agentRelation.getLlmTemplateConfig();
            LlmTemplateConfig currentConfig = new LlmTemplateConfig();
            currentConfig.setModelId(userLlmTemplateConfig.getModelId());
            if (!StringUtils.isEmpty(delegation.getLlmTemplateConfig())) {
                List<LlmTemplateConfig> llmTemplateConfigList = JSON.parseArray(delegation.getLlmTemplateConfig(), LlmTemplateConfig.class);
                if (!CollectionUtils.isEmpty(llmTemplateConfigList)) {
                    LlmTemplateConfig finalCurrentConfig = currentConfig;
                    llmTemplateConfigList = llmTemplateConfigList.stream().filter(e -> e.getModelId().equals(finalCurrentConfig.getModelId())).collect(Collectors.toList());
                    if (llmTemplateConfigList.size() > 0) {
                        currentConfig = llmTemplateConfigList.get(0);
                    }
                }
            }

            // TODO 逻辑恶心，先写死，后续需要改写
            if (currentConfig != null
//                    && ("33".equals(currentConfig.getModelId())
//                    || "27".equals(currentConfig.getModelId()))
//                    || "22".equals(currentConfig.getModelId())
//                    || "17".equals(currentConfig.getModelId())
            ) {
                log.info("fetch user llmTemplateConfig:" + JSON.toJSONString(currentConfig));
                return currentConfig;
            }
        }

        LlmTemplateConfig llmTemplateConfig = null;
        if (!StringUtils.isEmpty(templateConfigString)) {
            llmTemplateConfig = JSON.parseObject(templateConfigString, LlmTemplateConfig.class);
//            if (llmTemplateConfig.getTemperature() != null && llmTemplateConfig.getTemperature().equals(0.0d)) {
//                llmTemplateConfig.setTemperature(0.001d);
//            }
        }
        if (llmTemplateConfig == null) {
            if (ignore) {
                return null;
            }
            throw new AgentMagicException(AgentMagicErrorCode.SYSTEM_ERROR, "CotLlmTemplateConfig is null", null);
        }
        return llmTemplateConfig;
    }

    public Map<String, Object> invokeLangRunnableChain(ChatPromptTemplate prompt,
                                                       BaseLanguageModel model,
                                                       LlmTemplateConfig llmTemplateConfig,
                                                       List<BaseMessage> historyMessages,
                                                       Long startTime,
                                                       FrameworkSystemContext systemContext,
                                                       FrameworkCotCallingDelegation delegation) {
        LanguageModelService languageModelService = delegation.getLanguageModelService();
        AtomicReference<Long> start = new AtomicReference<>(startTime);
        Consumer<Object> chunkConsumer = systemContext.getChunkConsumer();
        String sessionId = systemContext.getSessionId();
        String userId = systemContext.getUserId();
        String requestId = systemContext.getRequestId();
        String agentCode = systemContext.getAgentCode();
        String env = systemContext.getEnv();
        AgentRelation agentRelation = systemContext.getAgentRelation();

        // cot异常重试次数
        Integer cotRetryCount = delegation.getCotRetryCount();
        RunnableInterface modelBinding = null;
        if (!CollectionUtils.isEmpty(llmTemplateConfig.getStop())) {
            modelBinding = model.bind(new HashMap<String, Object>() {
                {
                    put("stop", llmTemplateConfig.getStop());
                }
            });
        } else {
            modelBinding = model;
        }
        if (cotRetryCount != null && cotRetryCount > 0) {
            modelBinding = modelBinding.withRetry(cotRetryCount, new HashMap<>());
        }

        // cot异常切换备份模型
        BaseLanguageModel fallbackModel;
        try {
            LlmTemplateConfig fallbackLlmTemplateConfig = convertCotLlmTemplateConfig(delegation.getCotFallbackLlmTemplateConfig(), true, systemContext, delegation);

            LanguageModelGetRequest request = new LanguageModelGetRequest();
            request.setLlmTemplateConfig(fallbackLlmTemplateConfig);
            request.setSystemContext(systemContext);
            request.setFlag("fallbackLlmFetch");
            AgentResult<LanguageModelGetResponse> agentResult = languageModelService.getLanguageModel(request);
            if(!agentResult.isSuccess()) {
                throw new AgentMagicException(AgentMagicErrorCode.COT_SYSTEM_ERROR, agentResult.getErrorMsg(), systemContext.getRequestId());
            }
            fallbackModel = agentResult.getData().getLanguageModel();
            if (fallbackModel != null) {
                modelBinding = modelBinding.withFallbacks(fallbackModel);
            }
        } catch (Throwable e) {
            log.error("fetch fallbackModel error", e);
        }

        Runnable agent = Runnable.sequence(
                prompt,
                modelBinding);
        if (chunkConsumer != null) {
            Consumer<Object> finalChunkConsumer = chunkConsumer;
            String sectionId = IdGeneratorUtils.nextId();
            AtomicReference<Integer> firstTokenCost = new AtomicReference<>();
            AtomicReference<Double> firstTokenTime = new AtomicReference<>();
            Consumer<Object> agentConsumer = e -> {
                LlmExecuteLog llmExecuteLog = new LlmExecuteLog();
                llmExecuteLog.setEnv(env);
                llmExecuteLog.setStartTime(start.get());
                if (e instanceof AIMessage) {
                    AIMessage aiMessage = (AIMessage) e;
                    String content = aiMessage.getContent();

                    String firstCost = null;
                    try {
                        firstCost = ThreadLocalUtils.getFirstCost();
                        if (firstCost == null) {
                            firstCost = AgentResponseUtils.getTimeCost(startTime);
                            ThreadLocalUtils.set(firstCost);
                            firstTokenCost.set(tokenizer.getTokenCount(String.valueOf(e)));
                        }
                        Double firstCostDouble = Double.parseDouble(ThreadLocalUtils.getFirstCost()) * 1000;
                        llmExecuteLog.setFirstCost(firstCostDouble.toString());
                        firstTokenTime.set(firstCostDouble);
                    } catch (Exception ex) {
                        log.error("AIMessage ThreadLocalUtils.getFirstCost error:" + firstCost, ex);
                    }

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
                    message.getExtraInfo().setTimeCost(AgentResponseUtils.getTimeCost(start.get()));
                    message.getExtraInfo().setTimeCost(AgentResponseUtils.getTimeCost(start.get()));
                    double firstCostDouble = Double.parseDouble(ThreadLocalUtils.getFirstCost()) * 1000;
                    message.getExtraInfo().setFirstCost(Double.toString(firstCostDouble));

                    AgentAPIResult<AgentAPIInvokeResponse> apiResult = AgentAPIResult.success(agentAPIResponse, requestId);

                    delegation.onStreamNext(systemContext, apiResult);

                } else if (e instanceof RunnableTraceData) {
                    RunnableTraceData traceData = (RunnableTraceData) e;
                    log.info("traceData:" + JSON.toJSONString(traceData));
                    if ("onLlmEnd".equals(traceData.getStep())
                            || "onLlmError".equals(traceData.getStep())) {
                        String firstCost = null;
                        try {
                            firstCost = ThreadLocalUtils.getFirstCost();
                            if (firstCost != null) {
                                ThreadLocalUtils.set(null);
                            }
                            Double firstCostDouble = Double.parseDouble(ThreadLocalUtils.getFirstCost()) * 1000;
                            llmExecuteLog.setFirstCost(firstCostDouble.toString());
                            firstTokenTime.set(firstCostDouble);
                        } catch (Exception ex) {
                            log.error("AIMessage ThreadLocalUtils.getFirstCost error:" + firstCost, ex);
                        }

                        llmExecuteLog.setStartTime(traceData.getStartTime());
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
                        llmExecuteLog.doLog();

                        ThreadLocalUtils.set(null);
                        start.set(System.currentTimeMillis());
                    } else if ("onToolEnd".equals(traceData.getStep())
                            || "onToolError".equals(traceData.getStep())) {
                        ToolExecuteLog toolExecuteLog = new ToolExecuteLog();
                        toolExecuteLog.setStartTime(traceData.getStartTime());
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

                        ThreadLocalUtils.set(null);
                        start.set(System.currentTimeMillis());
                    }
                }
            };
            RunnableConfig config = new RunnableConfig();
            Object runnableOutput = agent.streamLog(new RunnableHashMap(), config, agentConsumer);
            log.info("runnableOutput not function stream:" + JSON.toJSONString(runnableOutput));

            // 日志打点
            Map<String, Object> nodeRequest = new HashMap<>();
            nodeRequest.put("messages", prompt.getMessages());

            Map<String, Object> enirchMap = new HashMap<>();
            enirchMap.put("nodeRequest", nodeRequest);
            if (Objects.nonNull(firstTokenTime.get())) {
                enirchMap.put("firstTokenTime", firstTokenTime.get());
            }
            if(Objects.nonNull(firstTokenCost.get())) {
                enirchMap.put("firstTokenCost", firstTokenCost.get());
            }
            if(Objects.nonNull(runnableOutput)) {
                enirchMap.put("costToken", tokenizer.getTokenCount(JSON.toJSONString(runnableOutput)));
            }
            if(Objects.nonNull(systemContext.getExecutionContext())) {
                ProcessExecuteLog.update(systemContext.getExecutionContext(), enirchMap);
            }

            //是否建议
            if (agentRelation.isLlmSuggestEnabled()) {
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
                callLlmSuggest(model, historyMessages, answer, systemContext, delegation);
            }

            if (runnableOutput instanceof AIMessage) {
                Object finalRunnableOutput = runnableOutput;
                return new HashMap<String, Object>() {{
                    put("output", ((AIMessage) finalRunnableOutput).getContent());
                }};
            } else {
                return (Map<String, Object>) runnableOutput;
            }
        } else {
            Object runnableOutput = agent.invoke(new RunnableHashMap());
            log.info("runnableOutput not function invoke:" + JSON.toJSONString(runnableOutput));
            if (runnableOutput instanceof AIMessage) {
                Object finalRunnableOutput = runnableOutput;
                return new HashMap<String, Object>() {{
                    put("output", ((AIMessage) finalRunnableOutput).getContent());
                }};
            } else {
                return (Map<String, Object>) runnableOutput;
            }
        }
    }

    public Map<String, Object> invokeLangRunnableAgent(ChatPromptTemplate prompt,
                                                       BaseLanguageModel model,
                                                       List<FunctionDefinition> functions,
                                                       List<BaseTool> tools,
                                                       LlmTemplateConfig llmTemplateConfig,
                                                       List<BaseMessage> historyMessages,
                                                       Long startTime,
                                                       FrameworkSystemContext systemContext,
                                                       FrameworkCotCallingDelegation delegation) {
        LanguageModelService languageModelService = delegation.getLanguageModelService();
        AtomicReference<Long> start = new AtomicReference<>(startTime);
        Consumer<Object> chunkConsumer = systemContext.getChunkConsumer();
        if (systemContext.getForceStream() != null && !systemContext.getForceStream() && chunkConsumer != null) {
            chunkConsumer = null;
        }
        String sessionId = systemContext.getSessionId();
        String userId = systemContext.getUserId();
        String requestId = systemContext.getRequestId();
        String agentCode = systemContext.getAgentCode();
        String env = systemContext.getEnv();
        AgentRelation agentRelation = systemContext.getAgentRelation();

        Object runnableOutput;
        RunnableInterface modelBinding = model.bind(new HashMap<String, Object>() {{
            put("functions", functions);
            if (!CollectionUtils.isEmpty(llmTemplateConfig.getStop())) {
                put("stop", llmTemplateConfig.getStop());
            }
        }});

        // cot异常重试次数
        Integer cotRetryCount = delegation.getCotRetryCount();
        if (cotRetryCount != null && cotRetryCount > 0) {
            modelBinding = modelBinding.withRetry(cotRetryCount, new HashMap<>());
        }

        // cot异常切换备份模型
        BaseLanguageModel fallbackModel;
        try {
            LlmTemplateConfig fallbackLlmTemplateConfig = convertCotLlmTemplateConfig(delegation.getCotFallbackLlmTemplateConfig(), true, systemContext, delegation);
            LanguageModelGetRequest request = new LanguageModelGetRequest();
            request.setLlmTemplateConfig(fallbackLlmTemplateConfig);
            request.setSystemContext(systemContext);
            request.setFlag("fallbackLlmFetch");
            AgentResult<LanguageModelGetResponse> agentResult = languageModelService.getLanguageModel(request);
            if(!agentResult.isSuccess()) {
                throw new AgentMagicException(AgentMagicErrorCode.COT_SYSTEM_ERROR, agentResult.getErrorMsg(), systemContext.getRequestId());
            }
            fallbackModel = agentResult.getData().getLanguageModel();
            if (fallbackModel != null) {
                RunnableInterface fallbackModelBinding = fallbackModel.bind(new HashMap<String, Object>() {{
                    put("functions", functions);
                }});
                modelBinding = modelBinding.withFallbacks(fallbackModelBinding);
            }
        } catch (Throwable e) {
            log.error("fetch fallbackModel error", e);
        }

        RunnableInterface assign = Runnable.assign(new RunnableHashMap() {{
            put("agent_scratchpad", new RunnableAgentLambda(intermediateSteps -> delegation.convertMessageIntermediateSteps(intermediateSteps)));
        }});

        RunnableAgent agent = new RunnableAgent(Runnable.sequence(
                assign,
                prompt,
                modelBinding
        ), new BotChatOutputParser());

        RunnableAgentExecutor agentExecutor = new RunnableAgentExecutor(agent, tools, t -> {
            RunnableHashMap runnableHashMap = new RunnableHashMap();
            List<FunctionDefinition> filterFunctions = functions.stream().filter(func -> t.stream().anyMatch(e -> e.getName().equals(func.getName()))).collect(Collectors.toList());
            runnableHashMap.put("functions", filterFunctions);
            return runnableHashMap;
        });

        // TODO 待优化
//            agentExecutor.setMaxIterations(3);

        if (chunkConsumer != null) {
            Consumer<Object> finalChunkConsumer = chunkConsumer;
            String sectionId = IdGeneratorUtils.nextId();
            LlmTemplateConfig finalLlmTemplateConfig = llmTemplateConfig;

            AtomicReference<Integer> firstTokenCost = new AtomicReference<>();
            AtomicReference<Double> firstTokenTime = new AtomicReference<>();

            Consumer<Object> agentConsumer = e -> {
                LlmExecuteLog llmExecuteLog = new LlmExecuteLog();
                llmExecuteLog.setEnv(env);
                llmExecuteLog.setStartTime(start.get());
                if (e instanceof AIMessage) {
                    AIMessage aiMessage = (AIMessage) e;
                    log.info("aiMessage chunk:" + JSON.toJSONString(aiMessage));

                    String firstCost = null;
                    try {
                        firstCost = ThreadLocalUtils.getFirstCost();
                        if (firstCost == null) {
                            firstCost = AgentResponseUtils.getTimeCost(start.get());
                            ThreadLocalUtils.set(firstCost);
                            firstTokenCost.set(tokenizer.getTokenCount(String.valueOf(e)));
                        }
                        Double firstCostDouble = Double.parseDouble(ThreadLocalUtils.getFirstCost()) * 1000;
                        llmExecuteLog.setFirstCost(firstCostDouble.toString());
                        firstTokenTime.set(firstCostDouble);
                    } catch (Exception ex) {
                        log.error("AIMessage ThreadLocalUtils.getFirstCost error:" + firstCost, ex);
                    }

                    String content = null;
                    boolean matchMessageReturn = false;
                    if (("AppEngineChatModel".equals(finalLlmTemplateConfig.getModelTemplate())
                            && ("gpt-4o".equals(finalLlmTemplateConfig.getModelName())
                            || "gpt-35-turbo-16k".equals(finalLlmTemplateConfig.getModelName())
                            || "gpt-35-turbo".equals(finalLlmTemplateConfig.getModelName())
                            || "Marco-Pro-32K".equals(finalLlmTemplateConfig.getModelName())
                            || "Marco-Max-32K".equals(finalLlmTemplateConfig.getModelName())
                            || "MarcoVL-8B".equals(finalLlmTemplateConfig.getModelName())
                            || "Qwen2-72B".equals(finalLlmTemplateConfig.getModelName())
                    )) || ("ApiGatewayChatModel".equals(finalLlmTemplateConfig.getModelTemplate()))
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
                        message.getExtraInfo().setTimeCost(AgentResponseUtils.getTimeCost(start.get()));
                        double firstCostDouble = Double.parseDouble(ThreadLocalUtils.getFirstCost()) * 1000;
                        message.getExtraInfo().setFirstCost(Double.toString(firstCostDouble));

                        AgentAPIResult<AgentAPIInvokeResponse> apiResult = AgentAPIResult.success(agentAPIResponse, requestId);

                        delegation.onStreamNext(systemContext, apiResult);
                    }
                } else if (e instanceof RunnableTraceData) {
                    RunnableTraceData traceData = (RunnableTraceData) e;
                    log.info("traceData:" + JSON.toJSONString(traceData));
                    if ("onLlmEnd".equals(traceData.getStep())
                            || "onLlmError".equals(traceData.getStep())) {
                        String firstCost = null;
                        try {
                            firstCost = ThreadLocalUtils.getFirstCost();
                            if (firstCost == null) {
                                firstCost = AgentResponseUtils.getTimeCost(start.get());
                                ThreadLocalUtils.set(firstCost);
                            }
                            Double firstCostDouble = Double.parseDouble(ThreadLocalUtils.getFirstCost()) * 1000;
                            llmExecuteLog.setFirstCost(firstCostDouble.toString());
                            firstTokenTime.set(firstCostDouble);
                        } catch (Exception ex) {
                            log.error("AIMessage ThreadLocalUtils.getFirstCost error:" + firstCost, ex);
                        }

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

                        llmExecuteLog.doLog();

                        ThreadLocalUtils.set(null);
                        start.set(System.currentTimeMillis());
                    } else if ("onToolEnd".equals(traceData.getStep())
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

                        ThreadLocalUtils.set(null);
                        start.set(System.currentTimeMillis());
                    }
                }
            };

            RunnableConfig config = new RunnableConfig();
            config.setStreamLog(true);
            runnableOutput = agentExecutor.stream(new RunnableHashMap(), config, agentConsumer);
            log.info("runnableOutput stream:" + JSON.toJSONString(runnableOutput));


            // 日志打点
            Map<String, Object> nodeRequest = new HashMap<>();
            nodeRequest.put("messages", prompt.getMessages());

            Map<String, Object> enirchMap = new HashMap<>();
            enirchMap.put("nodeRequest", nodeRequest);
            if (Objects.nonNull(firstTokenTime.get())) {
                enirchMap.put("firstTokenTime", firstTokenTime.get());
            }
            if(Objects.nonNull(firstTokenCost.get())) {
                enirchMap.put("firstTokenCost", firstTokenCost.get());
            }
            if(Objects.nonNull(runnableOutput)) {
                enirchMap.put("costToken", tokenizer.getTokenCount(JSON.toJSONString(runnableOutput)));
            }
            if(Objects.nonNull(systemContext.getExecutionContext())) {
                ProcessExecuteLog.update(systemContext.getExecutionContext(), enirchMap);
            }

            //是否建议
            if (agentRelation.isLlmSuggestEnabled()) {
                String answer = null;
                if (runnableOutput instanceof RunnableHashMap) {
                    if (((RunnableHashMap) runnableOutput).get("output") != null) {
                        answer = ((RunnableHashMap) runnableOutput).get("output").toString();
                    } else {
                        answer = JSON.toJSONString(runnableOutput);
                    }
                }
                callLlmSuggest(model, historyMessages, answer, systemContext, delegation);
            }

            if (runnableOutput instanceof AIMessage) {
                Object finalRunnableOutput = runnableOutput;
                return new HashMap<String, Object>() {{
                    put("output", ((AIMessage) finalRunnableOutput).getContent());
                }};
            } else {
                return (Map<String, Object>) runnableOutput;
            }
        } else {
            runnableOutput = agentExecutor.invoke(new RunnableHashMap());
            log.info("runnableOutput invoke:" + JSON.toJSONString(runnableOutput));
            if (runnableOutput instanceof AIMessage) {
                Object finalRunnableOutput = runnableOutput;
                return new HashMap<String, Object>() {{
                    put("output", ((AIMessage) finalRunnableOutput).getContent());
                }};
            } else {
                return (Map<String, Object>) runnableOutput;
            }
        }
    }

    public Map<String, Object> callLlm(FrameworkSystemContext systemContext, FrameworkCotCallingDelegation delegation, String knowledgeContext) {
        return callLlm(systemContext, delegation, knowledgeContext, null);
    }

    public Map<String, Object> callLlm(FrameworkSystemContext systemContext, FrameworkCotCallingDelegation delegation, String knowledgeContext, List<BaseMessage> intermediateMessages) {
        Long startTime = System.currentTimeMillis();
        log.info("FunctionCallCallExector startTime:" + startTime);

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

        List<BaseTool> tools = new ArrayList<>();
        List<FunctionDefinition> functions = new ArrayList<>();
        ToolBuildingUtils.buildTool(systemContext, tools, functions, startTime, delegation);

        List<BaseMessage> messages = new ArrayList<>();
        List<BaseMessage> historyMessages = MessageBuildingUtils.buildMessageReturnHistory(systemContext, messages, delegation, knowledgeContext, intermediateMessages);
        ChatPromptTemplate prompt = ChatPromptTemplate.fromChatMessages(messages);

        log.info("functions:" + JSON.toJSONString(functions));
        if (functions.size() == 0) {
            return invokeLangRunnableChain(prompt, model, llmTemplateConfig, historyMessages, startTime, systemContext, delegation);
        } else {
            return invokeLangRunnableAgent(prompt, model, functions, tools, llmTemplateConfig, historyMessages, startTime, systemContext, delegation);
        }
    }

    protected FunctionMessage executeTool(FrameworkSystemContext systemContext, FrameworkCotCallingDelegation delegation,
                                          String name, String arguments, List<ComponentCallingInput> componentList) {
        if(componentList == null || componentList.isEmpty()) {
            log.warn("componentList is null, requestId is " + systemContext.getRequestId());
            return null;
        }
        log.info("start call tool and llm. name={}, arguments={}, componentList={}", name, arguments,
                String.join(",",componentList.stream().map(ComponentCallingInput::getComponentId).collect(Collectors.toList())));
        List<ComponentCallingInput> filterComponentList = componentList.stream().filter(e -> e.getComponentId().equals(name))
                .collect(Collectors.toList());
        if(filterComponentList.isEmpty()) {
            log.warn("componentList is null after filter name={}, requestId={}",name, systemContext.getRequestId());
            return null;
        }
        ComponentCallingInput component = filterComponentList.get(0);
        return executeTool(systemContext, delegation.getToolCallingService(), component, arguments);
    }

    protected FunctionMessage executeTool(FrameworkSystemContext systemContext, ToolCallingService toolCallingService,
                                          ComponentCallingInput component, String arguments) {
        ComponentTool tool = ComponentTool.build(systemContext, toolCallingService, component, new HashMap<>());
        ToolExecuteResult toolExecuteResult = tool.execute(arguments);
        FunctionMessage functionMessage = new FunctionMessage();
        functionMessage.setName(component.getComponentId());
        functionMessage.setContent(toolExecuteResult.getOutput());
        return functionMessage;
    }

//    public static void main(String[] args) {
//        String input = "以下是根据用户兴趣点推荐的3个具有区分度的不同问题:\n1. Can you create a loading animation for the app with a dog theme?\n2. What color scheme would complement the logo for the app interface?\n3. Do you have any suggestions for a catchy slogan to go with the cute dog app";
//
//        List<String> questions = AgentDelegationBase.extractQuestions(input);
//
//        for (String question : questions) {
//            System.out.println(question);
//        }
//
//        input = "1. 问题不能是已经问过的问题，不能是已经回答过的问题，问题必须和用户最后一轮的问题紧密相关，可以适当延伸；\n" +
//                "2. 每句话只包含一个问题或者指令；\n" +
//                "3. 如果对话涉及政治敏感、违法违规、暴力伤害、违反公序良俗类内容，你应该拒绝推荐问题。";
//
//        input = CotCallingConstant.LLM_SUGGEST_PROMPT_PREFIX + input + CotCallingConstant.LLM_SUGGEST_PROMPT_SUFFIX;
//        System.out.println(input);
//    }
}
