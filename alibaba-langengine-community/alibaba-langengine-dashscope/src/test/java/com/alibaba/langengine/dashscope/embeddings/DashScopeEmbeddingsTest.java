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
package com.alibaba.langengine.dashscope.embeddings;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.dashscope.embeddings.embedding.DashScopeConstant;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DashScopeEmbeddingsTest {

    @Test
    public void test_embedDocument() {
        // success
        List<String> texts = new ArrayList<>();
        texts.add("什么是阿里巴巴");
        List<Document> documents =  texts.stream().map(text -> {
                Document document = new Document();
                document.setPageContent(text);
                return document;
            }).collect(Collectors.toList());
        DashScopeEmbeddings embeddings = new DashScopeEmbeddings();
        List<Document> embededDocuments = embeddings.embedDocument(documents);
        System.out.println(JSON.toJSONString(embededDocuments));
    }

    @Test
    public void test_embedDocument_text_embedding_v2() {
        // success
        List<String> texts = new ArrayList<>();
        texts.add("什么是阿里巴巴");
        List<Document> documents =  texts.stream().map(text -> {
            Document document = new Document();
            document.setPageContent(text);
            return document;
        }).collect(Collectors.toList());
        DashScopeEmbeddings embeddings = new DashScopeEmbeddings();
        embeddings.setModel(DashScopeConstant.MODEL_TEXT_EMBEDDING_V2);
        List<Document> embededDocuments = embeddings.embedDocument(documents);
        System.out.println(JSON.toJSONString(embededDocuments));
    }
}
