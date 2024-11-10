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

public class DetailMining extends Action {

    private static final String PROMPT_TEMPLATE = "##TOPIC\n" +
            "{topic}\n" +
            "\n" +
            "##RECORD\n" +
            "{record}\n" +
            "\n" +
            "##Format example\n" +
            "{format_example}\n" +
            "-----\n" +
            "\n" +
            "Task: Refer to the \"##TOPIC\" (discussion objectives) and \"##RECORD\" (discussion records) to further inquire about the details that interest you, within a word limit of 150 words.\n" +
            "Special Note 1: Your intention is solely to ask questions without endorsing or negating any individual's viewpoints.\n" +
            "Special Note 2: This output should only include the topic \"##OUTPUT\". Do not add, remove, or modify the topic. Begin the output with '##OUTPUT', followed by an immediate line break, and then proceed to provide the content in the specified format as outlined in the \"##Format example\" section.\n" +
            "Special Note 3: The output should be in the same language as the input.";

    public static final String FORMAT_EXAMPLE = "\n" +
            "##\n" +
            "\n" +
            "##OUTPUT\n" +
            "...(Please provide the specific details you would like to inquire about here.)\n" +
            "\n" +
            "##\n" +
            "\n" +
            "##";

    public DetailMining(String name, Object context, BaseLanguageModel llm) {
        super(name, context, llm);
    }

    @Override
    public ActionOutput run(List<Message> messages) {
        return null;
    }
}
