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
package com.alibaba.langengine.djl.embeddings.texttovec;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.djl.embeddings.SentenceTransformerEmbeddings;
import com.alibaba.langengine.djl.embeddings.SentenceTransformerEnum;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SentenceTransformerEmbeddingsTest {

    @Test
    public void test_embedDocument() {
        // success
        List<String> texts = new ArrayList<>();
        texts.add("阿里巴巴是什么样的企业");
        List<Document> documents =  texts.stream().map(text -> {
            Document document = new Document();
            document.setPageContent(text);
            return document;
        }).collect(Collectors.toList());

//        String modelPath = getClass().getClassLoader().getResource("all-MiniLM-L6-v2").getPath();
//        SentenceTransformerEmbeddings embeddings = new SentenceTransformerEmbeddings(modelPath, false);

//        SentenceTransformerEmbeddings embeddings = new SentenceTransformerEmbeddings("/Users/xiaoxuan.lp/works/runting/build/pytorch_models/all-MiniLM-L6-v2", false);
//        List<Document> embededDocuments = embeddings.embedDocument(documents);
//        System.out.println(JSON.toJSONString(embededDocuments));
//        embededDocuments.stream().forEach(embededDocument -> {
//            System.out.println(embededDocument.getEmbedding().size());
//        });

        SentenceTransformerEmbeddings embeddings = new SentenceTransformerEmbeddings(SentenceTransformerEnum.ALL_MINILM_L6_V2.getModelId(), true);
        List<Document> embededDocuments = embeddings.embedDocument(documents);
        System.out.println(JSON.toJSONString(embededDocuments));
        embededDocuments.stream().forEach(embededDocument -> {
            System.out.println(embededDocument.getEmbedding().size());
        });
    }
}
