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

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.agentframework.delegation.constants.SystemConstant;
import com.alibaba.langengine.agentframework.model.AgentResult;
import com.alibaba.langengine.agentframework.model.service.LanguageModelService;
import com.alibaba.langengine.agentframework.model.service.request.LanguageModelCallRequest;
import com.alibaba.langengine.agentframework.model.service.request.LanguageModelGetRequest;
import com.alibaba.langengine.agentframework.model.service.request.LanguageModelSuggestGetRequest;
import com.alibaba.langengine.agentframework.model.service.response.LanguageModelCallResponse;
import com.alibaba.langengine.agentframework.model.service.response.LanguageModelGetResponse;
import com.alibaba.langengine.agentframework.model.service.response.LanguageModelSuggestGetResponse;
import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.model.BaseLLM;
import com.alibaba.langengine.core.outputparser.BaseOutputParser;
import com.alibaba.langengine.core.outputparser.StrOutputParser;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.runnables.RunnableHashMap;
import com.alibaba.langengine.core.runnables.RunnableInterface;
import com.alibaba.langengine.core.runnables.RunnableStringVar;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.langengine.agentframework.model.constant.ModelConstants.LLM_RESULT_KEY;

@Slf4j
public class DefaultLanguageModelService implements LanguageModelService {

    @Override
    public AgentResult<LanguageModelCallResponse> call(LanguageModelCallRequest request) {
        log.info("DefaultLanguageModelService call request:" + JSON.toJSONString(request));
        LanguageModelCallResponse languageModelCallResponse = new LanguageModelCallResponse();

        BaseLanguageModel baseLanguageModel = new ChatModelOpenAI();
        BaseOutputParser baseOutputParser = new StrOutputParser();
        if(baseLanguageModel instanceof BaseChatModel) {
            BaseChatModel baseChatModel = (BaseChatModel) baseLanguageModel;
            ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate(request.getPrompt());

            RunnableInterface chain = com.alibaba.langengine.core.runnables.Runnable.sequence(prompt, baseChatModel, baseOutputParser);

            RunnableHashMap input = new RunnableHashMap() {{
                put(SystemConstant.QUERY_KEY, request.getQuery());
            }};
            Object runnableOutput;
            if(request.getChunkConsumer() != null) {
                runnableOutput = chain.stream(input, request.getChunkConsumer());
            } else {
                runnableOutput = chain.invoke(input);
            }
            if(runnableOutput instanceof RunnableStringVar) {
                Map<String, Object> outputs = new HashMap<>();
                outputs.put(LLM_RESULT_KEY, ((RunnableStringVar) runnableOutput).getValue());
                languageModelCallResponse.setOutputs(outputs);
            }
        } else {
            BaseLLM baseLLM = (BaseLLM) baseLanguageModel;
            PromptTemplate prompt = new PromptTemplate(request.getPrompt());

            RunnableInterface chain = Runnable.sequence(prompt, baseLLM, baseOutputParser);

            RunnableHashMap input = new RunnableHashMap() {{
                put(SystemConstant.QUERY_KEY, request.getQuery());
            }};
            Object runnableOutput;
            if(request.getChunkConsumer() != null) {
                runnableOutput = chain.stream(input, request.getChunkConsumer());
            } else {
                runnableOutput = chain.invoke(input);
            }
            if(runnableOutput instanceof RunnableStringVar) {
                Map<String, Object> outputs = new HashMap<>();
                outputs.put(LLM_RESULT_KEY, ((RunnableStringVar) runnableOutput).getValue());
                languageModelCallResponse.setOutputs(outputs);
            }
        }
        return AgentResult.success(languageModelCallResponse);
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
