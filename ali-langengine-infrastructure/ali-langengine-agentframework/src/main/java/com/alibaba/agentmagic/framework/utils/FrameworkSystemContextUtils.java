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
package com.alibaba.agentmagic.framework.utils;

import com.alibaba.agentmagic.framework.delegation.constants.SystemConstant;
import com.alibaba.agentmagic.framework.delegation.provider.DelegationHelper;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.agentframework.model.agent.domain.AgentRelation;
import com.alibaba.langengine.agentframework.model.domain.ChatAttachment;
import com.alibaba.langengine.agentframework.model.domain.ChatMessage;
import com.alibaba.langengine.agentframework.model.domain.FrameworkSystemContext;
import com.alibaba.smart.framework.engine.context.ExecutionContext;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.alibaba.agentmagic.framework.delegation.constants.SystemConstant.*;

/**
 * 框架级系统上下文变量工具
 *
 * @author xiaoxuan.lp
 */
public class FrameworkSystemContextUtils {

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
//        Boolean batch = DelegationHelper.getSystemBooleanOrDefault(request, BATCH_KEY, false);
//        Boolean offline = DelegationHelper.getSystemBooleanOrDefault(request, OFFLINE_KEY, false);

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
//        systemContext.setBatch(batch);
//        systemContext.setOffline(offline);
//        systemContext.setFlowNodeStream(flowNodeStream);

        return systemContext;
    }
}
