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
package com.alibaba.langengine.azure.embeddings;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: andrea.phl
 * @create: 2024-01-29 16:52
 **/
public class AzureOpenAIEmbeddingsTest {

    private String apiBase = "**";
    private String apiKey = "**";
    private String deploymentName = "text-embedding-ada-002";
    private String apiVersion = "2023-06-01-preview";

    @Test
    public void test_embedDocument() {
        List<String> texts = new ArrayList<>();
        texts.add("What is alibaba?");
        List<Document> documents = texts.stream().map(text -> {
            Document document = new Document();
            document.setPageContent(text);
            return document;
        }).collect(Collectors.toList());
        AzureOpenAIEmbeddings openAIEmbeddings = new AzureOpenAIEmbeddings(apiBase, deploymentName, apiVersion, apiKey);
        List<Document> embededDocuments = openAIEmbeddings.embedDocument(documents);
        System.out.println(JSON.toJSONString(embededDocuments, true));
    }
}
