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
package com.alibaba.langengine.gpt.nl2opensearch;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chain.Chain;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.util.DateUtils;

import com.alibaba.langengine.gpt.nl2opensearch.domain.OpenSearchConfig;
import com.alibaba.langengine.gpt.nl2opensearch.domain.OpenSearchReturnModel;
import com.alibaba.langengine.gpt.nl2opensearch.domain.PromptConfig;
import com.aliyun.opensearch.search.SearchParamsBuilder;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Chain for interacting with OpenSearch
 * 用于与OpenSearch搜索引擎交互的链。
 *
 * @author pingkuang.pk
 */
@Data
@Slf4j
public class OpenSearchQueryChain extends Chain {
    private LLMChain queryChain;
    private LLMChain summaryChain;
    private OpenSearchWrapper openSearchWrapper;
    private PromptConfig promptConfig;
    /**
     * 问题输入Key
     */
    public final static String QUESTION_KEY = "question";
    public final static String RETURN_QUERY_PARAM = "returnQueryParam";
    public final static String NEED_SUMMARY_KEY = "needSummary";
    public final static String MOCK_DATA = "mockData";
    /**
     * 回答输出Key
     */
    public final static String OUTPUT_KEY = "output";
    public final static String QUERY_PARAM = "queryParam";
    public final static String RAW_RESULT = "rawResult";


    public OpenSearchQueryChain(LLMChain queryChain, LLMChain summaryChain, OpenSearchWrapper openSearchWrapper,
                                PromptConfig promptConfig) {
        this.queryChain = queryChain;
        this.summaryChain = summaryChain;
        setOpenSearchWrapper(openSearchWrapper);
        this.promptConfig = promptConfig;
    }

    public static OpenSearchQueryChain fromLlmAndConfig(BaseLanguageModel llm, OpenSearchConfig openSearchConfig,
        PromptConfig promptConfig) {
        return fromLlmAndConfig(llm, llm, openSearchConfig, promptConfig);
    }

    public static OpenSearchQueryChain fromLlmAndConfig(BaseLanguageModel queryLlm, BaseLanguageModel summaryLlm,
        OpenSearchConfig openSearchConfig, PromptConfig promptConfig) {
        LLMChain llmQueryChain = new LLMChain();
        llmQueryChain.setLlm(queryLlm);
        llmQueryChain.setPrompt(PromptConstants.QUERY_PROMPT);
        LLMChain llmSummaryChain = new LLMChain();
        llmSummaryChain.setLlm(summaryLlm);
        llmSummaryChain.setPrompt(PromptConstants.SUMMARY_PROMPT);
        OpenSearchWrapper openSearchWrapper = new OpenSearchWrapper(openSearchConfig);
        return new OpenSearchQueryChain(llmQueryChain, llmSummaryChain, openSearchWrapper, promptConfig);
    }

    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext,
        Consumer<String> consumer, Map<String, Object> extraAttributes) {
        Map<String, Object> chainResult = new HashMap<>();
        String question = MapUtils.getString(inputs, QUESTION_KEY);
        Boolean needSummary = MapUtils.getBoolean(inputs, NEED_SUMMARY_KEY, true);
        Boolean returnQueryParam = MapUtils.getBoolean(inputs, RETURN_QUERY_PARAM, false);
        Boolean mockData = MapUtils.getBoolean(inputs, MOCK_DATA, false);
        if (StringUtils.isBlank(question)) {
            return chainResult;
        }

        // 生成OpenSearch Query参数
        Map<String, Object> queryInput = Maps.newHashMap();
        queryInput.put("question", question);
        queryInput.put("date", DateUtils.format(new Date(), "yyyyMMdd"));
        queryInput.put("queryDescText", promptConfig.getQueryDescText());
        queryInput.put("filterDescText", promptConfig.getFilterDescText());
        queryInput.put("sortDescText", promptConfig.getSortDectText());
        queryInput.put("sampleText", promptConfig.getSampleText());
        Map<String, Object> queryAnswerResult = queryChain.run(queryInput, executionContext, null, extraAttributes);
        String queryAnswer = MapUtils.getString(queryAnswerResult, "text", "").trim();
        if (StringUtils.isBlank(queryAnswer)) {
            return chainResult;
        }
        SearchParamsBuilder searchParamsBuilder = openSearchWrapper.queryGen(queryAnswer);
        log.info("searchParams: {}", searchParamsBuilder.toString());
        if (returnQueryParam) {
            chainResult.put(OUTPUT_KEY, searchParamsBuilder.toString());
            return chainResult;
        }

        // 执行OpenSearch检索
        OpenSearchReturnModel returnModel;
        if (mockData) {
            returnModel = openSearchWrapper.getMockData();
        } else {
            returnModel = openSearchWrapper.search(searchParamsBuilder);
        }
        if (returnModel == null || !returnModel.isSuccess()) {
            log.error("opensearch query error: {}",
                returnModel == null ? "未知异常" : JSON.toJSONString(returnModel.getErrors()));
            return chainResult;
        }
        if (!needSummary) {
            chainResult.put(OUTPUT_KEY, JSON.toJSONString(returnModel.getResult()));
            chainResult.put(QUERY_PARAM, searchParamsBuilder.toString());
            return chainResult;
        }

        // 执行summary
        Map<String, Object> summaryInput = Maps.newHashMap();
        summaryInput.put("question", question);
        summaryInput.put("searchResult", JSON.toJSONString(returnModel.getResult()));
        summaryInput.put("character", promptConfig.getCharacter());
        summaryInput.put("fetchFieldsDescText", promptConfig.getFetchFieldsDescText());
        Map<String, Object> summaryResult = summaryChain.run(summaryInput, executionContext, null, extraAttributes);
        chainResult.put(OUTPUT_KEY, MapUtils.getString(summaryResult, "text", "").trim());
        chainResult.put(QUERY_PARAM, searchParamsBuilder.toString());
        chainResult.put(RAW_RESULT, JSON.toJSONString(returnModel.getResult()));
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
}
