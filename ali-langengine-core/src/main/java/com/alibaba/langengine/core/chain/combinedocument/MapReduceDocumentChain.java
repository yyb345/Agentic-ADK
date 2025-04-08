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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Combining documents by mapping a chain over them, then combining results.
 * 通过在文档上映射链来组合文档，然后组合结果。
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class MapReduceDocumentChain extends BaseCombineDocumentChain {

    /**
     * Chain to apply to each document individually.
     * 链单独应用于每个文档。
     */
    private LLMChain llmChain;

    /**
     * Chain to use to reduce the results of applying `llm_chain` to each doc.
     * This typically either a ReduceDocumentChain or StuffDocumentChain.
     */
    private BaseCombineDocumentChain reduceDocumentChain;

    /**
     * The variable name in the llm_chain to put the documents in.
     * If only one variable in the llm_chain, this need not be provided.
     * llm_chain 中用于放置文档的变量名称。如果 llm_chain 中只有一个变量，则无需提供。
     */
    private String documentVariableName = "context";

    /**
     * Return the results of the map steps in the output.
     * 在输出中返回映射步骤的结果。
     */
    private Boolean returnIntermediateSteps = false;

    @Override
    public void setCallbackManager(BaseCallbackManager callbackManager) {
        super.setCallbackManager(callbackManager);
        if (this.llmChain != null) {
            this.llmChain.setCallbackManager(callbackManager != null ? callbackManager.getChild() : null);
        }
        if (this.reduceDocumentChain != null) {
            this.reduceDocumentChain.setCallbackManager(callbackManager != null ? callbackManager.getChild() : null);
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

    public BaseCombineDocumentChain getCollapseDocumentChain() {
        if(reduceDocumentChain instanceof ReduceDocumentChain) {
            if(((ReduceDocumentChain) reduceDocumentChain).getCollapseDocumentChain() != null) {
                return ((ReduceDocumentChain) reduceDocumentChain).getCollapseDocumentChain();
            } else {
                return ((ReduceDocumentChain) reduceDocumentChain).getCombineDocumentChain();
            }
        } else {
            throw new RuntimeException("reduce_documents_chain` is of type\n" +
                    "{type(self.reduce_documents_chain)} so it does not have\n" +
                    "this attribute.");
        }
    }

    public BaseCombineDocumentChain getCombineDocumentChain() {
        if(reduceDocumentChain instanceof ReduceDocumentChain) {
            return ((ReduceDocumentChain) reduceDocumentChain).getCombineDocumentChain();
        } else {
            throw new RuntimeException("reduce_documents_chain` is of type\n" +
                    "{type(self.reduce_documents_chain)} so it does not have\n" +
                    "this attribute.");
        }
    }

    @Override
    public Map<String, Object> combineDocs(List<Document> docs, String question, Map<String, Object> extraAttributes) {
        List<Map<String, Object>> mapResults = docs.stream().map(doc -> {
            Map<String, Object> inputs = getInputs(doc);
            Map<String, Object> outputs = llmChain.predict(inputs, null, extraAttributes);
            return outputs;
        }).collect(Collectors.toList());

        String questionResultKey = llmChain.getOutputKey();
        List<Document> resultDocs = new ArrayList<>();
        for (Map<String, Object> mapResult : mapResults) {
            if(MapUtils.isNotEmpty(mapResult) && mapResult.get(questionResultKey) != null) {
                Document resultDoc = new Document();
                resultDoc.setPageContent(mapResult.get(questionResultKey).toString());
                resultDocs.add(resultDoc);
            } else {
                log.warn(llmChain.toString() + "llm output is not map");
            }
        }
        Map<String, Object> result = reduceDocumentChain.combineDocs(resultDocs, question, extraAttributes);
        return result;
    }

    private Map<String, Object> getInputs(Document document) {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put(documentVariableName, document.getPageContent());
        return inputs;
    }
}
