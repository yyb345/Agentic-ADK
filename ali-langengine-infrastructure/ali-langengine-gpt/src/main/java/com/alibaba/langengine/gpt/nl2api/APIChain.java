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
package com.alibaba.langengine.gpt.nl2api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chain.Chain;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Chain for interacting with API
 * 用于与API交互的链。
 *
 * @author pingkuang.pk
 */
@Data
@Slf4j
public class APIChain extends Chain {
    private LLMChain apiRequestChain;
    private LLMChain apiAnswerChain;
    private TextRequestsWrapper requestsWrapper;
    private String apiDocs;
    public final static String QUESTION_KEY = "question";
    public final static String OUTPUT_KEY = "output";
    public final static String NEED_SUMMARY_KEY = "needSummary";

    public APIChain(LLMChain apiRequestChain, LLMChain apiAnswerChain, TextRequestsWrapper requestsWrapper,
        String apiDocs) {
        this.apiRequestChain = apiRequestChain;
        this.apiAnswerChain = apiAnswerChain;
        this.requestsWrapper = requestsWrapper;
        this.apiDocs = apiDocs;
    }

    public static APIChain fromLlmAndApiDocs(BaseLanguageModel llm, String apiDocs, Map<String, String> headers) {
        return fromLlmAndApiDocs(llm, llm, apiDocs, headers);
    }

    public static APIChain fromLlmAndApiDocs(BaseLanguageModel apiRequestLlm, BaseLanguageModel apiAnswerLlm,
        String apiDocs, Map<String, String> headers) {
        LLMChain getRequestChain = new LLMChain();
        getRequestChain.setLlm(apiRequestLlm);
        getRequestChain.setPrompt(PromptConstants.API_URL_PROMPT);
        getRequestChain.getPrompt().setOutputParser(apiRequestLlm.getAPIChainUrlOutputParser());
        LLMChain getAnswerChain = new LLMChain();
        getAnswerChain.setLlm(apiAnswerLlm);
        getAnswerChain.setPrompt(PromptConstants.API_RESPONSE_PROMPT);
        TextRequestsWrapper textRequestsWrapper = new TextRequestsWrapper(headers);
        return new APIChain(getRequestChain, getAnswerChain, textRequestsWrapper, apiDocs);
    }

    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext,
        Consumer<String> consumer, Map<String, Object> extraAttributes) {
        Map<String, Object> chainResult = new HashMap<>();
        String question = MapUtils.getString(inputs, QUESTION_KEY);
        Boolean needSummary = MapUtils.getBoolean(inputs, NEED_SUMMARY_KEY, true);
        if (StringUtils.isBlank(question)) {
            return chainResult;
        }

        // 获取url
        Map<String, Object> apiRequestInput = Maps.newHashMap();
        apiRequestInput.put("question", question);
        apiRequestInput.put("api_docs", apiDocs);
        String url = (String)apiRequestChain.predictAndParse(apiRequestInput, extraAttributes);
        //        String url = MapUtils.getString(apiRequestResult, "text", "").trim();
        if (StringUtils.isBlank(url)) {
            return chainResult;
        }
        if (!validateUrl(url)) {
            log.error("url format is incorrect: {}", url);
            return chainResult;
        }

        // 请求url
        String response = requestsWrapper.get(url);
        if (StringUtils.isBlank(response)) {
            return chainResult;
        }
        if (!needSummary) {
            chainResult.put(OUTPUT_KEY, response);
            return chainResult;
        }

        // 获取答案summary
        Map<String, Object> apiAnswerInput = Maps.newHashMap();
        apiAnswerInput.put("question", question);
        apiAnswerInput.put("api_docs", apiDocs);
        apiAnswerInput.put("api_url", url);
        apiAnswerInput.put("api_response", response);
        Map<String, Object> apiAnswerResult = apiAnswerChain.run(apiAnswerInput, executionContext, null, extraAttributes);

        // 返回答案
        chainResult.put(OUTPUT_KEY, MapUtils.getString(apiAnswerResult, "text", "").trim());
        return chainResult;
    }

    @Override
    public List<String> getInputKeys() {
        return Collections.singletonList(QUESTION_KEY);
    }

    @Override
    public List<String> getOutputKeys() {
        return Collections.singletonList(OUTPUT_KEY);
    }

    private static boolean validateUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
