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

/**
 * Action class to conduct research and generate a research report.
 *
 * @author xiaoxuan.lp
 */
public class ConductResearch extends Action {

    private static final String CONDUCT_RESEARCH_PROMPT = "### Reference Information\n" +
            "{content}\n" +
            "\n" +
            "### Requirements\n" +
            "Please provide a detailed research report in response to the following topic: \"{topic}\", using the information provided above. The report must meet the following requirements:\n" +
            "\n" +
            "- Focus on directly addressing the chosen topic.\n" +
            "- Ensure a well-structured and in-depth presentation, incorporating relevant facts and figures where available.\n" +
            "- Present data and findings in an intuitive manner, utilizing feature comparative tables, if applicable.\n" +
            "- The report should have a minimum word count of 2,000 and be formatted with Markdown syntax following APA style guidelines.\n" +
            "- Include all source URLs in APA format at the end of the report.";

    public ConductResearch(String name, Object context, BaseLanguageModel llm) {
        super(name, context, llm);
    }

    @Override
    public ActionOutput run(List<Message> messages) {
        return null;
    }
}
