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
package com.alibaba.langengine.core.chain.combinedocument;

import com.alibaba.langengine.core.callback.BaseCallbackManager;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.prompt.BasePromptTemplate;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Combine documents by doing a first pass and then refining on more documents.
 * 通过执行第一遍合并文档，然后改进更多文档。
 *
 * @author xiaoxuan.lp
 */
@Data
public class RefineDocumentChain extends BaseCombineDocumentChain {

    /**
     * LLM chain to use on initial document.
     * 用于初始文档的 LLM 链。
     */
    private LLMChain initialLlmChain;

    /**
     * LLM chain to use when refining.
     * 精炼时使用的LLM链。
     */
    private LLMChain refineLlmChain;

    /**
     * The variable name in the initial_llm_chain to put the documents in.
     * If only one variable in the initial_llm_chain, this need not be provided.
     */
    private String documentVariableName = "context_str";

    /**
     * The variable name to format the initial response in when refining.
     */
    private String initialResponseName = "existing_answer";

    /**
     * Prompt to use to format each document.
     */
    private BasePromptTemplate documentPrompt;

    /**
     * Return the results of the refine steps in the output.
     * 在输出中返回细化步骤的结果。
     */
    private boolean returnIntermediateSteps = false;

    @Override
    public void setCallbackManager(BaseCallbackManager callbackManager) {
        super.setCallbackManager(callbackManager);
        if (this.initialLlmChain != null) {
            this.initialLlmChain.setCallbackManager(callbackManager != null ? callbackManager.getChild() : null);
        }
        if (this.refineLlmChain != null) {
            this.refineLlmChain.setCallbackManager(callbackManager != null ? callbackManager.getChild() : null);
        }
    }

    @Override
    public List<String> getOutputKeys() {
        List<String> outputKeys = super.getOutputKeys();
        if(returnIntermediateSteps && !outputKeys.contains("intermediate_steps")) {
            outputKeys.add("intermediate_steps");
        }
        return outputKeys;
    }

    @Override
    public Map<String, Object> combineDocs(List<Document> docs, String question, Map<String, Object> extraAttributes) {
        if(docs == null || docs.size() == 0) {
            return null;
        }
        Map<String, Object> initialInputs = constructInitialInputs(docs, question);
        Map<String, Object> initialOutputs = initialLlmChain.predict(initialInputs);
        String res = (String) initialOutputs.get("text");
        List<String> refineSteps = new ArrayList<>();
        refineSteps.add(res);
        if(docs.size() > 1) {
            List<Document> refineDocs = docs.stream().skip(1).collect(Collectors.toList());
            for (Document refineDoc : refineDocs) {
                Map<String, Object> refineInputs = constructRefineInputs(refineDoc, res, question);
                Map<String, Object> refineOutputs =  refineLlmChain.predict(refineInputs);
                res = (String) refineOutputs.get("text");
                refineSteps.add(res);
            }
        }
        return constructResult(refineSteps, res);
    }

    private Map<String, Object> constructInitialInputs(List<Document> documents, String question) {
        String docString = formatDocument(documents.get(0));
        Map<String, Object> inputs = new HashMap<>();
        inputs.put(documentVariableName, docString);
        inputs.put("question", question);
        inputs.put("documents", documents);
        return inputs;
    }

    private Map<String, Object> constructRefineInputs(Document document, String res, String question) {
        String docString = formatDocument(document);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put(documentVariableName, docString);
        inputs.put(initialResponseName, res);
        inputs.put("question", question);
        return inputs;
    }

    public Map<String, Object> constructResult(List<String> refineSteps, String res) {
        Map<String, Object> outputs = new HashMap<>();
        if(returnIntermediateSteps) {
            outputs.put("intermediate_steps", refineSteps);
        }
        outputs.put("text", res);
        return outputs;
    }

    private String formatDocument(Document document) {
        Map<String, Object> baseInfo = new HashMap<>();
        baseInfo.put("page_content", document.getPageContent());
        return documentPrompt.format(baseInfo);
    }
}
