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

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;

import lombok.Builder;
import lombok.Data;

/**
 * @author aihe.ah
 * @time 2024/3/11
 * 功能说明：
 */
public interface TokenConsumptionStrategy {

    /**
     * 拿到输入Token，输出Token，总Token
     *
     * @param llm
     * @param executionContext
     * @return
     */
    TokenConsumption calculateTokenConsumption(BaseLanguageModel llm, ExecutionContext executionContext);

    @Data
    public static class TokenConsumption {
        /**
         * 输入Token
         */
        private Integer inputTokens;
        /**
         * 输出Token
         */
        private Integer outputTokens;
        /**
         * 总Token
         */
        private Integer totalTokens;

        /**
         * 具体的模型
         */
        private String modelName;
    }
}
