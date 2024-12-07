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
package com.alibaba.langengine.core.chain.router;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import lombok.Data;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Class that uses embeddings to route between options.
 * 使用嵌入在选项之间路由的类。
 *
 * @author xiaoxuan.lp
 */
@Data
public class EmbeddingRouterChain extends RouterChain {

    private VectorStore vectorStore;

    private List<String> routingKeys = Arrays.asList(new String[]{ "query" });

    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        List<String> _input = new ArrayList<>();
        for (String routingKey : routingKeys) {
            if(inputs.containsKey(routingKey)) {
                _input.add(inputs.get(routingKey).toString());
            }
        }
        String query = _input.stream().collect(Collectors.joining(", "));
        List<Document> results = vectorStore.similaritySearch(query, 1);
        Map<String, Object> result = new HashMap<>();
        result.put("next_inputs", inputs);
        if(results.get(0).getMetadata() == null) {
            throw new RuntimeException("metadata is null");
        }
        if(!results.get(0).getMetadata().containsKey("name")) {
            throw new RuntimeException("metadata's name is null");
        }
        result.put("destination", results.get(0).getMetadata().get("name"));
        return result;
    }

    @Override
    public List<String> getInputKeys() {
        return routingKeys;
    }

    public static EmbeddingRouterChain fromDocuments(List<Document> documents,
                                                     VectorStore vectorStore) {
        Map<String, List<String>> namesAndDescriptions = new HashMap<>();
        documents.stream().forEach(document -> {
            namesAndDescriptions.put(document.getMetadata().get("name").toString(), Arrays.asList(new String[]{ document.getPageContent() }));
        });
        return fromNamesAndDescriptions(namesAndDescriptions, vectorStore, Arrays.asList(new String[]{ "input" }));
    }

    public static EmbeddingRouterChain fromNamesAndDescriptions(Map<String, List<String>> namesAndDescriptions,
                                                                VectorStore vectorStore,
                                                                List<String> routingKeys) {
        List<Document> documents = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : namesAndDescriptions.entrySet()) {
            String name = entry.getKey();
            List<String> descriptions = entry.getValue();
            for (String description : descriptions) {
                Document document = new Document();
                document.setPageContent(description);
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("name", name);
                document.setMetadata(metadata);
                documents.add(document);
            }
        }
        vectorStore.addDocuments(documents);

        EmbeddingRouterChain embeddingRouterChain = new EmbeddingRouterChain();
        embeddingRouterChain.setVectorStore(vectorStore);
        embeddingRouterChain.setRoutingKeys(routingKeys);
        return embeddingRouterChain;
    }
}
