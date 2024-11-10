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
package com.alibaba.langengine.core.chain.qageneration;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chain.Chain;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.outputs.LLMResult;
import com.alibaba.langengine.core.prompt.BasePromptTemplate;
import com.alibaba.langengine.core.textsplitter.RecursiveCharacterTextSplitter;
import com.alibaba.langengine.core.textsplitter.TextSplitter;
import com.alibaba.langengine.core.util.JacksonUtils;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Base class for question-answer generation chains.
 *
 * @author Sinh Le
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = JacksonUtils.PROPERTY_CLASS_NAME)
public class QAGenerationChain extends Chain {
    private LLMChain llmChain;
    private TextSplitter textSplitter;
    private String inputKey = "text";
    private String outputKey = "questions";
    private Integer k;

    public QAGenerationChain(LLMChain llmChain, TextSplitter textSplitter, Integer k) {
        this.llmChain = llmChain;
        if (textSplitter == null) {
            this.textSplitter = new RecursiveCharacterTextSplitter();
            this.textSplitter.setMaxChunkOverlap(400);
        } else {
            this.textSplitter = textSplitter;
        }
        this.k = k;
    }

    public static QAGenerationChain init(BaseLanguageModel llm, BasePromptTemplate prompt) {
        BasePromptTemplate _prompt = prompt != null ? prompt : PromptConstants.QA_GENERATION_PROMPT;
        LLMChain chain = new LLMChain();
        chain.setLlm(llm);
        chain.setPrompt(_prompt);
        return new QAGenerationChain(chain, null, null);
    }

    public void setTextSplitter(TextSplitter textSplitter) {
        this.textSplitter = textSplitter;
    }

    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        List<Document> documents = textSplitter.createDocuments(Arrays.asList(new String[]{(String) inputs.get(inputKey)}), new ArrayList<>());
        List<Map<String, Object>> pageContents = documents.stream()
                .map(it -> {
                            HashMap<String, Object> map = new HashMap<>(1);
                            map.put("text", it.getPageContent());
                            return map;
                        }
                ).collect(Collectors.toList());
        LLMResult generate = llmChain.generate(pageContents, executionContext, consumer, extraAttributes);
        final List<Map<String, Object>> output = generate.getGenerations().stream()
                .map(it -> it.get(0).getText()).map(it -> processLlmResult(it))
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>(1);
        result.put(outputKey, output);
        return result;

    }


    public static Map<String, Object> processLlmResult(String llmOutput) {
        llmOutput = llmOutput.trim();
        Pattern pattern = Pattern.compile("\\{[^{}]*\\}");
        Matcher textMatch = pattern.matcher(llmOutput);
        if (textMatch.find()) {
            String expression = textMatch.group(0);
            return (Map<String, Object>) JSON.parse(expression);
        } else if (llmOutput.startsWith("{")) {
            return (Map<String, Object>) JSON.parse(llmOutput);
        } else {
            throw new IllegalArgumentException("unknown format from LLM: " + llmOutput);
        }
    }
    @Override
    public List<String> getInputKeys() {
        return Collections.singletonList(inputKey);
    }

    @Override
    public List<String> getOutputKeys() {
        return Collections.singletonList(outputKey);
    }
}
