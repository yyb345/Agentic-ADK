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
package com.alibaba.langengine.core.runnables.tools;

import com.alibaba.langengine.core.agent.semantickernel.skill.SemanticKernelSkill;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.core.tool.StructuredParameter;
import com.alibaba.langengine.core.tool.StructuredSchema;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
@Data
public class CharTranslateTool extends SemanticKernelSkill {

    private static final String PROMPT = "Translate the input below into {language}\n" +
            "\n" +
            "MAKE SURE YOU ONLY USE {language}.\n" +
            "\n" +
            "{input}\n" +
            "\n" +
            "Translation:\n";

    public CharTranslateTool() {
        setName("google_chat_translate");
        setDescription("Google translate the input into a language of your choice");
        setStructuredSchema(new WriterTranslateSchema());

        ChatModelOpenAI llm = new ChatModelOpenAI();
        PromptTemplate promptTemplate = new PromptTemplate(PROMPT, Arrays.asList(new String[] { "language", "input" }));

        LLMChain llmChain = new LLMChain();
        llmChain.setLlm(llm);
        llmChain.setPrompt(promptTemplate);
        setLlmChain(llmChain);

        setStop(Arrays.asList(new String[]{ "[done]" }));
    }

    @Data
    public class WriterTranslateSchema extends StructuredSchema {

        public WriterTranslateSchema() {
            StructuredParameter structuredParameter = new StructuredParameter();
            structuredParameter.setName("input");
            structuredParameter.setDescription("A topic description or goal.");
            getParameters().add(structuredParameter);

            structuredParameter = new StructuredParameter();
            structuredParameter.setName("language");
            structuredParameter.setDescription("A language.");
            getParameters().add(structuredParameter);
        }
    }
}
