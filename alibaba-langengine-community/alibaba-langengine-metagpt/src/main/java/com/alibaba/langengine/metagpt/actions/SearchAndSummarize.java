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

public class SearchAndSummarize extends Action {

    private static final String SEARCH_AND_SUMMARIZE_SYSTEM = "### Requirements\n" +
            "1. Please summarize the latest dialogue based on the reference information (secondary) and dialogue history (primary). Do not include text that is irrelevant to the conversation.\n" +
            "- The context is for reference only. If it is irrelevant to the user's search request history, please reduce its reference and usage.\n" +
            "2. If there are citable links in the context, annotate them in the main text in the format [main text](citation link). If there are none in the context, do not write links.\n" +
            "3. The reply should be graceful, clear, non-repetitive, smoothly written, and of moderate length, in {LANG}.\n" +
            "\n" +
            "### Dialogue History (For example)\n" +
            "A: MLOps competitors\n" +
            "\n" +
            "### Current Question (For example)\n" +
            "A: MLOps competitors\n" +
            "\n" +
            "### Current Reply (For example)\n" +
            "1. Alteryx Designer: <desc> etc. if any\n" +
            "2. Matlab: ditto\n" +
            "3. IBM SPSS Statistics\n" +
            "4. RapidMiner Studio\n" +
            "5. DataRobot AI Platform\n" +
            "6. Databricks Lakehouse Platform\n" +
            "7. Amazon SageMaker\n" +
            "8. Dataiku";

    private static final String SEARCH_AND_SUMMARIZE_PROMPT = "### Reference Information\n" +
            "{CONTEXT}\n" +
            "\n" +
            "### Dialogue History\n" +
            "{QUERY_HISTORY}\n" +
            "{QUERY}\n" +
            "\n" +
            "### Current Question\n" +
            "{QUERY}\n" +
            "\n" +
            "### Current Reply: Based on the information, please write the reply to the Question\n" +
            "\n" +
            "\n";

    private static final String SEARCH_AND_SUMMARIZE_SALES_SYSTEM = "## Requirements\n" +
            "1. Please summarize the latest dialogue based on the reference information (secondary) and dialogue history (primary). Do not include text that is irrelevant to the conversation.\n" +
            "- The context is for reference only. If it is irrelevant to the user's search request history, please reduce its reference and usage.\n" +
            "2. If there are citable links in the context, annotate them in the main text in the format [main text](citation link). If there are none in the context, do not write links.\n" +
            "3. The reply should be graceful, clear, non-repetitive, smoothly written, and of moderate length, in Simplified Chinese.\n" +
            "\n" +
            "# Example\n" +
            "## Reference Information\n" +
            "...\n" +
            "\n" +
            "## Dialogue History\n" +
            "user: Which facial cleanser is good for oily skin?\n" +
            "Salesperson: Hello, for oily skin, it is suggested to choose a product that can deeply cleanse, control oil, and is gentle and skin-friendly. According to customer feedback and market reputation, the following facial cleansers are recommended:...\n" +
            "user: Do you have any by L'Oreal?\n" +
            "> Salesperson: ...\n" +
            "\n" +
            "## Ideal Answer\n" +
            "Yes, I've selected the following for you:\n" +
            "1. L'Oreal Men's Facial Cleanser: Oil control, anti-acne, balance of water and oil, pore purification, effectively against blackheads, deep exfoliation, refuse oil shine. Dense foam, not tight after washing.\n" +
            "2. L'Oreal Age Perfect Hydrating Cleanser: Added with sodium cocoyl glycinate and Centella Asiatica, two effective ingredients, it can deeply cleanse, tighten the skin, gentle and not tight.";

    private static final String SEARCH_AND_SUMMARIZE_SALES_PROMPT = "## Reference Information\n" +
            "{CONTEXT}\n" +
            "\n" +
            "## Dialogue History\n" +
            "{QUERY_HISTORY}\n" +
            "{QUERY}\n" +
            "> {ROLE}: \n";

    private static final String SEARCH_FOOD = "# User Search Request\n" +
            "What are some delicious foods in Xiamen?\n" +
            "\n" +
            "# Requirements\n" +
            "You are a member of a professional butler team and will provide helpful suggestions:\n" +
            "1. Please summarize the user's search request based on the context and avoid including unrelated text.\n" +
            "2. Use [main text](reference link) in markdown format to **naturally annotate** 3-5 textual elements (such as product words or similar text sections) within the main text for easy navigation.\n" +
            "3. The response should be elegant, clear, **without any repetition of text**, smoothly written, and of moderate length.";

    public SearchAndSummarize(String name, Object context, BaseLanguageModel llm) {
        super(name, context, llm);
    }

    @Override
    public ActionOutput run(List<Message> messages) {
        return null;
    }
}
