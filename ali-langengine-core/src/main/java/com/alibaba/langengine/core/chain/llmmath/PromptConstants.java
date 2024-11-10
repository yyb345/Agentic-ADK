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
package com.alibaba.langengine.core.chain.llmmath;

import com.alibaba.langengine.core.prompt.impl.PromptTemplate;

import java.util.Arrays;

public class PromptConstants {

    public static final String PROMPT_TEMPLATE = "Translate a math problem into a expression that can be executed using Python's numexpr library. Use the output of running this code to answer the question.\n" +
            "\n" +
            "Question: ${{Question with math problem.}}\n" +
            "```text\n" +
            "${{single line mathematical expression that solves the problem}}\n" +
            "```\n" +
            "...numexpr.evaluate(text)...\n" +
            "```output\n" +
            "${{Output of running the code}}\n" +
            "```\n" +
            "Answer: ${{Answer}}\n" +
            "\n" +
            "Begin.\n" +
            "\n" +
            "Question: What is 37593 * 67?\n" +
            "```text\n" +
            "37593 * 67\n" +
            "```\n" +
            "...numexpr.evaluate(\"37593 * 67\")...\n" +
            "```output\n" +
            "2518731\n" +
            "```\n" +
            "Answer: 2518731\n" +
            "\n" +
            "Question: 37593^(1/5)\n" +
            "```text\n" +
            "37593**(1/5)\n" +
            "```\n" +
            "...numexpr.evaluate(\"37593**(1/5)\")...\n" +
            "```output\n" +
            "8.222831614237718\n" +
            "```\n" +
            "Answer: 8.222831614237718\n" +
            "\n" +
            "Question: {question}\n";

    public static final PromptTemplate PROMPT = new PromptTemplate(PROMPT_TEMPLATE, Arrays.asList(new String[]{ "question" }));

}
