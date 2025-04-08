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
package com.alibaba.langengine.core.caches;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.outputs.Generation;

import java.util.List;

/**
 * Base interface for cache.
 *
 * @author xiaoxuan.lp
 */
public abstract class BaseCache {

    /**
     * 获取缓存key
     *
     * @param prompt
     * @param llmString
     * @return
     */
    public String getCacheKey(String prompt, String llmString) {
        return prompt + llmString;
    }

    /**
     * Look up based on prompt and llm_string.
     *
     * @param prompt
     * @param llmString
     * @return
     */
    public abstract List<Generation> get(String prompt, String llmString);

    /**
     * 通过LLM做一些特殊的业务逻辑
     *
     * @param context
     * @param prompt
     * @param llmString
     * @return
     */
    public List<Generation> get(ExecutionContext context, String prompt, String llmString) {
        return null;
    }

    /**
     * Update cache based on prompt and llm_string.
     *
     * @param context
     * @param prompt
     * @param llmString
     * @param returnVal
     */
    public void update(ExecutionContext context, String prompt, String llmString, List<Generation> returnVal) {

    }

    /**
     * Update cache based on prompt and llm_string.
     *
     * @param prompt
     * @param llmString
     * @param returnVal
     */
    public abstract void update(String prompt, String llmString, List<Generation> returnVal);

    /**
     * Clear cache that can take additional keyword arguments.
     */
    public abstract void clear();
}
