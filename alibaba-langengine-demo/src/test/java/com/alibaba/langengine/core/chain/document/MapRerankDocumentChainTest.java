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
package com.alibaba.langengine.core.chain.document;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.chain.combinedocument.MapRerankDocumentChain;
import com.alibaba.langengine.core.chain.combinedocument.PromptConstants;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.outputparser.RegexParser;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.alibaba.langengine.core.chain.document.Constants.OPEN_DEMO_CONTENT;
import static com.alibaba.langengine.core.chain.document.Constants.OPEN_INFO_CONTENT;

public class MapRerankDocumentChainTest {

    @Test
    public void test_run() {
        // success
        PromptTemplate documentPrompt = new PromptTemplate();
        documentPrompt.setInputVariables(Arrays.asList(new String[]{ "page_content" }));
        documentPrompt.setTemplate("{page_content}");

        String documentVariableName = "context";

        ChatOpenAI llm = new ChatOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        RegexParser regexParser = new RegexParser();
        regexParser.setRegex("(.*?)\nScore: (.*)");
        regexParser.setOutputKeys(Arrays.asList(new String[]{ "answer", "score" }));

        PromptTemplate prompt = PromptConstants.QA_MAPRERANK_PROMPT_EN;
        prompt.setOutputParser(regexParser);

        LLMChain llmChain = new LLMChain();
        llmChain.setLlm(llm);
        llmChain.setPrompt(prompt);

        MapRerankDocumentChain chain = new MapRerankDocumentChain();
        chain.setLlmChain(llmChain);
        chain.setDocumentVariableName(documentVariableName);
        chain.setRankKey("score");
        chain.setAnswerKey("answer");

        List<Document> documents = new ArrayList<>();
        Document document = new Document();
        document.setPageContent(OPEN_INFO_CONTENT);
        documents.add(document);
        document = new Document();
        document.setPageContent(OPEN_DEMO_CONTENT);
        documents.add(document);

        Map<String, Object> response = chain.combineDocs(documents, "什么是开放平台", null);
        System.out.println("response:" + JSON.toJSONString(response));
    }
}
