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
import com.alibaba.langengine.core.outputs.LLMResult;

/**
 * @author aihe.ah
 * @time 2024/3/13
 * 功能说明：
 * {
 * "generations": [
 * [{
 * "generationInfo": {},
 * "text": "你好，我是测试角色。今天天气不错哦，适合出去走走。"
 * }]
 * ],
 * "llmOutput": {
 * "code": 200,
 * "data": {
 * "requestId": "df3edad7-a6dc-4861-9b05-8451860774fb",
 * "usage": {
 * "userTokens": 4,
 * "inputTokens": 89,
 * "outputTokens": 16
 * },
 * "context": {
 * "modelName": "xingchen-plus",
 * "requestId": "df3edad7-a6dc-4861-9b05-8451860774fb",
 * "modelRequestId": "91c477a7-3bfe-946b-9ca6-665e39f32733"
 * },
 * "choices": [{
 * "stopReason": "stop",
 * "messages": [{
 * "role": "assistant",
 * "finishReason": "stop",
 * "content": "你好，我是测试角色。今天天气不错哦，适合出去走走。"
 * }]
 * }]
 * },
 * "requestId": "df3edad7-a6dc-4861-9b05-8451860774fb",
 * "success": true    * 	}
 * }
 */
public class XingChenTokenConsumptionStrategy implements TokenConsumptionStrategy {
    @Override
    public TokenConsumption calculateTokenConsumption(BaseLanguageModel llm, ExecutionContext executionContext) {
        if (executionContext == null || executionContext.getLlmResult() == null) {
            return null;
        }

        LLMResult llmResult = executionContext.getLlmResult();

        try {
            return Optional.ofNullable(llmResult.getLlmOutput())
                .filter(llmOutput -> llmOutput instanceof Map)
                .map(llmOutput -> (Map<String, Object>)llmOutput)
                .filter(llmOutput -> llmOutput.containsKey("data"))
                .map(llmOutput -> llmOutput.get("data"))
                .filter(data -> data instanceof Map)
                .map(data -> (Map<String, Object>)data)
                .filter(data -> data.containsKey("usage"))
                .map(data -> data.get("usage"))
                .filter(usage -> usage instanceof Map)
                .map(usage -> (Map<String, Object>)usage)
                .map(usage -> {
                    Integer userTokens = getIntegerValue(usage, "userTokens");
                    Integer inputTokens = getIntegerValue(usage, "inputTokens");
                    Integer outputTokens = getIntegerValue(usage, "outputTokens");

                    // 使用新建对象的方式
                    TokenConsumption tokenConsumption = new TokenConsumption();
                    tokenConsumption.setTotalTokens(calculateTotalTokens(userTokens, inputTokens, outputTokens));
                    tokenConsumption.setOutputTokens(outputTokens);
                    tokenConsumption.setInputTokens(inputTokens);
                    tokenConsumption.setModelName(getModelName(llm));
                    return tokenConsumption;
                })
                .orElse(null);
        } catch (Exception e) {
            // 记录错误日志
            // 例如：logger.error("Error occurred while calculating token consumption.", e);
            return null;
        }
    }

    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.getOrDefault(key, 0);
        if (value instanceof Integer) {
            return (Integer)value;
        } else if (value instanceof Long) {
            return ((Long)value).intValue();
        } else {
            return 0;
        }
    }

    private int calculateTotalTokens(Integer userTokens, Integer inputTokens, Integer outputTokens) {
        return (userTokens != null ? userTokens : 0)
            + (inputTokens != null ? inputTokens : 0)
            + (outputTokens != null ? outputTokens : 0);
    }

    private String getModelName(BaseLanguageModel llm) {
        return llm != null ? llm.getLlmFamilyName() : null;
    }
}
