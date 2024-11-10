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
package com.alibaba.langengine.core.chain.tot.strategy;

import com.alibaba.langengine.core.chain.tot.JSONListOutputParser;
import com.alibaba.langengine.core.chain.tot.PromptConstants;
import com.alibaba.langengine.core.prompt.BasePromptTemplate;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.langengine.core.chain.tot.PromptConstants.COT_PROMPT_EN;

/**
 * Sample thoughts from a Chain-of-Thought (CoT) prompt.
 * This strategy works better when the thought space is rich,
 * such as when each thought is a paragraph. Independent and identically distributed samples lead to diversity,
 * which helps to avoid repetition.
 *
 * @author xiaoxuan.lp
 */
@Data
public class SampleCoTStrategy extends BaseThoughtGenerationStrategy {

    public SampleCoTStrategy() {
        setPrompt(COT_PROMPT_EN);
    }

    @Override
    public String nextThought(String problemDescription, List<String> thoughtsPath, Map<String, Object> extraAttributes) {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("problem_description", problemDescription);
        inputs.put("thoughts", getThoughtPrompt(thoughtsPath));
        Object responseText = predictAndParse(inputs, extraAttributes);
        if(responseText instanceof Map) {
            Map<String, Object> responseMap = (Map)responseText;
            return responseMap.get(getOutputKey()).toString();
        }
        return (responseText instanceof String) ? responseText.toString() : "";
    }

    private String getThoughtPrompt(List<String> thoughtsPath) {
        if(thoughtsPath == null || thoughtsPath.size() == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("THOUGHTS\n\n");
        thoughtsPath.stream().forEach(thought -> builder.append(thought + "\n"));
        return builder.toString();
    }
}
