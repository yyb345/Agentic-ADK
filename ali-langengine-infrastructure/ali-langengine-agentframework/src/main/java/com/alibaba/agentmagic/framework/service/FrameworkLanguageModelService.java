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
import com.alibaba.langengine.agentframework.model.service.LanguageModelService;
import com.alibaba.langengine.agentframework.model.service.ServiceBase;
import com.alibaba.langengine.agentframework.model.service.request.LanguageModelCallRequest;
import com.alibaba.langengine.agentframework.model.service.request.LanguageModelGetRequest;
import com.alibaba.langengine.agentframework.model.service.request.LanguageModelSuggestGetRequest;
import com.alibaba.langengine.agentframework.model.service.response.LanguageModelCallResponse;
import com.alibaba.langengine.agentframework.model.service.response.LanguageModelGetResponse;
import com.alibaba.langengine.agentframework.model.service.response.LanguageModelSuggestGetResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.langengine.agentframework.model.constant.ModelConstants.LLM_RESULT_KEY;

@Slf4j
public class FrameworkLanguageModelService extends ServiceBase implements LanguageModelService {

    public FrameworkLanguageModelService(FrameworkEngineConfiguration agentEngineConfiguration) {
        super(agentEngineConfiguration);
    }

    @Override
    public AgentResult<LanguageModelCallResponse> call(LanguageModelCallRequest request) {
        log.info("FrameworkLanguageModelService call request:" + JSON.toJSONString(request));
        LanguageModelCallResponse languageModelCallResponse = new LanguageModelCallResponse();
        Map<String, Object> outputs = new HashMap<>();
        outputs.put(LLM_RESULT_KEY, request.getQuery());
        languageModelCallResponse.setOutputs(outputs);
        return AgentResult.success(languageModelCallResponse);
    }

    @Override
    public AgentResult<LanguageModelGetResponse> getLanguageModel(LanguageModelGetRequest request) {
        return null;
    }

    @Override
    public AgentResult<LanguageModelSuggestGetResponse> getSuggestLanguageModel(LanguageModelSuggestGetRequest request) {
        return null;
    }
}
