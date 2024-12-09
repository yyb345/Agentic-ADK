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
package com.alibaba.langengine.agentframework.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.agentframework.model.agent.AgentModel;
import com.alibaba.langengine.agentframework.model.service.AgentChatService;
import com.alibaba.langengine.agentframework.model.service.ResultDO;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class FrameworkAgentChatService implements AgentChatService {

    @Override
    public ResultDO<Map<String, Object>> chat(String query, AgentModel agentModel) {
        log.info("DefaultAgentChatService chat query:" + query + ", agentModel:" + JSON.toJSONString(agentModel));
        Map<String, Object> response = new HashMap<>();
        response.put("answer", "mock1");
        return ResultDO.success(response);
    }

    @Override
    public ResultDO<Map<String, Object>> chat(String query, AgentModel agentModel, String sessionId, Boolean isDebug) {
        log.info("DefaultAgentChatService chat query:" + query + ", agentModel:" + JSON.toJSONString(agentModel) + ", sessionId:" + sessionId + ", isDebug:" + isDebug);
        Map<String, Object> response = new HashMap<>();
        response.put("answer", "mock1");
        return ResultDO.success(response);
    }
}
