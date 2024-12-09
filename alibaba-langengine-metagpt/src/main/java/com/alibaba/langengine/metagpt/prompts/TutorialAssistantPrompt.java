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
package com.alibaba.langengine.metagpt.prompts;

public class TutorialAssistantPrompt {

    public static final String COMMON_PROMPT = "You are now a seasoned technical professional in the field of the internet. \n" +
            "We need you to write a technical tutorial with the topic \"{topic}\".";

    public static final String DIRECTORY_PROMPT = COMMON_PROMPT + "Please provide the specific table of contents for this tutorial, strictly following the following requirements:\n" +
            "1. The output must be strictly in the specified language, {language}.\n" +
            "2. Answer strictly in the dictionary format like {{\"title\": \"xxx\", \"directory\": [{{\"dir 1\": [\"sub dir 1\", \"sub dir 2\"]}}, {{\"dir 2\": [\"sub dir 3\", \"sub dir 4\"]}}]}}.\n" +
            "3. The directory should be as specific and sufficient as possible, with a primary and secondary directory.The secondary directory is in the array.\n" +
            "4. Do not have extra spaces or line breaks.\n" +
            "5. Each directory title has practical significance.";

    public static final String CONTENT_PROMPT = COMMON_PROMPT + "Now I will give you the module directory titles for the topic. \n" +
            "Please output the detailed principle content of this title in detail. \n" +
            "If there are code examples, please provide them according to standard code specifications. \n" +
            "Without a code example, it is not necessary.\n" +
            "\n" +
            "The module directory titles for the topic is as follows:\n" +
            "{directory}\n" +
            "\n" +
            "Strictly limit output according to the following requirements:\n" +
            "1. Follow the Markdown syntax format for layout.\n" +
            "2. If there are code examples, they must follow standard syntax specifications, have document annotations, and be displayed in code blocks.\n" +
            "3. The output must be strictly in the specified language, {language}.\n" +
            "4. Do not have redundant output, including concluding remarks.\n" +
            "5. Strict requirement not to output the topic \"{topic}\".";
}
