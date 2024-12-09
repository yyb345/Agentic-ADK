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
 * Action class to collect links from a search engine.
 *
 * @author xiaoxuan.lp
 */
public class CollectLinks extends Action {
    
    private static final String SEARCH_TOPIC_PROMPT = "Please provide up to 2 necessary keywords related to your research topic for Google search. Your response must be in JSON format, for example: [\"keyword1\", \"keyword2\"].";

    private static final String SUMMARIZE_SEARCH_PROMPT = "### Requirements\n" +
            "1. The keywords related to your research topic and the search results are shown in the \"Search Result Information\" section.\n" +
            "2. Provide up to {decomposition_nums} queries related to your research topic base on the search results.\n" +
            "3. Please respond in the following JSON format: [\"query1\", \"query2\", \"query3\", ...].\n" +
            "\n" +
            "### Search Result Information\n" +
            "{search_results}";

    private static final String COLLECT_AND_RANKURLS_PROMPT = "### Topic\n" +
            "{topic}\n" +
            "### Query\n" +
            "{query}\n" +
            "\n" +
            "### The online search results\n" +
            "{results}\n" +
            "\n" +
            "### Requirements\n" +
            "Please remove irrelevant search results that are not related to the query or topic. Then, sort the remaining search results \\\n" +
            "based on the link credibility. If two results have equal credibility, prioritize them based on the relevance. Provide the\n" +
            "ranked results' indices in JSON format, like [0, 1, 3, 4, ...], without including other words.";

    public CollectLinks(String name, Object context, BaseLanguageModel llm) {
        super(name, context, llm);
    }

    @Override
    public ActionOutput run(List<Message> messages) {
        return null;
    }
}
