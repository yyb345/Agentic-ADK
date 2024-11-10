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
package com.alibaba.langengine.openai.embeddings;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OpenAIEmbeddingsTest {

    @Test
    public void test_embedDocument() {
        List<String> texts = new ArrayList<>();
        texts.add("什么是阿里巴巴国际站");
        texts.add("什么是AI Business");
       List<Document> documents =  texts.stream().map(text -> {
                Document document = new Document();
                document.setPageContent(text);
                return document;
            }).collect(Collectors.toList());
        OpenAIEmbeddings openAIEmbeddings = new OpenAIEmbeddings();
        List<Document> embededDocuments = openAIEmbeddings.embedDocument(documents);
        System.out.println(JSON.toJSONString(embededDocuments));
    }
}
