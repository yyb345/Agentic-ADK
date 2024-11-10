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

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.outputs.Generation;
import com.alibaba.langengine.core.outputs.LLMResult;

/**
 * @author aihe.ah
 * @time 2024/3/11
 * 功能说明：
 * LLMResult的结果
 * {
 * 	"generations": [
 * 		[{
 * 			"generationInfo": {
 * 				"output_sensitive": false,
 * 				"base_resp": {
 * 					"status_code": 0,
 * 					"status_msg": ""
 *                                },
 * 				"created": 1710139649,
 * 				"usage": {
 * 					"total_tokens": 488
 *                },
 * 				"model": "abab5.5-chat",
 * 				"id": "023ddc00f657d3e7365798e9c50c1e71",
 * 				"choices": [{
 * 					"finish_reason": "stop",
 * 					"messages": [{
 * 						"sender_type": "BOT",
 * 						"sender_name": "AI助手",
 * 						"text": "谈谈你为这个岗位做了哪些准备？"
 *                    }]
 *                }],
 * 				"input_sensitive": false,
 * 				"reply": "谈谈你为这个岗位做了哪些准备？"            * 			},
 * 			"text": "谈谈你为这个岗位做了哪些        "
 * 		}]
 * 	],
 * 	"llmOutput": {
 * 		"$ref": "$.llmResult.generations[0][0].generationI    o"
 * 	}
 * }
 */
public class MinimaxTokenConsumptionStrategy implements TokenConsumptionStrategy{

    @Override
    public TokenConsumption calculateTokenConsumption(BaseLanguageModel llm, ExecutionContext executionContext) {
        LLMResult llmResult = executionContext.getLlmResult();
        if (llmResult == null) {
            return null;
        }
        if (llmResult.getGenerations() == null || llmResult.getGenerations().isEmpty()) {
            return null;
        }
        if (llmResult.getGenerations().get(0) == null || llmResult.getGenerations().get(0).isEmpty()) {
            return null;
        }
        Generation generation = llmResult.getGenerations().get(0).get(0);
        if (generation == null) {
            return null;
        }
        if (generation.getGenerationInfo() == null) {
            return null;
        }
        Map<String, Object> generationInfo = generation.getGenerationInfo();
        if (generationInfo == null) {
            return null;
        }
        if (generationInfo.get("usage") == null) {
            return null;
        }
        Map<String, Object> usage = (Map<String, Object>) generationInfo.get("usage");
        if (usage == null) {
            return null;
        }
        if (usage.get("total_tokens") == null) {
            return null;
        }
        Integer totalTokens = (Integer) usage.get("total_tokens");
        if (totalTokens == null) {
            return null;
        }
        TokenConsumption tokenConsumption = new TokenConsumption();
        tokenConsumption.setTotalTokens(totalTokens);
        tokenConsumption.setModelName(llm.getLlmFamilyName());
        tokenConsumption.setInputTokens(0);
        tokenConsumption.setOutputTokens(0);
        return tokenConsumption;
    }
}
