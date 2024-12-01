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
package com.alibaba.langengine.agentframework.delegation.provider;

import com.alibaba.langengine.agentframework.delegation.constants.LlmCallingConstant;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.langengine.agentframework.delegation.provider.DelegationHelper.isSystemKey;

@Slf4j
public class AssignDelegationHelper implements LlmCallingConstant {

    public static Map<String, Object> executeInternal(ExecutionContext executionContext,
                                                      JSONObject properties,
                                                      JSONObject request) {
        Map<String, Object> outputs = new HashMap<>();
        for (String key : properties.keySet()) {
            if (isSystemKey(key)) {
                continue;
            }
            Object obj = properties.get(key);
            request.put(key, obj);
            outputs.put(key, obj);
        }
        return outputs;
    }
}
