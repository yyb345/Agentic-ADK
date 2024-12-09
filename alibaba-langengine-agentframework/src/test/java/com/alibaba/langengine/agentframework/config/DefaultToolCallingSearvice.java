/**
 * Copyright (C) 2024 AIDC-AI
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.agentframework.config;

import com.alibaba.langengine.agentframework.model.AgentResult;
import com.alibaba.langengine.agentframework.model.service.ToolCallingService;
import com.alibaba.langengine.agentframework.model.service.request.ToolCallingInvokeRequest;
import com.alibaba.langengine.agentframework.model.service.response.ToolCallingInvokeResponse;
import com.alibaba.langengine.core.agent.Agent;

import java.util.HashMap;
import java.util.Map;

public class DefaultToolCallingSearvice implements ToolCallingService {

    @Override
    public AgentResult<ToolCallingInvokeResponse> invoke(ToolCallingInvokeRequest request) {
        if("getWeather".equals(request.getToolId())) {
            ToolCallingInvokeResponse response = new ToolCallingInvokeResponse();
            Map<String, Object> toolApiResult = new HashMap<>();
            toolApiResult.put("result", "杭州的天气今天是10摄氏度");
            response.setToolApiResult(toolApiResult);
            return AgentResult.success(response);
        }
        return null;
    }
}
