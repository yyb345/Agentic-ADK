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
package com.alibaba.langengine.core.prompt;

import com.alibaba.langengine.core.prompt.impl.FewShotPromptTemplate;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import org.junit.jupiter.api.Test;

import java.util.*;

public class FewShotPromptTemplateTest {

    @Test
    public void test_format() {
        // success
        List<Map<String, Object>> examples = new ArrayList<>();
        Map<String, Object> example = new HashMap<>();
        example.put("word", "happy");
        example.put("antonym", "sad");
        examples.add(example);

        example = new HashMap<>();
        example.put("word", "tall");
        example.put("antonym", "short");
        examples.add(example);

        String template = "Word: {word}\nAntonym: {antonym}";

        PromptTemplate promptTemplate = new PromptTemplate();
        promptTemplate.setTemplate(template);

        FewShotPromptTemplate fewShotPromptTemplate = new FewShotPromptTemplate();
        fewShotPromptTemplate.setExamples(examples);
        fewShotPromptTemplate.setExamplePrompt(promptTemplate);
        fewShotPromptTemplate.setPrefix("Give the antonym of every input");
        fewShotPromptTemplate.setSuffix("Word: {input}\nAntonym:");
        fewShotPromptTemplate.setExampleSeparator("\n\n");

        Map<String, Object> context = new HashMap<>();
        context.put("input", "big");
        String prompt = fewShotPromptTemplate.format(context);
        System.out.println(prompt);
    }
}
