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
package com.alibaba.langengine.core.memory.impl;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.indexes.VectorStoreRetriever;
import com.alibaba.langengine.core.memory.BaseMemory;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class for a VectorStore-backed memory object.
 *
 * @author xiaoxuan.lp
 */
@Data
public class VectorStoreRetrieverMemory extends BaseMemory {

    /**
     * VectorStoreRetriever object to connect to.
     */
    private VectorStoreRetriever retriever;

    /**
     * Key name to locate the memories in the result of load_memory_variables.
     */
    private String memoryKey = "history";

    /**
     * Key name to index the inputs to load_memory_variables.
     */
    private String inputKey = "input";

    @Override
    public List<String> memoryVariables() {
        return Arrays.asList(new String[] { memoryKey });
    }

    @Override
    public Map<String, Object> loadMemoryVariables(String sessionId, Map<String, Object> inputs) {
        String query = (String)inputs.get(inputKey);
        List<Document> docs = retriever.getRelevantDocuments(query, retriever.getRecommendCount());
        String result = docs.stream().map(e -> e.getPageContent()).collect(Collectors.joining("\n"));
        Map<String, Object> output = new HashMap<>();
        output.put(memoryKey, result);
        return output;
    }

    @Override
    public void saveContext(String sessionId, Map<String, Object> inputs, Map<String, Object> outputs) {
        List<Document> documents = fromDocuments(inputs, outputs);
        retriever.addDocuments(documents);
    }

    @Override
    public void clear(String sessionId) {

    }

    private List<Document> fromDocuments(Map<String, Object> inputs, Map<String, Object> outputs) {
        List<String> texts = new ArrayList<>();
        for (Map.Entry<String, Object> entry : inputs.entrySet()) {
            if(entry.getKey().equals(memoryKey)) {
                continue;
            }
            texts.add(String.format("%s: %s", entry.getKey(), entry.getValue()));
        }
        for (Map.Entry<String, Object> entry : outputs.entrySet()) {
            texts.add(String.format("%s: %s", entry.getKey(), entry.getValue()));
        }
        String pageContent = texts.stream().collect(Collectors.joining("\n"));

        List<Document> documents = new ArrayList<>();
        Document document = new Document();
        document.setPageContent(pageContent);
        documents.add(document);
        return documents;
    }
}
