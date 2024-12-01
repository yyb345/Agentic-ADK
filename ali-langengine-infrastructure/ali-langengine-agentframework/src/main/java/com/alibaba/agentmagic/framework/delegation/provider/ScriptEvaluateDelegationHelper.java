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

import com.alibaba.agentmagic.framework.delegation.constants.ScriptEvaluateConstant;
import com.alibaba.agentmagic.framework.delegation.constants.SystemConstant;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.agentframework.model.AgentResult;
import com.alibaba.langengine.agentframework.model.enums.AgentMagicErrorCode;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.langengine.agentframework.model.service.ScriptService;
import com.alibaba.langengine.agentframework.model.service.request.ScriptEvaluateRequest;
import com.alibaba.langengine.agentframework.model.service.response.ScriptEvaluateResponse;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ScriptEvaluateDelegationHelper implements ScriptEvaluateConstant {

    private static Map<String, ScriptService> scriptServiceMap = new ConcurrentHashMap<>();

    public static Map<String, Object> executeInternal(ExecutionContext executionContext,
                                                      JSONObject properties,
                                                      JSONObject request,
                                                      ScriptService scriptService) {
        String requestId = DelegationHelper.getSystemValue(request, SystemConstant.REQUEST_ID_KEY);
        String scriptServiceClassName = properties.getString(SCRIPT_SERVICE_KEY);

        if(scriptServiceClassName != null) {
            if (!scriptServiceMap.containsKey(scriptServiceClassName)) {
                try {
                    Class<?> classObj = Class.forName(scriptServiceClassName);
                    Object instance = classObj.getDeclaredConstructor().newInstance();

                    if (ScriptService.class.isAssignableFrom(classObj)) {
                        scriptService = (ScriptService) instance;
                        scriptServiceMap.put(scriptServiceClassName, scriptService);
                    }
                } catch (Throwable e) {
                    throw new AgentMagicException(AgentMagicErrorCode.SCRIPT_EVALUATE_ERROR, e.getMessage(), requestId);
                }
            }
            scriptService = scriptServiceMap.get(scriptServiceClassName);
        }

        ScriptEvaluateRequest scriptEvaluateRequest = new ScriptEvaluateRequest();
        scriptEvaluateRequest.setContext(request);
        AgentResult<ScriptEvaluateResponse> agentResult = scriptService.evaluate(scriptEvaluateRequest);
        if(!agentResult.isSuccess() || agentResult.getData() == null) {
            throw new AgentMagicException(AgentMagicErrorCode.SCRIPT_EVALUATE_ERROR,
                    "scriptService evaluate error:" + agentResult.getErrorCode() + "," + agentResult.getErrorMsg(), requestId);
        }
        return agentResult.getData().getResult();
    }
}
