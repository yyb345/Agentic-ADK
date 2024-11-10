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
package com.alibaba.langengine.core.callback.api;

import java.util.Map;
import java.util.Optional;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.outputs.Generation;
import com.alibaba.langengine.core.outputs.LLMResult;

/**
 * @author aihe.ah
 * @time 2024/3/11
 * 功能说明：
 * {
 * "generations": [
 * [{
 * "generationInfo": {
 * "output": {
 * "finish_reason": "stop",
 * "text": "嘿，朋友，你在忙什么呢？希望你一天过得不错，有什么新鲜事儿想分享一下吗？"
 * },
 * "usage": {
 * "total_tokens": 136,
 * "output_tokens": 22,
 * "input_tokens": 114
 * },
 * "request_id": "e196681e-2918-9f6a-a2dc-c01e50043664"            * 			},
 * "text": "嘿，朋友，你在忙什么呢？希望你一天过得不错，有什么新鲜事儿想分享一        "
 * }]
 * ],
 * "llmOutput": {
 * "$ref": "$.llmResult.generations[0][0].generationI    o"
 * }
 * }
 */
public class DashScopeTokenConsumptionStrategy implements TokenConsumptionStrategy {
    @Override
    public TokenConsumption calculateTokenConsumption(BaseLanguageModel llm, ExecutionContext executionContext) {

        return Optional.ofNullable(executionContext.getLlmResult())
            .map(LLMResult::getGenerations)
            .filter(generations -> !generations.isEmpty())
            .map(generations -> generations.get(0))
            .filter(generation -> !generation.isEmpty())
            .map(generation -> generation.get(0))
            .map(Generation::getGenerationInfo)
            .map(generationInfo -> {
                Map<String, Object> usage = (Map<String, Object>)generationInfo.get("usage");
                Integer totalTokens = (Integer)usage.get("total_tokens");
                Integer outputTokens = (Integer)usage.get("output_tokens");
                Integer inputTokens = (Integer)usage.get("input_tokens");

                // 使用新建对象的方式
                TokenConsumption tokenConsumption = new TokenConsumption();
                tokenConsumption.setTotalTokens(totalTokens);
                tokenConsumption.setOutputTokens(outputTokens);
                tokenConsumption.setInputTokens(inputTokens);
                tokenConsumption.setModelName(llm.getLlmFamilyName());
                return tokenConsumption;
            })
            .orElse(null);
    }
}
