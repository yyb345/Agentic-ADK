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
package com.alibaba.agentmagic.framework.delegation.provider;

import com.alibaba.agentmagic.framework.delegation.constants.LlmCallingConstant;
import com.alibaba.agentmagic.framework.delegation.constants.SystemConstant;
import com.alibaba.agentmagic.framework.domain.LlmExecuteLog;
import com.alibaba.agentmagic.framework.domain.ProcessExecuteLog;
import com.alibaba.agentmagic.framework.utils.AgentResponseUtils;
import com.alibaba.agentmagic.framework.utils.FrameworkUtils;
import com.alibaba.agentmagic.framework.utils.ThreadLocalUtils;
import com.alibaba.agentmagic.framework.utils.UrlUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.agentframework.model.AgentResult;
import com.alibaba.langengine.agentframework.model.constant.ModelConstants;
import com.alibaba.langengine.agentframework.model.domain.*;
import com.alibaba.langengine.agentframework.model.enums.AgentMagicErrorCode;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.langengine.agentframework.model.service.LanguageModelService;
import com.alibaba.langengine.agentframework.model.service.request.LanguageModelCallRequest;
import com.alibaba.langengine.agentframework.model.service.response.LanguageModelCallResponse;
import com.alibaba.langengine.core.tokenizers.QwenTokenizer;

import com.alibaba.fastjson.TypeReference;

import com.alibaba.langengine.core.util.LLMUtils;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.alibaba.agentmagic.framework.delegation.constants.SystemConstant.*;

@Slf4j
public class LlmCallingDelegationHelper implements LlmCallingConstant {

    private static final QwenTokenizer tokenizer = new QwenTokenizer();

    private static void addArrayChatAttachmentsInternal(Map<String, Object> data, String mediaType, Map<String, Object> propsTemplate, List<ChatAttachment> chatAttachments){
        Object valueField =  data.get("value");
        List<String> values = null;
        // valueField from schema is already a json array
        log.info("the value field from json array: {}", valueField);
        values = JSON.parseArray(JSON.toJSONString(valueField), String.class);
        if(CollectionUtils.isEmpty(values)){
            log.error("empty chat attachment url parameter, values before extract urls : {}", valueField);
            throw new AgentMagicException(AgentMagicErrorCode.INPUT_PARAMETER_ERROR, "empty url parameter: " + valueField, null);
        }
        for (String value : values) {
            addChatAttachment(chatAttachments, mediaType, value.trim(), propsTemplate);
        }
    }

    private static void addSingleChatAttachmentInternal(Map<String, Object> data, String mediaType, Map<String, Object> propsTemplate, List<ChatAttachment> chatAttachments){
        String valueField = (String) data.get("value");
        addChatAttachment(chatAttachments, mediaType, valueField, propsTemplate);
    }

    public static void processMultiModalData(List<Map<String, Object>> dataList, List<ChatAttachment> chatAttachments){
        if (CollectionUtils.isNotEmpty(dataList)) {
            for (Map<String, Object> data : dataList) {

                String valueType = data.get("valueType").toString();
                String mediaType = data.get("mediaType").toString();
        
                Map<String, Object> propsTemplate = new HashMap<>();
                // for model limitation, we don't put the following fields into propsTemplate
                // propsTemplate.put("name", name);
                // propsTemplate.put("valueType", valueType);
                // propsTemplate.put("mediaType", mediaType);
        
                if (valueType.startsWith("array")) {
                    addArrayChatAttachmentsInternal(data, mediaType, propsTemplate, chatAttachments);
                } else {
                    addSingleChatAttachmentInternal(data, mediaType, propsTemplate, chatAttachments);
                }
            }
        }
    }

    private static void addChatAttachment(List<ChatAttachment> chatAttachments, String mediaType, String value, Map<String, Object> propsTemplate) {
        ChatAttachment chatAttachment = new ChatAttachment();
        chatAttachment.setType(mediaType);
        Map<String, Object> props = new HashMap<>(propsTemplate);
        if(!StringUtils.isEmpty(value)) {
            if (!UrlUtils.isValidURL(value)) {
                log.error("invalid url parameter: {}", value);
                throw new AgentMagicException(AgentMagicErrorCode.INPUT_PARAMETER_ERROR, "invalid url parameter: " + value, null);
            }
            props.put("url", value);
            chatAttachment.setProps(props);
            chatAttachments.add(chatAttachment);
        }
    }

    public static void processImageUrlLegacy(String vlImageUrl, List<ChatAttachment> chatAttachments){
        if(!StringUtils.isEmpty(vlImageUrl)) {

            String[] imageUrlArray = vlImageUrl.split(",");

            if(imageUrlArray.length > 0) {
                for (String imageUrl : imageUrlArray) {
                    addChatAttachment(chatAttachments, "image", imageUrl, new HashMap<>());
                }
            }

        }
    }

    public static Map<String, Object> executeInternal(ExecutionContext executionContext,
                                                      JSONObject properties,
                                                      JSONObject request,
                                                      LanguageModelService languageModelService,
                                                      LanguageModelCallRequest languageModelCallRequest,
                                                      String llmTemplateConfig) {
        String activityId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();
        String processInstanceId = executionContext.getExecutionInstance().getProcessInstanceId();

        // 系统变量
        String query = DelegationHelper.getSystemValue(request, QUERY_KEY);
        Boolean async = DelegationHelper.getSystemBooleanOrDefault(request, SystemConstant.ASYNC_KEY, false);
//        Boolean batch = DelegationHelper.getSystemBooleanOrDefault(request, BATCH_KEY, false);
//        Boolean offline = DelegationHelper.getSystemBooleanOrDefault(request, OFFLINE_KEY, false);
        String agentCode = DelegationHelper.getSystemValue(request, SystemConstant.AGENT_CODE_KEY);
        Boolean debug = Boolean.parseBoolean(DelegationHelper.getSystemValue(request, SystemConstant.DEBUG_KEY));
        String sessionId = DelegationHelper.getSystemValue(request, SystemConstant.SESSION_ID_KEY);
        String userId = DelegationHelper.getSystemValue(request, SystemConstant.USER_ID_KEY);
        String requestId = DelegationHelper.getSystemValue(request, SystemConstant.REQUEST_ID_KEY);
        String env = DelegationHelper.getSystemValue(request, SystemConstant.ENV_KEY);
        String projectName = DelegationHelper.getSystemValue(request, PROJECT_NAME_KEY);
        Boolean botChat = false;
        Object botChatObj = DelegationHelper.getSystemValue(request, SystemConstant.BOT_CHAT_KEY);
        if(botChatObj != null) {
            botChat = (Boolean) botChatObj;
        }
        Map<String, Object> invokeContext = null;
        Object invokeContextObj = DelegationHelper.getSystem(request, INVOKE_CONTEXT_KEY);
        if(invokeContextObj != null) {
            invokeContext = (Map<String, Object>) invokeContextObj;
        }

        List<ChatAttachment> chatAttachments = new ArrayList<>();

        String vlImageUrl = request.getString("sys_vl_image_url");
        
        String vlStr = properties.getString(VLINPUTPARAMETERS_PROPERTY_KEY);
        List<Map<String, Object>> dataList = JSON.parseObject(vlStr, new TypeReference<List<Map<String, Object>>>() {});

        // multimodal data parametersprocessing
        try{
            processMultiModalData(dataList, chatAttachments);
        }catch (AgentMagicException e){
            log.error("process multimodal data error: ", e);
            throw new AgentMagicException(AgentMagicErrorCode.INPUT_PARAMETER_ERROR, e.getErrorDetail(), requestId);
        }
        // original sys_vl_image_url parameters processing
        processImageUrlLegacy(vlImageUrl, chatAttachments);

        List<ChatMessage> chatHistory = null;
        Object chatHistoryObj = DelegationHelper.getSystem(request, SystemConstant.HISTORY_KEY);
        if(chatHistoryObj != null) {
            chatHistory = (List<ChatMessage>)chatHistoryObj;
        }

        Consumer<Object> chunkConsumer = null;
        Object chunkConsumerObj = DelegationHelper.getSystem(request, SystemConstant.CHUNK_CONSUMER_KEY);
        if(chunkConsumerObj != null) {
            chunkConsumer = (Consumer<Object>)chunkConsumerObj;
        }

        // 属性变量
        String prompt = DelegationHelper.replaceNewLine(properties.getString(PROMPT_KEY));
        String stream = properties.getString(STREAM_KEY);
        if(StringUtils.isEmpty(prompt)) {
            throw new AgentMagicException(AgentMagicErrorCode.LLM_SYSTEM_ERROR, "prompt is empty", requestId);
        }
        prompt = DelegationHelper.replacePromptToVelocity(prompt, request);

        String modelId;
        if(StringUtils.isNotBlank(DelegationHelper.getSystemValue(request, MODEL_ID_KEY))) {
            modelId = DelegationHelper.getSystemValue(request, MODEL_ID_KEY);
        } else {
            modelId = properties.getString(MODEL_ID_KEY);
        }

        Double temperature;
        if(StringUtils.isNotBlank(DelegationHelper.getSystemValue(request, TEMPERATURE_KEY))) {
            temperature = Double.valueOf(Objects.requireNonNull(DelegationHelper.getSystemValue(request, TEMPERATURE_KEY)));
        } else {
            temperature = properties.getDouble(TEMPERATURE_KEY);
        }
        String llmType = properties.getString(LLM_TYPE_KEY);
        String llmTemplateCode = properties.getString(LLM_TEMPLATE_CODE_KEY);
        String responseFilter = properties.getString(RESPONSE_FILTER_KEY);
        String hasHistory = properties.getString(HAS_HISTORY_KEY);
        if(hasHistory == null) {
            // 默认开启大模型节点多轮记忆
            hasHistory = HAS_HISTORY_TRUE;
        }
        Object streamReference = properties.get(STREAM_REFERENCE);
        log.info("streamReference:" + streamReference);

        LlmTemplateConfig currentConfig = new LlmTemplateConfig();
        currentConfig.setModelId(modelId);
        if(!StringUtils.isEmpty(llmTemplateConfig)) {
            List<LlmTemplateConfig> llmTemplateConfigList = JSON.parseArray(llmTemplateConfig, LlmTemplateConfig.class);
            if(!CollectionUtils.isEmpty(llmTemplateConfigList)) {
                LlmTemplateConfig finalCurrenConfig = currentConfig;
                llmTemplateConfigList = llmTemplateConfigList.stream().filter(e -> e.getModelId().equals(finalCurrenConfig.getModelId())).collect(Collectors.toList());
                if (llmTemplateConfigList.size() > 0) {
                    currentConfig = llmTemplateConfigList.get(0);
                }
            }
        }
        String finalPrompt = LLMUtils.generateFinalPrompt(prompt, currentConfig.getAutoLlmFlag(), currentConfig.getModelType());

        Boolean traceOutput = DelegationHelper.getSystemBoolean(executionContext.getRequest(), TRACE_OUTPUT_KEY);
        if(traceOutput != null && traceOutput) {
            TraceOutputDO traceOutputDO = AgentTraceHelper.getTraceOutputByNode(executionContext, activityId);
            traceOutputDO.getRequest().put(PROMPT_KEY, finalPrompt);
            AgentTraceHelper.setTraceOutputResponseByNode(executionContext, activityId, traceOutputDO);
        }

        Boolean apiKeyCall = DelegationHelper.getSystemBooleanOrDefault(request, SystemConstant.API_KEY_CALL_KEY, false);
        String apiKey = null;
        if (apiKeyCall) {
            apiKey = DelegationHelper.getSystemString(request, SystemConstant.APIKEY_KEY);
        }

        languageModelCallRequest.setAgentCode(agentCode);
        languageModelCallRequest.setQuery(query);
        languageModelCallRequest.setChatAttachments(chatAttachments);
        languageModelCallRequest.setPrompt(prompt);
        languageModelCallRequest.setModelId(modelId);
        languageModelCallRequest.setTemperature(temperature);
        languageModelCallRequest.setLlmType(llmType);
        languageModelCallRequest.setLlmTemplateCode(llmTemplateCode);
        languageModelCallRequest.setDebug(debug);
        languageModelCallRequest.setEnv(env);
        languageModelCallRequest.setProjectName(projectName);
        languageModelCallRequest.setApiKeyCall(apiKeyCall);
        languageModelCallRequest.setApiKey(apiKey);
        languageModelCallRequest.setSessionId(sessionId);
        languageModelCallRequest.setUserId(userId);
        languageModelCallRequest.setRequestId(requestId);
        languageModelCallRequest.setChatHistory(chatHistory);
        languageModelCallRequest.setHasHistory(HAS_HISTORY_TRUE.equals(hasHistory));

        // 判断是否走流式，如果为空或者等于1，才走流式处理
        if(stream == null || stream.equals("1")) {
            languageModelCallRequest.setChunkConsumer(chunkConsumer);
        }
        languageModelCallRequest.setRequest(request);
        languageModelCallRequest.setResponse(executionContext.getResponse());
        languageModelCallRequest.setStreamReference(streamReference);
        languageModelCallRequest.setTraceToken(traceOutput != null && traceOutput);
        if(!StringUtils.isEmpty(responseFilter)) {
            languageModelCallRequest.setResponseFilter(responseFilter);
        }
        log.info("languageModelCallRequest responseFilter:" + responseFilter);
        languageModelCallRequest.setNodeId(activityId);
        languageModelCallRequest.setBotChat(botChat);
        languageModelCallRequest.setAsync(async);
        languageModelCallRequest.setInvokeContext(invokeContext);
        languageModelCallRequest.setProcessInstanceId(processInstanceId);
//        languageModelCallRequest.setBatch(batch);
//        languageModelCallRequest.setOffline(offline);

        boolean enableExceptionConfig = properties.getBooleanValue(CODE_ENABLE_EXCEPTION_CONFIG);
        JSONObject exceptionConfig = properties.getJSONObject(CODE_EXCEPTION_CONFIG);
        if(Objects.nonNull(exceptionConfig)) {
            exceptionConfig = FrameworkUtils.replaceJson(exceptionConfig, new JSONObject(executionContext.getRequest()));
        }


        languageModelCallRequest.setEnableExceptionConfig(enableExceptionConfig);
        languageModelCallRequest.setExceptionConfig(exceptionConfig);

        long start = System.currentTimeMillis();
        log.info("LanguageModelService call request:activityId:" + activityId + ",requestId:" + requestId);

        LlmExecuteLog llmExecuteLog = new LlmExecuteLog();
        AtomicReference<Integer> firstTokenCost = new AtomicReference<>();
        if(languageModelCallRequest.getChunkConsumer() != null) {
            Consumer<Object> finalChunkConsumer = languageModelCallRequest.getChunkConsumer();
            Consumer<Object> agentConsumer = e -> {
                String firstCost = null;
                try {
                    firstCost = ThreadLocalUtils.getFirstCost();
                    if(firstCost == null) {
                        firstCost = AgentResponseUtils.getTimeCost(start);
                        ThreadLocalUtils.set(firstCost);
                        if(e instanceof AgentAPIResult) {
                            Object data = ((AgentAPIResult<?>) e).getData();
                            if(data instanceof AgentAPIInvokeResponse) {
                                String answer = ((AgentAPIInvokeResponse) data).getAnswer();
                                firstTokenCost.set(tokenizer.getTokenCount(answer));
                            }
                        }
                    }
                    Double firstCostDouble = Double.parseDouble(ThreadLocalUtils.getFirstCost()) * 1000;
                    llmExecuteLog.setFirstCost(firstCostDouble.toString());

                } catch (Exception ex) {
                    log.error("AIMessage ThreadLocalUtils.getFirstCost error:" + firstCost, ex);
                }
                finalChunkConsumer.accept(e);
            };
            languageModelCallRequest.setChunkConsumer(agentConsumer);
        }
        log.info("LanguageModelService call agentCode: {}, temperature: {}, modelId: {}, async: {}, invokeContext: {}", agentCode, temperature, modelId, async, invokeContext);
        AgentResult<LanguageModelCallResponse> agentResult = languageModelService.call(languageModelCallRequest);
        long cost = System.currentTimeMillis() - start;
        log.info("LanguageModelService call response:" + JSON.toJSONString(agentResult) + ",cost:" + cost);

        llmExecuteLog.setStartTime(start);
        llmExecuteLog.setAgentCode(agentCode);
        llmExecuteLog.setExecuteTime(cost);
        llmExecuteLog.setStream(chunkConsumerObj != null ? "true": "false");
        llmExecuteLog.setModelType(currentConfig.getModelTemplate());
        llmExecuteLog.setModelName(currentConfig.getModelName());
        llmExecuteLog.setRequestId(requestId);
        llmExecuteLog.setUserId(userId);
        llmExecuteLog.setEnv(env);

        if(!agentResult.isSuccess()) {
            llmExecuteLog.setSuccess(false);
            llmExecuteLog.setCode(agentResult.getErrorCode());
            llmExecuteLog.setMessage(agentResult.getErrorMsg());
            // llm日志打点
            llmExecuteLog.doLog();
            log.error("LanguageModelService call error:{}", AgentResult.getAgentResultError(agentResult));
            throw new AgentMagicException(agentResult.getErrorCode(), agentResult.getErrorMsg(), agentResult.getErrorDetail(), requestId);
        }
        if(agentResult.getData() == null) {
            String msg = "LanguageModelService call error:data is empty";
            log.error(msg);

            llmExecuteLog.setSuccess(false);
            llmExecuteLog.setCode("llm-system-error");
            llmExecuteLog.setMessage(msg);
            // llm日志打点
            llmExecuteLog.doLog();
            throw new AgentMagicException(AgentMagicErrorCode.LLM_SYSTEM_ERROR, msg, requestId);
        }
        Map<String, Object> outputs = agentResult.getData().getOutputs();
        if(outputs != null) {
            if(traceOutput != null && traceOutput) {
                TraceOutputDO traceOutputDO = AgentTraceHelper.getTraceOutputByNode(executionContext, activityId);
                //计算Token数
                if(outputs.get(ModelConstants.LLM_INPUT_TOKENS) != null) {
                    traceOutputDO.setInputTokens(Long.parseLong(outputs.get(ModelConstants.LLM_INPUT_TOKENS).toString()));
                }
                if(outputs.get(ModelConstants.LLM_OUTPUT_TOKENS) != null) {
                    traceOutputDO.setOutputTokens(Long.parseLong(outputs.get(ModelConstants.LLM_OUTPUT_TOKENS).toString()));
                }
                AgentTraceHelper.setTraceOutputResponseByNode(executionContext, activityId, traceOutputDO);
            }
        }

        // llm日志打点
        llmExecuteLog.doLog();

        // 日志打点
        Map<String, Object> nodeRequest = new HashMap<>();
        nodeRequest.put(PROMPT_KEY, finalPrompt);
        nodeRequest.put(STREAM_KEY, stream);
        nodeRequest.put(MODEL_ID_KEY, modelId);
        nodeRequest.put(TEMPERATURE_KEY, temperature);
        nodeRequest.put(LLM_TYPE_KEY, llmType);
        nodeRequest.put(LLM_TEMPLATE_CODE_KEY, llmTemplateCode);
        nodeRequest.put(RESPONSE_FILTER_KEY, responseFilter);

        Map<String, Object> enirchMap = new HashMap<>();
        enirchMap.put("nodeRequest", nodeRequest);
        if(Objects.nonNull(llmExecuteLog.getFirstCost())) {
            enirchMap.put("firstTokenTime", llmExecuteLog.getFirstCost());
        }
        log.warn("output content: {}", JSON.toJSONString(outputs));
        if(MapUtils.isNotEmpty(outputs) && Objects.nonNull(outputs.get(ModelConstants.LLM_OUTPUT_TOKENS))) {
            enirchMap.put("costToken", outputs.get(ModelConstants.LLM_OUTPUT_TOKENS));
        }
        if(Objects.nonNull(firstTokenCost.get())) {
            enirchMap.put("firstTokenCost", firstTokenCost.get());
        }
        ProcessExecuteLog.update(executionContext, enirchMap);

        return outputs;
    }

    public static String replacePrompt(String text, String input) {
        Map<String, Object> inputs = new HashMap<String, Object>() {{
            put("input", input);
        }};
        return replacePrompt(text, inputs);
    }

    private static String replacePrompt(String text, Map<String, Object> inputs) {
        for (Map.Entry<String, Object> entry : inputs.entrySet()) {
            if(entry.getValue() == null) {
                continue;
            }
            text = text.replaceAll("\\{" + entry.getKey() + "\\}", Matcher.quoteReplacement(entry.getValue().toString()));
        }
        return text;
    }
}
