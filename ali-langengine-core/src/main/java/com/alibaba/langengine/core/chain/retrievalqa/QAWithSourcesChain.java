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
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.chain.combinedocument.BaseCombineDocumentChain;
import com.alibaba.langengine.core.chain.combinedocument.MapReduceDocumentChain;
import com.alibaba.langengine.core.chain.combinedocument.StuffDocumentChain;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.prompt.BasePromptTemplate;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.alibaba.langengine.core.chain.retrievalqa.PromptConstants.COMBINE_PROMPT_TEMPLATE;
import static com.alibaba.langengine.core.chain.retrievalqa.PromptConstants.QUESTION_PROMPT_TEMPLATE;

/**
 * Question answering with sources over documents.
 *
 * @author xiaoxuan.lp
 */
@Data
public class QAWithSourcesChain extends Chain {

    /**
     * Chain to use to combine documents.
     */
    private BaseCombineDocumentChain combineDocumentChain;

    private String questionKey = "question";
    private String inputDocsKey = "docs";
    private String answerKey = "answer";
    private String sourcesAnswerKey = "sources";

    /**
     * Return the source documents.
     */
    private Boolean returnSourceDocuments = false;

    public static QAWithSourcesChain fromLlm(BaseLanguageModel llm,
                                             BasePromptTemplate documentPrompt,
                                             BasePromptTemplate questionPrompt,
                                             BasePromptTemplate combinePrompt) {
        if(documentPrompt == null) {
            PromptTemplate promptTemplate = new PromptTemplate();
            promptTemplate.setTemplate("Context:\n{page_content}");
            documentPrompt = promptTemplate;
        }
        if(questionPrompt == null) {
            PromptTemplate promptTemplate = new PromptTemplate();
            promptTemplate.setTemplate(QUESTION_PROMPT_TEMPLATE);
            promptTemplate.setInputVariables(Arrays.asList(new String[]{ "context", "question" }));
            questionPrompt = promptTemplate;
        }
        if(combinePrompt == null) {
            PromptTemplate promptTemplate = new PromptTemplate();
            promptTemplate.setTemplate(COMBINE_PROMPT_TEMPLATE);
            promptTemplate.setInputVariables(Arrays.asList(new String[]{ "summaries", "question" }));
            combinePrompt = promptTemplate;
        }

        LLMChain llmQuestionChain = new LLMChain();
        llmQuestionChain.setLlm(llm);
        llmQuestionChain.setPrompt(questionPrompt);

        LLMChain llmCombineChain = new LLMChain();
        llmCombineChain.setLlm(llm);
        llmCombineChain.setPrompt(combinePrompt);

        StuffDocumentChain combineResultsChain = new StuffDocumentChain();
        combineResultsChain.setLlmChain(llmCombineChain);
        combineResultsChain.setDocumentPrompt(documentPrompt);
        combineResultsChain.setDocumentVariableName("summaries");

        MapReduceDocumentChain combineDocumentChain = new MapReduceDocumentChain();

//        combineDocumentChain
//                llm_chain=llm_question_chain,
//                combine_document_chain=combine_results_chain,
//                document_variable_name="context",
//                )

        QAWithSourcesChain qaWithSourcesChain = new QAWithSourcesChain();
        qaWithSourcesChain.setCombineDocumentChain(combineDocumentChain);
        return qaWithSourcesChain;
    }

    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return null;
    }

    @Override
    public List<String> getInputKeys() {
        return null;
    }

    @Override
    public List<String> getOutputKeys() {
        return null;
    }
}
