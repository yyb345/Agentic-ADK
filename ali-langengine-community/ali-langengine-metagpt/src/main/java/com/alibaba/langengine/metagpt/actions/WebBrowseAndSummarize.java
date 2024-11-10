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
 * Action class to explore the web and provide summaries of articles and webpages.
 *
 * @author xiaoxuan.lp
 */
public class WebBrowseAndSummarize extends Action {

    private static final String RESEARCH_TOPIC_SYSTEM = "You are an AI researcher assistant, and your research topic is:\n#TOPIC#\n{topic}";

    private static final String WEB_BROWSE_AND_SUMMARIZE_PROMPT = "### Requirements\n" +
            "1. Utilize the text in the \"Reference Information\" section to respond to the question \"{query}\".\n" +
            "2. If the question cannot be directly answered using the text, but the text is related to the research topic, please provide \n" +
            "a comprehensive summary of the text.\n" +
            "3. If the text is entirely unrelated to the research topic, please reply with a simple text \"Not relevant.\"\n" +
            "4. Include all relevant factual information, numbers, statistics, etc., if available.\n" +
            "\n" +
            "### Reference Information\n" +
            "{content}";

    public WebBrowseAndSummarize(String name, Object context, BaseLanguageModel llm) {
        super(name, context, llm);
    }

    @Override
    public ActionOutput run(List<Message> messages) {
        return null;
    }
}
