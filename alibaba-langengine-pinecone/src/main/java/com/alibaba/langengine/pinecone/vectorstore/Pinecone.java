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
package com.alibaba.langengine.pinecone.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.pinecone.PineconeConfiguration;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Pinecone向量库
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class Pinecone extends VectorStore {

    private Embeddings embedding;

    private static final String DEFAULT_NAMESPACE = "default";

    private final String nameSpace;

    private static final String PINECONE_API_KEY = PineconeConfiguration.PINECONE_API_KEY;

    private static final String PINECONE_ENVIRONMENT = PineconeConfiguration.PINECONE_ENVIRONMENT;

    private static final String PINECONE_PROJECT_NAME = PineconeConfiguration.PINECONE_PROJECT_NAME;

    private String upsertServerUrl;

    private String queryServerUrl;

    public Pinecone(String index, String nameSpace) {
        this.upsertServerUrl = String.format("https://%s-%s.svc.%s.pinecone.io/vectors/upsert",
                index, PINECONE_PROJECT_NAME, PINECONE_ENVIRONMENT);
        this.queryServerUrl = String.format("https://%s-%s.svc.%s.pinecone.io/query",
                index, PINECONE_PROJECT_NAME, PINECONE_ENVIRONMENT);
        this.nameSpace = nameSpace == null ? DEFAULT_NAMESPACE : nameSpace;
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if(documents == null || documents.size() == 0) {
            return;
        }
        documents = embedding.embedDocument(documents);
        if(documents.size() == 0) {
            return;
        }
        try {
            AsyncHttpClient client = new DefaultAsyncHttpClient();

            for (Document document : documents) {
                PineconeUpsertRequest pineconeUpsertRequest = new PineconeUpsertRequest();
                pineconeUpsertRequest.setNamespace(nameSpace);
                pineconeUpsertRequest.setVectors(new ArrayList<>());

                PineconeUpsertRequest.PineconeVector pineconeVector = new PineconeUpsertRequest.PineconeVector();
                pineconeVector.setId(document.getUniqueId());
                pineconeVector.setValues(document.getEmbedding());

                pineconeVector.setMetadata(new HashMap<>());
                pineconeVector.getMetadata().put("content", document.getPageContent());
                if(document.getMetadata() != null && document.getMetadata().containsKey("name")) {
                    pineconeVector.getMetadata().put("name", document.getMetadata().get("name").toString());
                }
                pineconeUpsertRequest.getVectors().add(pineconeVector);

                String body = JSON.toJSONString(pineconeUpsertRequest);
                ListenableFuture<Response> whenResponse = client.preparePost(upsertServerUrl)
                        .setHeader("accept", "application/json")
                        .setHeader("content-type", "application/json")
                        .setHeader("Api-Key", PINECONE_API_KEY)
                        .setBody(body)
                        .execute();
                Response response = whenResponse.get();
                log.warn("responseBody:" + response.getResponseBody());
            }
            client.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
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

        try {
            AsyncHttpClient client = new DefaultAsyncHttpClient();

            PineconeQueryRequest pineconeQueryRequest = new PineconeQueryRequest();
            pineconeQueryRequest.setNamespace(nameSpace);
            pineconeQueryRequest.setTopK(k);
            pineconeQueryRequest.setQueries(new ArrayList<>());

            PineconeQueryRequest.PineconeQuery pineconeQuery = new PineconeQueryRequest.PineconeQuery();
            pineconeQuery.setValues(embeddings.stream().map(e -> Double.parseDouble(e)).collect(Collectors.toList()));

            pineconeQueryRequest.getQueries().add(pineconeQuery);

            String body = JSON.toJSONString(pineconeQueryRequest);
            ListenableFuture<Response> whenResponse = client.preparePost(queryServerUrl)
                    .setHeader("accept", "application/json")
                    .setHeader("content-type", "application/json")
                    .setHeader("Api-Key", PINECONE_API_KEY)
                    .setBody(body)
                    .execute();
            Response response = whenResponse.get();
//            log.warn("responseBody:" + response.getResponseBody());
            client.close();

            PineconeQueryResponse pineconeQueryResponse = JSON.parseObject(response.getResponseBody(), PineconeQueryResponse.class);
            if(pineconeQueryResponse == null) {
                return new ArrayList<>();
            }
            return pineconeQueryResponse.getResults().stream().map(e -> {
                Document document = new Document();
                document.setUniqueId(e.getMatches().get(0).getId());
                document.setPageContent(e.getMatches().get(0).getMetadata().get("content").toString());
                document.setScore(e.getMatches().get(0).getScore());
                return document;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
