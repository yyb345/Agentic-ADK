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
import com.alibaba.langengine.djl.embeddings.GanymedeNilEmbeddings;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.alibaba.langengine.djl.embeddings.GanymedeNilEnum.TEXT2VEC_LARGE_CHINESE;

public class GanymedeNilEmbeddingsTest {

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

//        GanymedeNilEmbeddings embeddings = new GanymedeNilEmbeddings("/Users/xiaoxuan.lp/works/runting/build/pytorch_models/text2vec-large-chinese", false);
//        List<Document> embededDocuments = embeddings.embedDocument(documents);
//        System.out.println(JSON.toJSONString(embededDocuments));
//        embededDocuments.stream().forEach(embededDocument -> {
//            System.out.println(embededDocument.getEmbedding().size());
//        });

        GanymedeNilEmbeddings embeddings = new GanymedeNilEmbeddings(TEXT2VEC_LARGE_CHINESE.getModelId(), true);
        List<Document> embededDocuments = embeddings.embedDocument(documents);
        System.out.println(JSON.toJSONString(embededDocuments));
        embededDocuments.stream().forEach(embededDocument -> {
            System.out.println(embededDocument.getEmbedding().size());
        });
    }
}
