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
package com.alibaba.langengine.agentframework.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.agentframework.delegation.constants.SystemConstant;
import com.alibaba.langengine.agentframework.delegation.provider.DelegationHelper;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.agentframework.engine.AgentOriginRequest;
import com.alibaba.langengine.agentframework.model.agent.AgentModel;
import com.alibaba.langengine.agentframework.model.agent.domain.AgentRelation;
import com.alibaba.langengine.agentframework.model.agent.flow.FlowAgentModel;
import com.alibaba.langengine.agentframework.model.domain.ChatAttachment;
import com.alibaba.langengine.agentframework.model.domain.ChatMessage;
import com.alibaba.langengine.agentframework.model.domain.FrameworkSystemContext;
import com.alibaba.smart.framework.engine.constant.RequestMapSpecialKeyConstant;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.alibaba.langengine.agentframework.delegation.constants.SystemConstant.*;

/**
 * 框架级系统上下文变量工具
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class FrameworkSystemContextUtils {

    public static void putSystemContext(Map<String, Object> context, AgentOriginRequest agentOriginRequest) {
        // 设置系统变量
        Map<String, Object> system = new HashMap<String, Object>() {{
            // 会话问答
            put(SystemConstant.QUERY_KEY, agentOriginRequest.getQuery());
            // 会话附件
            if(agentOriginRequest.getAttachments() != null) {
                put(SystemConstant.CHAT_ATTACHMENT_KEY, agentOriginRequest.getAttachments());
                List<String> attachementUrlList = new ArrayList<>();
                for (ChatAttachment attachment : agentOriginRequest.getAttachments()) {
                    if(!StringUtils.isEmpty(attachment.getUrl())) {
                        attachementUrlList.add(attachment.getUrl());
                    } else {
                        if(attachment.getProps() != null && attachment.getProps().get("url") != null) {
                            attachementUrlList.add(attachment.getProps().get("url").toString());
                        }
                    }
                }
                log.info("attachmentUrlList is " + JSON.toJSONString(attachementUrlList));
                put(SystemConstant.ATTACHMENT_URL_LIST_KEY, attachementUrlList);
            }

            AgentModel agentModel = agentOriginRequest.getAgentModel();
            if (agentModel instanceof FlowAgentModel) {
                FlowAgentModel flowAgentModel = (FlowAgentModel) agentModel;

                if (flowAgentModel.getRelation() != null) {
                    put(SystemConstant.AGENT_RELATION_KEY, flowAgentModel.getRelation());
                    put(SystemConstant.WELCOME_MESSAGE_KEY, flowAgentModel.getRelation().getWelcomeMessage());
                    put(SystemConstant.RECOMMEND_QUESTIONS_KEY, flowAgentModel.getRelation().getRecommendQuestions());
                }
            }
            // 是否异步调用
            put(SystemConstant.ASYNC_KEY, agentOriginRequest.getAsync());
            put(SystemConstant.SESSION_ID_KEY, agentOriginRequest.getSessionId());
//            put(SystemConstant.HISTORY_KEY, buildChatMsg(agentOriginRequest, sessionId));

            if (agentOriginRequest.getChunkConsumer() != null) {
                put(SystemConstant.CHUNK_CONSUMER_KEY, agentOriginRequest.getChunkConsumer());
            }

            put(SystemConstant.AGENT_CODE_KEY, agentOriginRequest.getAgentCode());
            put(SystemConstant.AGENT_NAME_KEY, agentOriginRequest.getAgentName());
            put(SystemConstant.REQUEST_ID_KEY, agentOriginRequest.getRequestId());
        }};
        context.put(SYSTEM_KEY, system);
        // 并发超时时间
        context.put(RequestMapSpecialKeyConstant.LATCH_WAIT_TIME_IN_MILLISECOND, 3 * 60 * 1000L);
    }

    /**
     * 临时用于兼容，建议全都改成getSystemContext(JSONObject request, ExecutionContext executionContext)
     *
     * @param request
     * @return
     */
    @Deprecated
    public static FrameworkSystemContext getSystemContext(JSONObject request) {
        return getSystemContext(request, null);
    }

    public static FrameworkSystemContext getSystemContext(JSONObject request, ExecutionContext executionContext) {
        Boolean isInit = DelegationHelper.getSystemBooleanOrDefault(request, SystemConstant.IS_INIT_KEY, false);
        String agentCode = DelegationHelper.getSystemValue(request, AGENT_CODE_KEY);
        String requestId = DelegationHelper.getSystemString(request, SystemConstant.REQUEST_ID_KEY);

        Object agentRelationObj = DelegationHelper.getSystem(request, AGENT_RELATION_KEY);
        AgentRelation agentRelation = null;
        if(agentRelationObj != null) {
            if(agentRelationObj instanceof JSONObject) {
                JSONObject agentRelationJSONObject = (JSONObject) agentRelationObj;
                agentRelation = agentRelationJSONObject.toJavaObject(AgentRelation.class);
            } else {
                agentRelation = (AgentRelation) agentRelationObj;
            }
        }

        Map<String, Object> invokeContext = null;
        Object invokeContextObj = DelegationHelper.getSystem(request, INVOKE_CONTEXT_KEY);
        if(invokeContextObj != null) {
            invokeContext = (Map<String, Object>) invokeContextObj;
        }

        String query = DelegationHelper.getSystemValue(request, QUERY_KEY);
        String sessionId = DelegationHelper.getSystemValue(request, SystemConstant.SESSION_ID_KEY);
        String userId = DelegationHelper.getSystemString(request, SystemConstant.USER_ID_KEY);
        String env = DelegationHelper.getSystemString(request, SystemConstant.ENV_KEY);
        String projectName = DelegationHelper.getSystemString(request, PROJECT_NAME_KEY);
//        Boolean flowNodeStream = DelegationHelper.getSystemBooleanOrDefault(request, FLOW_NODE_STREAM_KEY, false);
        List<ChatAttachment> chatAttachments = null;
        Object chatAttachmentObj = DelegationHelper.getSystem(request, SystemConstant.CHAT_ATTACHMENT_KEY);
        if(chatAttachmentObj != null) {
            chatAttachments = (List<ChatAttachment>)chatAttachmentObj;
        }
        Boolean apiKeyCall = DelegationHelper.getSystemBooleanOrDefault(request, SystemConstant.API_KEY_CALL_KEY, false);
        String apiKey = null;
        if (apiKeyCall) {
            apiKey = DelegationHelper.getSystemString(request, SystemConstant.APIKEY_KEY);
        }
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

        Boolean async = DelegationHelper.getSystemBooleanOrDefault(request, SystemConstant.ASYNC_KEY, false);

        FrameworkSystemContext systemContext = new FrameworkSystemContext();
        systemContext.setShortcutContent(request.get(SystemConstant.SHORTCUT_CONTENT_KEY));
        systemContext.setIsInit(isInit);
        systemContext.setAgentRelation(agentRelation);
        systemContext.setQuery(query);
        systemContext.setAgentCode(agentCode);
        systemContext.setSessionId(sessionId);
        systemContext.setRequestId(requestId);
        systemContext.setUserId(userId);
        systemContext.setApikeyCall(apiKeyCall);
        systemContext.setApikey(apiKey);
        systemContext.setEnv(env);
        systemContext.setProjectName(projectName);
        systemContext.setChatAttachments(chatAttachments);
        systemContext.setChunkConsumer(chunkConsumer);
        systemContext.setHistory(chatHistory);
        systemContext.setAsync(async);
        systemContext.setInvokeContext(invokeContext);
        systemContext.setExecutionContext(executionContext);

        return systemContext;
    }
}
