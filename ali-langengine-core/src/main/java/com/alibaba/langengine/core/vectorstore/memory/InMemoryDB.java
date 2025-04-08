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
package com.alibaba.langengine.core.vectorstore.memory;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingDouble;

/**
 * 内存向量库
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class InMemoryDB extends VectorStore {

    private Embeddings embedding;

    private final CopyOnWriteArrayList<EmbeddingEntity> entities = new CopyOnWriteArrayList<>();

    @Override
    public void addDocuments(List<Document> documents) {
        if(documents == null || documents.size() == 0) {
            return;
        }
        documents = embedding.embedDocument(documents);
        for (Document document : documents) {
            EmbeddingValueEntity embeddingValue = new EmbeddingValueEntity(document.getEmbedding().toArray(new Double[0]));
            // 阮萤发现NPE的bug
            entities.add(new EmbeddingEntity(document.getUniqueId(), embeddingValue, document.getPageContent(),
                    document.getMetadata() != null && document.getMetadata().get("name") != null ? document.getMetadata().get("name").toString() : null));
        }
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        List<String> embeddingStrings = embedding.embedQuery(query, k);
        if (embeddingStrings.size() == 0 || !embeddingStrings.get(0).startsWith("[")) {
            return new ArrayList<>();
        }
        String embeddingString = embeddingStrings.get(0);
        List<String> embeddings = JSON.parseArray(embeddingString, String.class);

        EmbeddingValueEntity referenceEmbedding = EmbeddingValueEntity.from(embeddings.stream()
                .map(embedding -> Double.parseDouble(embedding))
                .collect(Collectors.toList()));

        Comparator<EmbeddingMatch> comparator = comparingDouble(EmbeddingMatch::score);
        PriorityQueue<EmbeddingMatch> matches = new PriorityQueue<>(comparator);

        double minSimilarity = -1;
        if(maxDistanceValue != null) {
            minSimilarity = maxDistanceValue;
        }
        for (EmbeddingEntity entity : entities) {
            double similarity = cosineSimilarity(entity.embeddingValue, referenceEmbedding);
            if (similarity >= minSimilarity) {
                matches.add(new EmbeddingMatch(entity.id, entity.embeddingValue, similarity, entity.content, entity.name));
                if (matches.size() > k) {
                    matches.poll();
                }
            }
        }

        List<EmbeddingMatch> result = new ArrayList<>(matches);
        result.sort(comparingDouble(EmbeddingMatch::score));
        Collections.reverse(result);

        return result.stream().map(e -> {
            Document document = new Document();
            document.setUniqueId(e.embeddingId());
            document.setPageContent(e.content());
            document.setScore(e.score());
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("name", e.name());
            document.setMetadata(metadata);
            return document;
        }).collect(Collectors.toList());
    }

    private float cosineSimilarity(EmbeddingValueEntity first, EmbeddingValueEntity second) {
        float dot = 0.0F;
        float nru = 0.0F;
        float nrv = 0.0F;

        for (int i = 0; i < first.vector().length; ++i) {
            dot += first.vector()[i] * second.vector()[i];
            nru += first.vector()[i] * first.vector()[i];
            nrv += second.vector()[i] * second.vector()[i];
        }

        return dot / (float) (Math.sqrt(nru) * Math.sqrt(nrv));
    }
}
