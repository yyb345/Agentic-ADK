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
package com.alibaba.langengine.core.prompt.impl;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * 包含少量样本示例的提示模板
 *
 * @author xiaoxuan.lp
 */
@Data
public class FewShotPromptTemplate extends StringPromptTemplate {

    private List<Map<String, Object>> examples;

    private PromptTemplate examplePrompt;

    private String suffix;

    private String prefix;

    private String exampleSeparator = "\n\n";

    @Override
    public String format(Map<String, Object> args) {
        if(examples != null) {
            List<String> exampleStrings = examples.stream()
                    .map(example -> examplePrompt.format(example)).collect(Collectors.toList());

            List<String> pieces = new ArrayList<>();
            pieces.add(prefix);
            pieces.addAll(exampleStrings);
            pieces.add(suffix);

            String prompt = pieces.stream().collect(Collectors.joining(exampleSeparator));

            if (args == null) {
                return prompt;
            }
            String realTemplate = prompt;
            for (Map.Entry<String, Object> entry : args.entrySet()) {
                if (entry.getValue() instanceof String) {
                    realTemplate = realTemplate.replaceAll("\\{" + entry.getKey() + "\\}", Matcher.quoteReplacement(entry.getValue().toString()));
                }
                return realTemplate;
            }
        }
        return null;
    }

    @Override
    public String getPromptType() {
        return "few_shot";
    }
}
