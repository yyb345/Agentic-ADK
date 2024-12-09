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
package com.alibaba.langengine.metagpt.actions;

import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.metagpt.Message;

import java.util.List;

public class WriteCodeReview extends Action {

    private static final String PROMPT_TEMPLATE = "NOTICE\n" +
            "Role: You are a professional software engineer, and your main task is to review the code. You need to ensure that the code conforms to the PEP8 standards, is elegantly designed and modularized, easy to read and maintain, and is written in Python 3.9 (or in another programming language).\n" +
            "ATTENTION: Use '##' to SPLIT SECTIONS, not '#'. Output format carefully referenced \"Format example\".\n" +
            "\n" +
            "## Code Review: Based on the following context and code, and following the check list, Provide key, clear, concise, and specific code modification suggestions, up to 5.\n" +
            "```\n" +
            "1. Check 0: Is the code implemented as per the requirements?\n" +
            "2. Check 1: Are there any issues with the code logic?\n" +
            "3. Check 2: Does the existing code follow the \"Data structures and interface definitions\"?\n" +
            "4. Check 3: Is there a function in the code that is omitted or not fully implemented that needs to be implemented?\n" +
            "5. Check 4: Does the code have unnecessary or lack dependencies?\n" +
            "```\n" +
            "\n" +
            "## Rewrite Code: {filename} Base on \"Code Review\" and the source code, rewrite code with triple quotes. Do your utmost to optimize THIS SINGLE FILE. \n" +
            "-----\n" +
            "# Context\n" +
            "{context}\n" +
            "\n" +
            "## Code: {filename}\n" +
            "```\n" +
            "{code}\n" +
            "```\n" +
            "-----\n" +
            "\n" +
            "## Format example\n" +
            "-----\n" +
            "{format_example}\n" +
            "-----\n";

    private static final String FORMAT_EXAMPLE = "\n" +
            "## Code Review\n" +
            "1. The code ...\n" +
            "2. ...\n" +
            "3. ...\n" +
            "4. ...\n" +
            "5. ...\n" +
            "\n" +
            "## Rewrite Code: {filename}\n" +
            "```python\n" +
            "## {filename}\n" +
            "...\n" +
            "```";

    public WriteCodeReview(String name, Object context, BaseLanguageModel llm) {
        super(name, context, llm);
    }

    @Override
    public ActionOutput run(List<Message> messages) {
        return null;
    }
}
