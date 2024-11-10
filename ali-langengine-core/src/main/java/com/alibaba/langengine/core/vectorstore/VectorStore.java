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
package com.alibaba.langengine.core.vectorstore;

import com.alibaba.langengine.core.indexes.BaseRetriever;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.indexes.VectorStoreRetriever;
import com.alibaba.langengine.core.util.JacksonUtils;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 向量存储的基类
 *
 * @author xiaoxuan.lp
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property= JacksonUtils.PROPERTY_CLASS_NAME)
public abstract class VectorStore {

    /**
     * 通过嵌入运行更多文档并添加到vectorstore
     *
     * @param texts
     */
    public void addTexts(List<String> texts) {
        List<Document> documents = texts.stream().map(text -> {
            Document document = new Document();
            document.setPageContent(text);
            return document;
        }).collect(Collectors.toList());
        addDocuments(documents);
    }

    /**
     * 通过嵌入运行更多文档并添加到vectorstore
     *
     * @param documents
     */
    public abstract void addDocuments(List<Document> documents);

    /**
     * 返回与查询最相似的文档
     *
     * @param query
     * @param k
     * @return
     */
    public List<Document> similaritySearch(String query, int k) {
        return similaritySearch(query, k, null, null);
    }

    /**
     * 返回与查询最相似的文档
     *
     * @param query
     * @param k
     * @param maxDistanceValue
     * @return
     */
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue) {
        return similaritySearch(query, k, maxDistanceValue, null);
    }

    /**
     * 返回与查询最相似的文档
     *
     * @param query
     * @param k
     * @param type
     * @return
     */
    public List<Document> similaritySearch(String query, int k, Integer type) {
        return similaritySearch(query, k, null, type);
    }

    /**
     * 返回与查询最相似的文档
     *
     * @param query
     * @param k
     * @param maxDistanceValue
     * @param type
     * @return
     */
    public abstract List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type);

    public BaseRetriever asRetriever() {
        VectorStoreRetriever retriever = new VectorStoreRetriever();
        retriever.setVectorStore(this);
        return retriever;
    }
}
