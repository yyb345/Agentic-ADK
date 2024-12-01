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
import com.alibaba.langengine.agentframework.model.service.LanguageModelService;
import com.alibaba.langengine.agentframework.model.service.request.LanguageModelCallRequest;
import com.alibaba.langengine.agentframework.model.service.request.LanguageModelGetRequest;
import com.alibaba.langengine.agentframework.model.service.request.LanguageModelSuggestGetRequest;
import com.alibaba.langengine.agentframework.model.service.response.LanguageModelCallResponse;
import com.alibaba.langengine.agentframework.model.service.response.LanguageModelGetResponse;
import com.alibaba.langengine.agentframework.model.service.response.LanguageModelSuggestGetResponse;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;

public class DefaultLanguageModelService implements LanguageModelService {

    @Override
    public AgentResult<LanguageModelCallResponse> call(LanguageModelCallRequest request) {
        return null;
    }

    @Override
    public AgentResult<LanguageModelGetResponse> getLanguageModel(LanguageModelGetRequest request) {
        LanguageModelGetResponse response = new LanguageModelGetResponse();
        ChatModelOpenAI llm = new ChatModelOpenAI();
        response.setLanguageModel(llm);
        return AgentResult.success(response);
    }

    @Override
    public AgentResult<LanguageModelSuggestGetResponse> getSuggestLanguageModel(LanguageModelSuggestGetRequest request) {
        return null;
    }
}
