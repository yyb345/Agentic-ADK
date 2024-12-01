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

import com.alibaba.langengine.agentframework.delegation.FrameworkCotCallingDelegation;
import com.alibaba.langengine.agentframework.delegation.constants.ToolCallingConstant;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.agentframework.model.agent.domain.ComponentCallingInput;
import com.alibaba.langengine.agentframework.model.domain.FrameworkSystemContext;
import com.alibaba.langengine.agentframework.model.enums.AgentMagicErrorCode;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.FunctionMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * description
 *
 * @Author zhishan
 * @Date 2024-08-21
 */
@Slf4j
public class ShortcutExecutor extends BaseCotExecutor {

    public Map<String, Object> execute(FrameworkSystemContext systemContext, FrameworkCotCallingDelegation delegation) {
        String requestId = systemContext.getRequestId();
        log.info("start invokeAgent requestId=" + requestId);

        Object shortcutContent = systemContext.getShortcutContent();
        if (!(shortcutContent instanceof Map)) {
            log.error("shortcut context is null, requestId is " + requestId);
            throw new AgentMagicException(AgentMagicErrorCode.LLM_SYSTEM_ERROR, "shortcut context is null", requestId);
        }

        Map<String, Object> shortcutContentMap = (Map<String, Object>) shortcutContent;
        String name = (String) shortcutContentMap.get(ToolCallingConstant.API_CODE_KEY);
        String apiVersion = (String) shortcutContentMap.get(ToolCallingConstant.API_VERSION_KEY);
        Object paramJsonObj = shortcutContentMap.get(ToolCallingConstant.TOOL_PARAM_JSON_KEY);
        String arguments = "";
        if (paramJsonObj != null) {
            arguments = JSONObject.toJSONString(paramJsonObj);
        }

        // 执行指令并生成回复
        ComponentCallingInput component = new ComponentCallingInput(name, apiVersion);
        FunctionMessage functionMessage = executeTool(systemContext, delegation.getToolCallingService(), component, arguments);
        List<BaseMessage> intermediateMessages = new ArrayList<>();
        if (functionMessage != null) {
            intermediateMessages.add(functionMessage);
        } else {
            log.error("executeTool return null, requestId is " + requestId);
        }

        // 不需要LLM来决策工具调用，所以这里把工具信息删除
        Optional.ofNullable(systemContext.getAgentRelation()).ifPresent(agentRelation -> agentRelation.setComponentList(new ArrayList<>()));
        log.info("clear componentList, requestId is " + requestId);

        return callLlm(systemContext, delegation, null, intermediateMessages);
    }
}
