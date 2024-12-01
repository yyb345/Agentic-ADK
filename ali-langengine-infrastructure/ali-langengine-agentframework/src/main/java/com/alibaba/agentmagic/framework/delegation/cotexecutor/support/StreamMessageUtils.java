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
package com.alibaba.agentmagic.framework.delegation.cotexecutor.support;

import com.alibaba.agentmagic.framework.delegation.FrameworkCotCallingDelegation;
import com.alibaba.agentmagic.framework.domain.ProcessExecuteLog;
import com.alibaba.agentmagic.framework.domain.TemplateTypeEnums;
import com.alibaba.agentmagic.framework.utils.AgentResponseUtils;
import com.alibaba.agentmagic.framework.utils.IdGeneratorUtils;
import com.alibaba.agentmagic.framework.utils.ToolCallingLogContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.langengine.agentframework.model.AgentResult;
import com.alibaba.langengine.agentframework.model.agent.domain.AgentRelation;
import com.alibaba.langengine.agentframework.model.domain.*;
import com.alibaba.langengine.agentframework.model.service.request.RetrievalSearchRequest;
import com.alibaba.langengine.agentframework.model.service.request.ToolCallingInvokeRequest;
import com.alibaba.langengine.agentframework.model.service.response.FrameworkDocumentCollection;
import com.alibaba.langengine.agentframework.model.service.response.RetrievalSearchResponse;
import com.alibaba.langengine.agentframework.model.service.response.ToolCallingInvokeResponse;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessageRole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.alibaba.agentmagic.framework.delegation.constants.KnowledgeRetrievalConstant.DEFAULT_KNOWLEDGE_TOPN;
import static com.alibaba.agentmagic.framework.delegation.constants.KnowledgeRetrievalConstant.KNOWLEDGE_TYPE_DOCUMENT;

/**
 * 流式消息辅助工具
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class StreamMessageUtils {

    /**
     * 发送开场白流式消息
     *
     * @param systemContext
     * @return
     */
    public static Map<String, Object> sendInitStreamMessage(FrameworkSystemContext systemContext) {
        Long startTime = System.currentTimeMillis();
        String sessionId = systemContext.getSessionId();
        String requestId = systemContext.getRequestId();
        String userId = systemContext.getUserId();
        Consumer<Object> chunkConsumer = systemContext.getChunkConsumer();
        AgentRelation agentRelation = systemContext.getAgentRelation();

        Map<String, Object> output = new HashMap<>();
        output.put("welcomeMessage", agentRelation.getWelcomeMessage());
        output.put("recommendQuestions", agentRelation.getRecommendQuestions());

        if(chunkConsumer != null) {
            AgentAPIInvokeResponse agentAPIResponse = new AgentAPIInvokeResponse();

            String content = JSON.toJSONString(output);
            String sectionId = IdGeneratorUtils.nextId();
            String messageId = IdGeneratorUtils.nextId();

            agentAPIResponse.getMessage().addAll(AgentResponseUtils.buildChatMessageListFromOneAnswer(content, messageId, sectionId, sessionId, userId, startTime));
            AgentAPIResult<AgentAPIInvokeResponse> apiResult = AgentAPIResult.success(agentAPIResponse, requestId);
            chunkConsumer.accept(apiResult);
        }
        return output;
    }

    /**
     * 发送知识库检索结果流式消息
     *
     * @param systemContext
     * @param delegation
     * @return
     */
    public static String sendKnowledgeRetrievalStreamMessage(FrameworkSystemContext systemContext, FrameworkCotCallingDelegation delegation) {
        Long startTime = System.currentTimeMillis();
        String query = systemContext.getQuery();
        String sessionId = systemContext.getSessionId();
        String requestId = systemContext.getRequestId();
        String userId = systemContext.getUserId();
        String env = systemContext.getEnv();
        Consumer<Object> chunkConsumer = systemContext.getChunkConsumer();
        AgentRelation agentRelation = systemContext.getAgentRelation();
        List<KnowledgeRetrievalInput> knowledgeList = null;
        if(CollectionUtils.isEmpty(agentRelation.getKnowledgeList())) {
            return null;
        }
        knowledgeList = agentRelation.getKnowledgeList();

        RetrievalSearchRequest retrievalSearchRequest = new RetrievalSearchRequest();
        retrievalSearchRequest.setKnowledgeInputs(knowledgeList);
        retrievalSearchRequest.setQuery(query);
        retrievalSearchRequest.setKnowledgeTopN(DEFAULT_KNOWLEDGE_TOPN);
        retrievalSearchRequest.setKnowledgeType(KNOWLEDGE_TYPE_DOCUMENT);
        retrievalSearchRequest.setRetrievalStrategyEnabled(false);
        retrievalSearchRequest.setUserId(userId);
//        if(!StringUtils.isEmpty(opensearchIndexTable)) {
//            retrievalSearchRequest.setOpensearchIndexTable(opensearchIndexTable);
//        }

        try {
            String searchRequest = JSON.toJSONString(retrievalSearchRequest);
            log.info("CotRetrievalService search request:" + searchRequest);
            String sectionId = IdGeneratorUtils.nextId();

            // 发送流式消息
            if(chunkConsumer != null) {
                AgentAPIInvokeResponse agentAPIResponse = new AgentAPIInvokeResponse();
                ChatMessage message = new ChatMessage();
                agentAPIResponse.getMessage().add(message);

                ToolCallingInvokeRequest toolCallingInvokeRequest = new ToolCallingInvokeRequest();
                toolCallingInvokeRequest.setToolId("知识库检索");
                toolCallingInvokeRequest.setToolParams(searchRequest);
                toolCallingInvokeRequest.setEnv(env);
                String toolJson = JSON.toJSONString(toolCallingInvokeRequest);

                String messageId = IdGeneratorUtils.nextId();
                message.setRole(ChatMessageRole.ASSISTANT.value());
                message.setType(ChatMessage.TYPE_FUNCTION_CALL);
                message.setContent(toolJson);
                message.setMessageId(messageId);
                message.setSectionId(sectionId);
                message.setSessionId(sessionId);
                message.setSenderId(userId);
                message.setContentType(ChatMessage.CONTENT_TYPE_TEXT);
                message.getExtraInfo().setTimeCost(AgentResponseUtils.getTimeCost(startTime));

                AgentAPIResult apiResult = AgentAPIResult.success(agentAPIResponse, requestId);
                chunkConsumer.accept(apiResult);
            }

            Long knowledgeInvokeStartTime = System.currentTimeMillis();
            setStartProcessLogInfo(systemContext, searchRequest);
            AgentResult<RetrievalSearchResponse> agentResult = delegation.getRetrievalService().search(retrievalSearchRequest);
            long cost = System.currentTimeMillis() - knowledgeInvokeStartTime;
            log.info("CotRetrievalService search response:" + JSON.toJSONString(agentResult) + ",cost:" + cost);
            if (!agentResult.isSuccess()) {
                log.error("CotRetrievalService search error:{}", AgentResult.getAgentResultError(agentResult));
                setExceptionProcessLogInfo(agentResult, null);
                return null;
            }
            if (agentResult.getData() == null) {
                String msg = "CotRetrievalService search error:data is empty";
                log.error(msg);
                setExceptionProcessLogInfo(agentResult, msg);
                return null;
            }
            FrameworkDocumentCollection documentCollection = agentResult.getData().getDocumentCollection();
            if(documentCollection == null) {
                String msg = "CotRetrievalService search error:documentCollection is empty";
                log.error(msg);
                setExceptionProcessLogInfo(agentResult, msg);
                return null;
            }
            setEndProcessLogInfo(agentResult);
            String knowledgeContext = documentCollection.toString();
            if(chunkConsumer != null) {
                AgentAPIInvokeResponse agentAPIResponse = new AgentAPIInvokeResponse();
                ChatMessage message = new ChatMessage();
                agentAPIResponse.getMessage().add(message);

                String messageId = IdGeneratorUtils.nextId();
                message.setRole(ChatMessageRole.ASSISTANT.value());
                message.setType(ChatMessage.TYPE_TOOL_RESPONSE);

                Map<String, Object> contentMap = new HashMap<>();
                contentMap.put("data", knowledgeContext);
                message.setContent(JSON.toJSONString(contentMap));
                message.setMessageId(messageId);
                message.setSectionId(sectionId);
                message.setSessionId(sessionId);
                message.setSenderId(userId);
                message.setContentType(ChatMessage.CONTENT_TYPE_TEXT);
                message.getExtraInfo().setTimeCost(AgentResponseUtils.getTimeCost(knowledgeInvokeStartTime));

                AgentAPIResult agentAPIResult = AgentAPIResult.success(agentAPIResponse, requestId);
                chunkConsumer.accept(agentAPIResult);
            }
            return knowledgeContext;
        } catch (Throwable e) {
            log.error("CotRetrievalService search exception", e);
        }
        return null;
    }

    public static void sendFunctionCallStreamMessage(FrameworkSystemContext systemContext, Long startTime, String firstCost,
                                                     String sectionId,
                                                     String toolJson) {
        Consumer<Object> chunkConsumer = systemContext.getChunkConsumer();
        if(chunkConsumer == null) {
            return;
        }
        String userId = systemContext.getUserId();
        String sessionId = systemContext.getSessionId();
        String requestId = systemContext.getRequestId();
        AgentAPIInvokeResponse agentAPIResponse = new AgentAPIInvokeResponse();
        ChatMessage message = new ChatMessage();
        agentAPIResponse.getMessage().add(message);

        String messageId = IdGeneratorUtils.nextId();
        sectionId = IdGeneratorUtils.nextId();
        message.setRole(ChatMessageRole.ASSISTANT.value());
        message.setType(ChatMessage.TYPE_FUNCTION_CALL);
        message.setContent(toolJson);
        message.setMessageId(messageId);
        message.setSectionId(sectionId);
        message.setSessionId(sessionId);
        message.setSenderId(userId);
        message.setContentType(ChatMessage.CONTENT_TYPE_TEXT);
        message.getExtraInfo().setTimeCost(AgentResponseUtils.getTimeCost(startTime));
        message.getExtraInfo().setFirstCost(firstCost);

        AgentAPIResult apiResult = AgentAPIResult.success(agentAPIResponse, requestId);
        chunkConsumer.accept(apiResult);
    }

    public static void sendToolResponseStreamMessage(FrameworkSystemContext systemContext,
                                                     Long startTime, String firstCost, String sectionId,
                                                     AgentResult<ToolCallingInvokeResponse> agentResult) {
        Consumer<Object> chunkConsumer = systemContext.getChunkConsumer();
        if(chunkConsumer == null) {
            return;
        }
        String userId = systemContext.getUserId();
        String sessionId = systemContext.getSessionId();
        String requestId = systemContext.getRequestId();
        // TODO 待优化
        if(!agentResult.isSuccess()) {
//                AgentAPIResult agentAPIResult = AgentAPIResult.fail(15, AgentMagicErrorCode.TOOL_APIGATEWAY_ERROR.getCode(),
//                        AgentMagicErrorCode.TOOL_APIGATEWAY_ERROR.getMessage(), agentResult.getErrorCode() + "," + agentResult.getErrorMsg(), agentResult.getRequestId(), requestId);

            AgentAPIInvokeResponse agentAPIResponse = new AgentAPIInvokeResponse();
            ChatMessage message = new ChatMessage();
            agentAPIResponse.getMessage().add(message);

            String messageId = IdGeneratorUtils.nextId();
            message.setRole(ChatMessageRole.ASSISTANT.value());
            message.setType(ChatMessage.TYPE_TOOL_RESPONSE);

            message.setContent(JSON.toJSONString(agentResult));
            message.setMessageId(messageId);
            message.setSectionId(sectionId);
            message.setSessionId(sessionId);
            message.setSenderId(userId);
            message.setContentType(ChatMessage.CONTENT_TYPE_TEXT);
            message.getExtraInfo().setTimeCost(AgentResponseUtils.getTimeCost(startTime));
            message.getExtraInfo().setFirstCost(firstCost);
            // 内部错误坐标
            message.getExtraInfo().setInnerSuccess(false);

            AgentAPIResult agentAPIResult = AgentAPIResult.success(agentAPIResponse, requestId);

            chunkConsumer.accept(agentAPIResult);
        } else {
            AgentAPIInvokeResponse agentAPIResponse = new AgentAPIInvokeResponse();
            ChatMessage message = new ChatMessage();
            agentAPIResponse.getMessage().add(message);

            String messageId = IdGeneratorUtils.nextId();
            message.setRole(ChatMessageRole.ASSISTANT.value());
            message.setType(ChatMessage.TYPE_TOOL_RESPONSE);
            message.setContent(agentResult.getData() == null ? "{}" : JSON.toJSONString(agentResult.getData().getToolApiResult()));
            message.setMessageId(messageId);
            message.setSectionId(sectionId);
            message.setSessionId(sessionId);
            message.setSenderId(userId);
            message.setContentType(ChatMessage.CONTENT_TYPE_TEXT);
            message.getExtraInfo().setTimeCost(AgentResponseUtils.getTimeCost(startTime));
            message.getExtraInfo().setFirstCost(firstCost);
            //内部成功坐标
            message.getExtraInfo().setInnerSuccess(true);

            AgentAPIResult agentAPIResult = AgentAPIResult.success(agentAPIResponse, requestId);
            chunkConsumer.accept(agentAPIResult);
        }
    }

    public static void sendCardStreamMessage(FrameworkSystemContext systemContext, JSONArray cardConfig) {
        Map<String, Object> cardMap = new HashMap<>();
        cardMap.put("cardConfig", cardConfig);
        sendMessage(systemContext, JSON.toJSONString(cardMap), ChatMessage.TYPE_ANSWER, ChatMessage.CONTENT_TYPE_COMPONENT_CARD);
    }

    public static void sendCardStreamMessage(FrameworkSystemContext systemContext, JSONArray cardConfig, String answerType) {
        Map<String, Object> cardMap = new HashMap<>();
        cardMap.put("cardConfig", cardConfig);
        sendMessage(systemContext, JSON.toJSONString(cardMap), answerType, ChatMessage.CONTENT_TYPE_COMPONENT_CARD);
    }

    public static void sendTextAnswer(FrameworkSystemContext systemContext, String content, String contentType) {
        sendMessage(systemContext, content, ChatMessage.TYPE_ANSWER, contentType);
    }

    private static void sendMessage(FrameworkSystemContext systemContext, String content, String messageType, String contentType) {
        String sectionId = IdGeneratorUtils.nextId();
        Consumer<Object> chunkConsumer = systemContext.getChunkConsumer();
        if(chunkConsumer == null) {
            return;
        }
        String userId = systemContext.getUserId();
        String sessionId = systemContext.getSessionId();
        String requestId = systemContext.getRequestId();
        AgentAPIInvokeResponse agentAPIResponse = new AgentAPIInvokeResponse();
        ChatMessage message = new ChatMessage();
        agentAPIResponse.getMessage().add(message);

        String messageId = IdGeneratorUtils.nextId();
        message.setRole(ChatMessageRole.ASSISTANT.value());
        message.setType(messageType);
        message.setContent(content);
        message.setMessageId(messageId);
        message.setSectionId(sectionId);
        message.setSessionId(sessionId);
        message.setSenderId(userId);
        message.setContentType(contentType);
        //内部成功坐标
        message.getExtraInfo().setInnerSuccess(true);

        AgentAPIResult agentAPIResult = AgentAPIResult.success(agentAPIResponse, requestId);
        log.info("sendCardStreamMessage body is " + JSON.toJSONString(agentAPIResult));
        chunkConsumer.accept(agentAPIResult);
    }

    private static void setStartProcessLogInfo(FrameworkSystemContext systemContext, String searchRequest) {
        ProcessExecuteLog log = new ProcessExecuteLog();
        String agentCode = systemContext.getAgentCode();
        String requestId = systemContext.getRequestId();

        log.setTemplateType("knowledge");
        log.setActivityType(TemplateTypeEnums.knowledge.getActivityType());
        log.setActivityName(TemplateTypeEnums.knowledge.getName());
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("toolName", "知识库检索");
        requestMap.put("toolDesc", "知识库检索");
        requestMap.put("input", systemContext.getQuery());
        requestMap.put("params", searchRequest);
        log.setRequest(JSON.toJSONString(requestMap));

        log.setAgentCode(agentCode);
        log.setRequestId(requestId);
        log.setSuccess(true);
        log.setParentTemplateType("cot");
        log.setStartTime(System.currentTimeMillis());

        Map<String, ProcessExecuteLog> map = ToolCallingLogContext.getLog();
        if(MapUtils.isEmpty(map)) {
            map = new HashMap<>();
            map.put("知识库检索", log);
            ToolCallingLogContext.set(map);
        } else {
            map.put("知识库检索", log);
        }
    }

    private static void setEndProcessLogInfo(AgentResult<RetrievalSearchResponse> agentResult) {
        Map<String, ProcessExecuteLog> map = ToolCallingLogContext.getLog();
        if(MapUtils.isNotEmpty(map) && map.containsKey("知识库检索")) {
            ProcessExecuteLog log = map.get("知识库检索");
            if(Objects.isNull(log)) {
                return;
            }
            log.setEndTime(System.currentTimeMillis());
            log.setExecuteTime(log.getEndTime() - log.getStartTime());
            if(Objects.nonNull(agentResult) && Objects.nonNull(agentResult.getData())) {
                log.setResponse(JSON.toJSONString(agentResult.getData()));
            }
            ProcessExecuteLog.stop(log);
        }
    }

    private static void setExceptionProcessLogInfo(AgentResult<RetrievalSearchResponse> agentResult, String message) {
        Map<String, ProcessExecuteLog> map = ToolCallingLogContext.getLog();
        if(MapUtils.isNotEmpty(map) && map.containsKey("知识库检索")) {
            ProcessExecuteLog log = map.get("知识库检索");
            if(Objects.isNull(log)) {
                return;
            }
            log.setEndTime(System.currentTimeMillis());
            log.setExecuteTime(log.getEndTime() - log.getStartTime());
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("errorCode", agentResult.getErrorCode());
            resultMap.put("errorDetail", agentResult.getErrorDetail());
            resultMap.put("message", message);
            log.setResponse(JSON.toJSONString(resultMap));
            ProcessExecuteLog.stop(log);
        }
    }
}
