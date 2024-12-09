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

public class AnalyzeDepLibs extends Action {

    private static final String PROMPT = "You are an AI developer, trying to write a program that generates code for users based on their intentions.\n" +
            "\n" +
            "For the user's prompt:\n" +
            "\n" +
            "---\n" +
            "The API is: {prompt}\n" +
            "---\n" +
            "\n" +
            "We decide the generated files are: {filepaths_string}\n" +
            "\n" +
            "Now that we have a file list, we need to understand the shared dependencies they have.\n" +
            "Please list and briefly describe the shared contents between the files we are generating, including exported variables, \n" +
            "data patterns, id names of all DOM elements that javascript functions will use, message names and function names.\n" +
            "Focus only on the names of shared dependencies, do not add any other explanations.";

    public AnalyzeDepLibs(String name, Object context, BaseLanguageModel llm) {
        super(name, context, llm);
    }

    @Override
    public ActionOutput run(List<Message> messages) {
        return null;
    }
}
