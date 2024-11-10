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
import com.alibaba.langengine.core.indexes.Document;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Combining documents by recursively reducing them.
 * 通过递归减少文档来组合文档。
 *
 * @author xiaoxuan.lp
 */
@Data
public class ReduceDocumentChain extends BaseCombineDocumentChain {

    /**
     * Final chain to call to combine documents.
     * This is typically a StuffDocumentsChain.
     */
    BaseCombineDocumentChain combineDocumentChain;

    /**
     * Chain to use to collapse documents if needed until they can all fit.
     * If None, will use the combine_documents_chain.
     * This is typically a StuffDocumentsChain.
     */
    BaseCombineDocumentChain collapseDocumentChain;

    /**
     * qa chain
     */
    BaseCombineDocumentChain qaDocumentChain;

    /**
     * The maximum number of tokens to group documents into. For example, if
     * set to 5000 then documents will be grouped into chunks of no greater than
     * 5000 tokens before trying to combine them into a smaller chunk.
     */
    int tokenMax = 5000;

    public BaseCombineDocumentChain getCollapseChain() {
        if(collapseDocumentChain != null) {
            return collapseDocumentChain;
        } else {
            return combineDocumentChain;
        }
    }

    @Override
    public void setCallbackManager(BaseCallbackManager callbackManager) {
        super.setCallbackManager(callbackManager);
        if (this.combineDocumentChain != null) {
            this.combineDocumentChain.setCallbackManager(callbackManager != null ? callbackManager.getChild() : null);
        }
        if (this.collapseDocumentChain != null) {
            this.collapseDocumentChain.setCallbackManager(callbackManager != null ? callbackManager.getChild() : null);
        }
        if (this.qaDocumentChain != null) {
            this.qaDocumentChain.setCallbackManager(callbackManager != null ? callbackManager.getChild() : null);
        }
    }

    @Override
    public Map<String, Object> combineDocs(List<Document> docs, String question, Map<String, Object> extraAttributes) {
        List<Document> resultDocs = collapse(docs, question, extraAttributes);
        Map<String, Object> outputs = getCombineDocumentChain().combineDocs(resultDocs, question, extraAttributes);
        if(StringUtils.isEmpty(question)) {
            return outputs;
        }
        //针对reduce之后的上下文进行最后的问答
        List<Document> qaDocuments = new ArrayList<>();
        Document qaDocument = new Document();
        qaDocument.setPageContent(outputs.get("text").toString());
        qaDocuments.add(qaDocument);
        return getQaDocumentChain().combineDocs(qaDocuments, question, extraAttributes);
    }

    private List<Document> collapse(List<Document> docs, String question, Map<String, Object> extraAttributes) {
        List<Document> resultDocs = docs;
        if(combineDocumentChain instanceof StuffDocumentChain) {
            StuffDocumentChain stuffDocumentChain = (StuffDocumentChain) combineDocumentChain;
            int numTokens = stuffDocumentChain.getPromptLength(docs);

            int tokenMax = getTokenMax();
            while(numTokens > tokenMax) {
                List<List<Document>> newResultDocList = splitListOfDocs(resultDocs, stuffDocumentChain);

                resultDocs = new ArrayList<>();
                for (List<Document> newResultDoc : newResultDocList) {
                    Document newDoc = collapseDocs(newResultDoc, question, extraAttributes);
                    resultDocs.add(newDoc);
                }
                numTokens = stuffDocumentChain.getPromptLength(resultDocs);
            }
            return resultDocs;
        }
        return resultDocs;
    }

    public Document collapseDocs(List<Document> docs, String question, Map<String, Object> extraAttributes) {
        Map<String, Object> outputs = getCollapseChain().combineDocs(docs, question, extraAttributes);
        String text = (String) outputs.get("text");
        Document document = new Document();
        document.setPageContent(text);
        return document;
    }

    private List<List<Document>> splitListOfDocs(List<Document> docs, StuffDocumentChain stuffDocumentChain) {
        List<List<Document>> newResultDocList = new ArrayList<>();

        List<Document> subResultDocs = new ArrayList<>();
        for (Document doc : docs) {
            subResultDocs.add(doc);
            int numTokens = stuffDocumentChain.getPromptLength(subResultDocs);
            if(numTokens > tokenMax) {
                if(subResultDocs.size() == 1) {
                    throw new RuntimeException("A single document was longer than the context length, we cannot handle this.");
                }
                newResultDocList.add(subResultDocs.stream().limit(subResultDocs.size() - 1).collect(Collectors.toList())) ;
                subResultDocs = subResultDocs.stream().skip(1).collect(Collectors.toList());
            }
        }
        newResultDocList.add(subResultDocs);
        return newResultDocList;
    }
}
