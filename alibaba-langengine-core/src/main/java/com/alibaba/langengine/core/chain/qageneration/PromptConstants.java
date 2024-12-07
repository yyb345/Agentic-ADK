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

import com.alibaba.langengine.core.prompt.impl.PromptTemplate;

public class PromptConstants {

    public static final PromptTemplate QA_GENERATION_PROMPT = new PromptTemplate("You are a smart assistant designed to help high school teachers come up with reading comprehension questions.\n" +
            "Given a piece of text, you must come up with a question and answer pair that can be used to test a student's reading comprehension abilities.\n" +
            "When coming up with this question/answer pair, you must respond in the following format:\n" +
            "```\n" +
            "{\n" +
            "    \"question\": \"$YOUR_QUESTION_HERE\",\n" +
            "    \"answer\": \"$THE_ANSWER_HERE\"\n" +
            "}\n" +
            "```\n" +
            "\n" +
            "Everything between the ``` must be valid json.\n" +
            "\n" +
            "Please come up with a question/answer pair, in the specified JSON format, for the following text:\n" +
            "----------------\n" +
            "{text}");


}
