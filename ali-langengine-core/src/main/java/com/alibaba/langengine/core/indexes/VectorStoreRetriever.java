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
package com.alibaba.langengine.core.indexes;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VectorStoreRetriever
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class VectorStoreRetriever extends BaseRetriever {

    private VectorStore vectorStore;

    private String searchType = "similarity";

    @Override
    public List<Document> getRelevantDocuments(String query, int recommendCount, Double maxDistanceValue, ExecutionContext executionContext) {
        log.info("getRelevantDocuments query:" + query + ", recommendCount:" + recommendCount + ", maxDistanceValue:" + maxDistanceValue);
        if (executionContext == null) {
            executionContext = new ExecutionContext();
        }

        try {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("query", query);
            inputs.put("recommendCount", recommendCount);
            inputs.put("maxDistanceValue", maxDistanceValue);
            onRetrieverStart(this, inputs, executionContext);

            List<Document> documents = new ArrayList<>();
            if ("similarity".equals(searchType)) {
                documents = vectorStore.similaritySearch(query, recommendCount, maxDistanceValue);
            }

            onRetrieverEnd(this, inputs, documents, executionContext);

            return documents;
        } catch (Exception e) {
            onRetrieverError(this, e, executionContext);
            throw e;
        }
    }

    /**
     * Add documents to vectorstore.
     *
     * @param documents
     * @return
     */

    public void addDocuments(List<Document> documents) {
        vectorStore.addDocuments(documents);
    }
}
