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

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.storage.InMemoryEmbeddingsStore;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CacheBackedEmbeddingsTest {

    @Test
    public void test_embedDocument() {
        // success
        List<String> texts = new ArrayList<>();
        texts.add("阿里巴巴是什么类型的企业？");
        texts.add("淘宝可以做什么？");
        List<Document> documents =  texts.stream().map(text -> {
            Document document = new Document();
            document.setPageContent(text);
            return document;
        }).collect(Collectors.toList());

        FakeEmbeddings mockEmbeddings = new FakeEmbeddings();
        CacheBackedEmbeddings embeddings = new CacheBackedEmbeddings(mockEmbeddings, new InMemoryEmbeddingsStore());

        long start = System.currentTimeMillis();
        List<Document> embededDocuments = embeddings.embedDocument(documents);
        System.out.println(JSON.toJSONString(embededDocuments));
        System.out.println((System.currentTimeMillis() - start) + "ms");

        documents.forEach(document -> document.setEmbedding(null));
        start = System.currentTimeMillis();
        embededDocuments = embeddings.embedDocument(documents);
        System.out.println(JSON.toJSONString(embededDocuments));
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }
}
