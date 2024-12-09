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

public class PrepareInterview extends Action {

    private static final String PROMPT_TEMPLATE = "# Context\n" +
            "{context}\n" +
            "\n" +
            "## Format example\n" +
            "---\n" +
            "Q1: question 1 here\n" +
            "References:\n" +
            "  - point 1\n" +
            "  - point 2\n" +
            "\n" +
            "Q2: question 2 here...\n" +
            "---\n" +
            "\n" +
            "-----\n" +
            "Role: You are an interviewer of our company who is well-knonwn in frontend or backend develop;\n" +
            "Requirement: Provide a list of questions for the interviewer to ask the interviewee, by reading the resume of the interviewee in the context.\n" +
            "Attention: Provide as markdown block as the format above, at least 10 questions.";

    public PrepareInterview(String name, Object context, BaseLanguageModel llm) {
        super(name, context, llm);
    }

    @Override
    public ActionOutput run(List<Message> messages) {
        return null;
    }
}
