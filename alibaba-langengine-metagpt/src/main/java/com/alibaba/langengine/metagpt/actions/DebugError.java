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

public class DebugError extends Action {

    private static final String PROMPT_TEMPLATE = "NOTICE\n" +
            "1. Role: You are a Development Engineer or QA engineer;\n" +
            "2. Task: You received this message from another Development Engineer or QA engineer who ran or tested your code. \n" +
            "Based on the message, first, figure out your own role, i.e. Engineer or QaEngineer,\n" +
            "then rewrite the development code or the test code based on your role, the error, and the summary, such that all bugs are fixed and the code performs well.\n" +
            "Attention: Use '##' to split sections, not '#', and '## <SECTION_NAME>' SHOULD WRITE BEFORE the test case or script and triple quotes.\n" +
            "The message is as follows:\n" +
            "{context}\n" +
            "---\n" +
            "Now you should start rewriting the code:\n" +
            "## file name of the code to rewrite: Write code with triple quoto. Do your best to implement THIS IN ONLY ONE FILE.";

    public DebugError(String name, Object context, BaseLanguageModel llm) {
        super(name, context, llm);
    }

    @Override
    public ActionOutput run(List<Message> messages) {
        return null;
    }
}
