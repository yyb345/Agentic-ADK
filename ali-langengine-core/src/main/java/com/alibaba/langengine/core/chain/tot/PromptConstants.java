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
package com.alibaba.langengine.core.chain.tot;

import com.alibaba.langengine.core.prompt.impl.PromptTemplate;

import java.util.Arrays;

public class PromptConstants {

    public static final String COT_PROMPT_TEMPLATE_EN = "You are an intelligent agent that is generating one thought at a time in a tree of" +
            " thoughts setting.\n" +
            "\n" +
            "PROBLEM\n" +
            "\n" +
            "{problem_description}\n" +
            "\n" +
            "{thoughts}\n" +
            "\n" +
            "Let's think step by step.";

    public static final String PROPOSE_PROMPT_TEMPLATE_EN = "You are an intelligent agent that is generating thoughts in a tree of\n" +
            "thoughts setting.\n" +
            "\n" +
            "The output should be a markdown code snippet formatted as a JSON list of\n" +
            "strings, including the leading and trailing \"```json\" and \"```\":\n" +
            "\n" +
            "```json\n" +
            "[\n" +
            "    \"<thought-1>\",\n" +
            "    \"<thought-2>\",\n" +
            "    \"<thought-3>\"\n" +
            "]\n" +
            "```\n" +
            "\n" +
            "PROBLEM\n" +
            "\n" +
            "{problem_description}\n" +
            "\n" +
            "{thoughts}";

    public static final PromptTemplate COT_PROMPT_EN = new PromptTemplate(COT_PROMPT_TEMPLATE_EN, Arrays.asList(new String[]{ "problem_description", "thoughts" }));
    public static final PromptTemplate PROPOSE_PROMPT_EN = new PromptTemplate(PROPOSE_PROMPT_TEMPLATE_EN, Arrays.asList(new String[]{ "problem_description", "thoughts" }));
}
