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
import com.alibaba.langengine.agentframework.model.AgentResult;
import com.alibaba.langengine.agentframework.model.FrameworkEngineConfiguration;
import com.alibaba.langengine.agentframework.model.service.ServiceBase;
import com.alibaba.langengine.agentframework.model.service.ToolCallingService;
import com.alibaba.langengine.agentframework.model.service.request.ToolCallingInvokeRequest;
import com.alibaba.langengine.agentframework.model.service.response.ToolCallingInvokeResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class FrameworkToolCallingService extends ServiceBase implements ToolCallingService {

    public FrameworkToolCallingService(FrameworkEngineConfiguration agentEngineConfiguration) {
        super(agentEngineConfiguration);
    }

    @Override
    public AgentResult<ToolCallingInvokeResponse> invoke(ToolCallingInvokeRequest request) {
        log.info("FrameworkToolCallingService invoke request:" + JSON.toJSONString(request));
        Map<String, Object> toolApiResult = new HashMap<>();
        toolApiResult.put("response", "Tool mock result.");
        if(request.getChunkConsumer() != null) {
            request.getChunkConsumer().accept(toolApiResult);
        }
        ToolCallingInvokeResponse toolCallingInvokeResponse = new ToolCallingInvokeResponse();
        toolCallingInvokeResponse.setToolApiResult(toolApiResult);
        return AgentResult.success(toolCallingInvokeResponse);
    }
}
