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
package com.alibaba.langengine.chroma.vectorstore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;

import tech.amikos.chromadb.Client;
import tech.amikos.chromadb.Collection;
import tech.amikos.chromadb.EmbeddingFunction;

/**
 * @author aihe.ah
 * @time 2023/12/17
 * 功能说明：
 */
public class ChromaDemo {
    public static void main(String[] args) {
        try {
            Client client = new Client("http://localhost:8000");
            //String apiKey = System.getenv("OPENAI_API_KEY");
            //EmbeddingFunction ef = new OpenAIEmbeddingFunction(apiKey);

            FakeEmbeddings embeddings = new FakeEmbeddings();

//            DashScopeEmbeddings embeddings = new DashScopeEmbeddings(System.getenv("DASHSCOPE_API_KEY"));
            Collection collection = client.createCollection("test-collection", null, true, new EmbeddingFunction() {
                @Override
                public List<List<Float>> createEmbedding(List<String> documents) {
                    List<Document> documentList = embeddings.embedTexts(documents);
                    List<List<Float>> list = documentList.stream()
                        .map(document -> document.getEmbedding().toArray(new Double[] {}))
                        .map(doubles -> Arrays.asList(doubles).stream()
                            .map(Double::floatValue)
                            .collect(Collectors.toList()))
                        .collect(Collectors.toList());
                    return list;
                }

                @Override
                public List<List<Float>> createEmbedding(List<String> documents, String model) {
                    // TODO Auto-generated method stub
                    return null;
                }
            });
            List<Map<String, String>> metadata = new ArrayList<>();
            metadata.add(new HashMap<String, String>() {{
                put("type", "scientist");
            }});
            metadata.add(new HashMap<String, String>() {{
                put("type", "spy");
            }});
            collection.add(null, metadata,
                Arrays.asList("Hello, my name is John. I am a Data Scientist.", "Hello, my name is Bond. I am a Spy."),
                Arrays.asList("1", "2"));

            Collection.QueryResponse qr = collection.query(Arrays.asList("Who is the spy"), 10, null, null, null);
            System.out.println(qr);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }
}
