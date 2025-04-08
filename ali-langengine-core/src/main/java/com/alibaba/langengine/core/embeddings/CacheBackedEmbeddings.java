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
package com.alibaba.langengine.core.embeddings;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.storage.BaseStore;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * 用于缓存嵌入模型结果的接口
 *
 * @author xiaoxuan.lp
 */
@Data
public class CacheBackedEmbeddings extends Embeddings {

    private Embeddings underlyingEmbeddings;

    private BaseStore<String, List<Double>> documentEmbeddingStore;

    public CacheBackedEmbeddings(Embeddings underlyingEmbeddings,
                                 BaseStore<String, List<Double>> documentEmbeddingStore) {
        setUnderlyingEmbeddings(underlyingEmbeddings);
        setDocumentEmbeddingStore(documentEmbeddingStore);
    }

    @Override
    public String getModelType() {
        return "0";
    }

    @Override
    public List<Document> embedDocument(List<Document> documents) {
        for (Document document : documents) {
            List<Double> embeddingsValue = documentEmbeddingStore.get(document.getPageContent());
            if(embeddingsValue != null && embeddingsValue.size() > 0) {
                document.setEmbedding(embeddingsValue);
            } else {
                underlyingEmbeddings.embedDocument(Arrays.asList(new Document[] { document }));
                documentEmbeddingStore.set(document.getPageContent(), document.getEmbedding());
            }
        }
        return documents;
    }

    @Override
    public List<String> embedQuery(String text, int recommend) {
        return underlyingEmbeddings.embedQuery(text, recommend);
    }
}
