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
package com.alibaba.langengine.demo.agent.skills;

import com.alibaba.langengine.core.agent.semantickernel.skill.SemanticKernelSkill;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.core.tool.StructuredParameter;
import com.alibaba.langengine.core.tool.StructuredSchema;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Data
@Slf4j
public class ShakespeareSkill extends SemanticKernelSkill {

    private static final String PROMPT = "{input}\n" +
            "\n" +
            "    Rewrite the above in the style of Shakespeare.\n";

    public ShakespeareSkill() {
        setName("ShakespeareSkill");
        setFunctionName("shakespeare");
        setDescription("Generic function, unknown purpose");
        setStructuredSchema(new ShakespeareSchema());

        ChatModelOpenAI llm = new ChatModelOpenAI();
        PromptTemplate promptTemplate = new PromptTemplate(PROMPT, Arrays.asList(new String[] { "input" }));

        LLMChain llmChain = new LLMChain();
        llmChain.setLlm(llm);
        llmChain.setPrompt(promptTemplate);
        setLlmChain(llmChain);
    }

    @Data
    public class ShakespeareSchema extends StructuredSchema {

        public ShakespeareSchema() {
            StructuredParameter structuredParameter = new StructuredParameter();
            structuredParameter.setName("input");
            structuredParameter.setDescription("");
            getParameters().add(structuredParameter);
        }
    }
}
