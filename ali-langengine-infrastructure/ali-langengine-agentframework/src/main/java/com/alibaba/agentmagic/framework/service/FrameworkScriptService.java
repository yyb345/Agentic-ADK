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
package com.alibaba.agentmagic.framework.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.agentframework.model.AgentResult;
import com.alibaba.langengine.agentframework.model.FrameworkEngineConfiguration;
import com.alibaba.langengine.agentframework.model.service.ScriptService;
import com.alibaba.langengine.agentframework.model.service.ServiceBase;
import com.alibaba.langengine.agentframework.model.service.request.ScriptEvaluateRequest;
import com.alibaba.langengine.agentframework.model.service.response.ScriptEvaluateResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FrameworkScriptService extends ServiceBase implements ScriptService {

    public FrameworkScriptService(FrameworkEngineConfiguration agentEngineConfiguration) {
        super(agentEngineConfiguration);
    }

    @Override
    public AgentResult<ScriptEvaluateResponse> evaluate(ScriptEvaluateRequest request) {
        log.info("DefaultScriptService evaluate request:" + JSON.toJSONString(request));
        ScriptEvaluateResponse scriptEvaluateResponse = new ScriptEvaluateResponse();
        scriptEvaluateResponse.setResult(request.getContext());
        return AgentResult.success(scriptEvaluateResponse);
    }
}
