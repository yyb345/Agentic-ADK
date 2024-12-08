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
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Data
@Slf4j
public class WriterBrainstormSkill extends SemanticKernelSkill {

    private static final String PROMPT = "Must: brainstorm ideas and create a list.\n" +
            "Must: use a numbered list.\n" +
            "Must: only one list.\n" +
            "Must: end list with ##END##\n" +
            "Should: no more than 10 items.\n" +
            "Should: at least 3 items.\n" +
            "Topic: {input}\n" +
            "Start.\n";

    public WriterBrainstormSkill() {
        setName("WriterSkill");
        setFunctionName("Brainstorm");
        setDescription("Given a goal or topic description generate a list of ideas");
        setStructuredSchema(new WriterBrainstormSchema());

        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);
        PromptTemplate promptTemplate = new PromptTemplate(PROMPT, Arrays.asList(new String[] { "input" }));

        LLMChain llmChain = new LLMChain();
        llmChain.setLlm(llm);
        llmChain.setPrompt(promptTemplate);
        setLlmChain(llmChain);

        setStop(Arrays.asList(new String[]{ "##END##" }));
    }

    @Data
    public class WriterBrainstormSchema extends StructuredSchema {

        public WriterBrainstormSchema() {
            StructuredParameter structuredParameter = new StructuredParameter();
            structuredParameter.setName("input");
            structuredParameter.setDescription("A topic description or goal.");
            getParameters().add(structuredParameter);
        }
    }
}
