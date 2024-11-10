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
package com.alibaba.langengine.core.chain.retrievalqa;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chain.Chain;
import com.alibaba.langengine.core.chain.combinedocument.*;
import com.alibaba.langengine.core.outputparser.RegexParser;
import com.alibaba.langengine.core.util.JacksonUtils;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.alibaba.langengine.core.chain.combinedocument.PromptConstants.*;

/**
 * 检索器基类
 *
 * @author xiaoxuan.lp
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property= JacksonUtils.PROPERTY_CLASS_NAME)
public abstract class BaseRetrievalQA extends Chain {
    private BaseCombineDocumentChain combineDocumentsChain;
    private BaseLanguageModel llm;

    private String inputKey = "query";
    private String outputKey = "result";
    private boolean returnSourceDocuments = false;

    /**
     * stuff专用
     */
    private PromptTemplate prompt;

    public void init(boolean isCH) {
        init(isCH, "stuff");
    }

    public void init(boolean isCH, String chainType) {
        PromptTemplate documentPrompt = new PromptTemplate();
        documentPrompt.setTemplate("Context:\n{page_content}");

        if("stuff".equals(chainType)) {
            if(prompt == null) {
                prompt = (isCH ? QA_PROMPT_CH : QA_PROMPT_EN);
            }
            LLMChain llmChain = new LLMChain();
            llmChain.setLlm(llm);
            llmChain.setPrompt(prompt);

            StuffDocumentChain stuffDocumentsChain = new StuffDocumentChain();
            stuffDocumentsChain.setLlmChain(llmChain);
            stuffDocumentsChain.setDocumentVariableName("context");
            stuffDocumentsChain.setDocumentPrompt(documentPrompt);
            if(getCallbackManager() != null) {
                stuffDocumentsChain.setCallbackManager(getCallbackManager());
            }
            combineDocumentsChain = stuffDocumentsChain;
        } else if("refine".equals(chainType)) {
            PromptTemplate questionPrompt = (isCH ? QA_QUESTION_PROMPT_CH : QA_QUESTION_PROMPT_EN);
            LLMChain initialChain = new LLMChain();
            initialChain.setLlm(llm);
            initialChain.setPrompt(questionPrompt);

            PromptTemplate refinePrompt = (isCH ? QA_REFINE_PROMPT_CH : QA_REFINE_PROMPT_EN);
            LLMChain refineChain = new LLMChain();
            refineChain.setLlm(llm);
            refineChain.setPrompt(refinePrompt);

            RefineDocumentChain refineDocumentChain = new RefineDocumentChain();
            refineDocumentChain.setDocumentPrompt(documentPrompt);
            refineDocumentChain.setInitialLlmChain(initialChain);
            refineDocumentChain.setRefineLlmChain(refineChain);
            refineDocumentChain.setDocumentVariableName("context_str");
            refineDocumentChain.setInitialResponseName("existing_answer");
            if(getCallbackManager() != null) {
                refineDocumentChain.setCallbackManager(getCallbackManager());
            }
            combineDocumentsChain = refineDocumentChain;
        } else if("map_reduce".equals(chainType)) {
            PromptTemplate mapPrompt = (isCH ? QA_MAPREDUCE_MAP_PROMPT_CH : QA_MAPREDUCE_MAP_PROMPT_EN);

            LLMChain llmChain = new LLMChain();
            llmChain.setLlm(llm);
            llmChain.setPrompt(mapPrompt);

            PromptTemplate reducePrompt = (isCH ? QA_MAPREDUCE_REDUCE_PROMPT_CH : QA_MAPREDUCE_REDUCE_PROMPT_EN);

            LLMChain reduceLlmChain = new LLMChain();
            reduceLlmChain.setLlm(llm);
            reduceLlmChain.setPrompt(reducePrompt);

            StuffDocumentChain combineDocumentChain = new StuffDocumentChain();
            combineDocumentChain.setLlmChain(reduceLlmChain);
            combineDocumentChain.setDocumentPrompt(documentPrompt);
            combineDocumentChain.setDocumentVariableName("context");

            ReduceDocumentChain reduceDocumentChain = new ReduceDocumentChain();
            reduceDocumentChain.setCombineDocumentChain(combineDocumentChain);

            if(prompt == null) {
                prompt = (isCH ? QA_PROMPT_CH : QA_PROMPT_EN);
            }
            LLMChain qaLlmChain = new LLMChain();
            qaLlmChain.setLlm(llm);
            qaLlmChain.setPrompt(prompt);

            StuffDocumentChain qaDocumentChain = new StuffDocumentChain();
            qaDocumentChain.setLlmChain(qaLlmChain);
            qaDocumentChain.setDocumentVariableName("context");
            qaDocumentChain.setDocumentPrompt(documentPrompt);
            if(getCallbackManager() != null) {
                qaDocumentChain.setCallbackManager(getCallbackManager());
            }

            reduceDocumentChain.setQaDocumentChain(qaDocumentChain);

            MapReduceDocumentChain chain = new MapReduceDocumentChain();
            chain.setLlmChain(llmChain);
            chain.setReduceDocumentChain(reduceDocumentChain);

            combineDocumentsChain = chain;
        } else if("map_rerank".equals(chainType)) {
//            JsonParser jsonParser = new JsonParser();
//            jsonParser.setOutputKeys(Arrays.asList(new String[]{ "answer", "score" }));

            RegexParser regexParser = new RegexParser();
            regexParser.setRegex("(.*?)\nScore: (.*)");
            regexParser.setOutputKeys(Arrays.asList(new String[]{ "answer", "score" }));

            PromptTemplate prompt = (isCH ? QA_MAPRERANK_PROMPT_CH : QA_MAPRERANK_PROMPT_EN);
            prompt.setOutputParser(regexParser);

            LLMChain llmChain = new LLMChain();
            llmChain.setLlm(llm);
            llmChain.setPrompt(prompt);

            MapRerankDocumentChain chain = new MapRerankDocumentChain();
            chain.setLlmChain(llmChain);
            chain.setDocumentVariableName("context");
            chain.setRankKey("score");
            chain.setAnswerKey("answer");

            combineDocumentsChain = chain;
        }

    }

    public void init() {
        init(false);
    }

    @Override
    public List<String> getInputKeys() {
        return null;
    }

    @Override
    public List<String> getOutputKeys() {
        return null;
    }

    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        String question = (String)inputs.get("question");
        List<Document> documents = getDocs(question);
        Map<String, Object> combineInputs = new HashMap<>();
        combineInputs.put("input_documents", documents);
        combineInputs.put("question", question);
        return combineDocumentsChain.call(combineInputs, executionContext, consumer, extraAttributes);
    }

    @Override
    public CompletableFuture<Map<String, Object>> callAsync(Map<String, Object> inputs, ExecutionContext executionContext, Map<String, Object> extraAttributes) {
        return CompletableFuture.supplyAsync(() -> call(inputs, executionContext, null, extraAttributes));
    }

    /**
     * qa获取文档列表
     *
     * @param question
     * @return
     */
    public abstract List<Document> getDocs(String question);
}
