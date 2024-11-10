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
package com.alibaba.langengine.core.chain.llmbash;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chain.Chain;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.prompt.BasePromptTemplate;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Chain that interprets a prompt and executes bash operations.
 * 解释提示并执行 bash 操作的链。
 *
 * @author xiaoxuan.lp
 */
@Data
public class LLMBashChain extends Chain {

    private LLMChain llmChain;

    private String inputKey = "question";

    private String outputKey = "answer";

    /**
     * bash执行工作目录
     */
    private String workingDirectoryPath;

    private BasePromptTemplate prompt = PROMPT;

    public static final String PROMPT_TEMPLATE = "If someone asks you to perform a task, your job is to come up with a series of bash commands that will perform the task. There is no need to put \"#!/bin/bash\" in your answer. Make sure to reason step by step, using this format:\n" +
            "\n" +
            "Question: \"copy the files in the directory named 'target' into a new directory at the same level as target called 'myNewDirectory'\"\n" +
            "\n" +
            "I need to take the following actions:\n" +
            "- List all files in the directory\n" +
            "- Create a new directory\n" +
            "- Copy the files from the first directory into the second directory\n" +
            "```bash\n" +
            "ls\n" +
            "mkdir myNewDirectory\n" +
            "cp -r target/* myNewDirectory\n" +
            "```\n" +
            "\n" +
            "That is the format. Begin!\n" +
            "\n" +
            "Question: {question}";

    public static final PromptTemplate PROMPT = new PromptTemplate(PROMPT_TEMPLATE, Arrays.asList(new String[]{ "question" }), new BashOutputParser());

    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        String text = llmChain.predictToOutput(inputs, executionContext, extraAttributes);
        BashOutputParser outputParser = (BashOutputParser) llmChain.getPrompt().getOutputParser();
        List<String> commandList = outputParser.parse(text);
        List<String> result = BashProcess.executeCommand(commandList, workingDirectoryPath);
        return Collections.singletonMap(outputKey, "Answer: " + JSON.toJSONString(result));
    }

    @Override
    public List<String> getInputKeys() {
        return Arrays.asList(inputKey);
    }

    @Override
    public List<String> getOutputKeys() {
        return Arrays.asList(outputKey);
    }

    public static LLMBashChain fromLlm(BaseLanguageModel llm) {
        return fromLlm(llm, null);
    }

    public static LLMBashChain fromLlm(BaseLanguageModel llm, BasePromptTemplate prompt) {
        if(prompt == null) {
            prompt = PROMPT;
        }
        LLMChain llmChain = new LLMChain();
        llmChain.setLlm(llm);
        llmChain.setPrompt(prompt);

        LLMBashChain llmBashChain = new LLMBashChain();
        llmBashChain.setLlmChain(llmChain);
        return llmBashChain;
    }
}
