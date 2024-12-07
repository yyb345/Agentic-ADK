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
package com.alibaba.langengine.core.chain.llmguard.input;

import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.chain.llmguard.ScannerResult;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sinh Le
 */
class BanTopics implements InputScanner {
    private final BaseLanguageModel llm;

    private final PromptTemplate promptTemplate;

    private final List<String> topics;

    private final double threshold;


    public BanTopics(BaseLanguageModel llmChain, PromptTemplate promptTemplate, double threshold, List<String> topics) {
        this.llm = llmChain;
        this.promptTemplate = promptTemplate;
        this.topics = topics;
        this.threshold = threshold;
    }

    public BanTopics(BaseLanguageModel llmChain, double threshold, String... topics) {
        this(llmChain, PromptConstants.RELEVANT_TO_TOPIC, threshold, Arrays.asList(topics));
    }


    @Override
    public ScannerResult scan(String text, Map<String, Object> extraAttributes) {
        for (String topic : topics) {
            LLMChain llmChain = new LLMChain();
            llmChain.setPrompt(promptTemplate);
            llmChain.setLlm(llm);

            final HashMap<String, Object> inputs = new HashMap<>(1);
            inputs.put("topic", topic);
            inputs.put("query", text);
            final Map<String, Object> output = llmChain.predict(inputs, extraAttributes);
            final String result = (String) output.get(llmChain.getOutputKey());
            System.out.println(result);
            double relevantScore = Double.parseDouble(result.trim());
            if (relevantScore >= threshold) {
                return new ScannerResult(text, false, 1 - relevantScore);
            }
        }


        return new ScannerResult(text, true, 1.0);
    }

    @Override
    public String getName() {
        return "BanTopics";
    }
}
