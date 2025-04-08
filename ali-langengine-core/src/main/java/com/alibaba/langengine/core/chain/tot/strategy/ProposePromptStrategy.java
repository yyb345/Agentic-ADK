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
import com.alibaba.langengine.core.chain.tot.ToTDFSMemory;
import com.alibaba.langengine.core.prompt.BasePromptTemplate;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.alibaba.langengine.core.chain.tot.PromptConstants.PROPOSE_PROMPT_EN;

/**
 * Propose thoughts sequentially using a "propose prompt".
 * This strategy works better when the thought space is more constrained,
 * such as when each thought is just a word or a line.
 * Proposing different thoughts in the same prompt completion helps to avoid duplication.
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class ProposePromptStrategy extends BaseThoughtGenerationStrategy {

    private Map<String, Stack<String>> totMemory = new HashMap<>();

    public ProposePromptStrategy() {
        setPrompt(PROPOSE_PROMPT_EN);
        getPrompt().setOutputParser(new JSONListOutputParser());
    }

    @Override
    public String nextThought(String problemDescription, List<String> thoughtsPath, Map<String, Object> extraAttributes) {
        String key = String.join("$", thoughtsPath);
        if (!totMemory.containsKey(key) || totMemory.get(key).isEmpty()) {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("problem_description", problemDescription);
            inputs.put("thoughts", getThoughtPrompt(thoughtsPath));
            inputs.put("n", getC());
            Object newThoughts = predictAndParse(inputs, extraAttributes);
            if (newThoughts == null) {
                return "";
            }
            log.warn("newThoughts:" + newThoughts);
            if (newThoughts instanceof List) {
                Stack<String> stack = new Stack<>();
                List<String> newThoughtsList = (List<String>)newThoughts;
                Collections.reverse(newThoughtsList);
                stack.addAll(newThoughtsList);
                totMemory.put(key, stack);
            } else {
                return "";
            }
        }
        String nextThought = totMemory.get(key).pop();
        log.warn("nextThought:" + nextThought);
        return nextThought;
    }

    private String getThoughtPrompt(List<String> thoughtsPath) {
        StringBuilder builder = new StringBuilder();
        if(thoughtsPath == null || thoughtsPath.size() == 0) {
            builder.append("\n\n");
            builder.append("Possible next " + getC() + " valid thoughts based on the PROBLEM:\n");
            return builder.toString();
        }
        builder.append("VALID THOUGHTS\n\n");
        thoughtsPath.stream().forEach(thought -> builder.append(thought + "\n\n"));
        builder.append("\n");
        builder.append("Possible next " + getC() + " valid thoughts based on the last valid thought:\n");
        return builder.toString();
    }
}
