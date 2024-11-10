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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import tech.amikos.chromadb.Client;
import tech.amikos.chromadb.Collection.QueryResponse;
import tech.amikos.chromadb.EmbeddingFunction;
import tech.amikos.chromadb.handler.ApiException;

import static com.alibaba.langengine.chroma.ChromaConfiguration.CHROMA_SERVER_URL;

/**
 * Chroma向量库
 * 使用的client仓库：https://github.com/chroma-core/chroma
 * Chroma向量数据库API地址：http://localhost:8000/docs#/
 * Chroma向量数据库Docker部署：https://docs.trychroma.com/deployment
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class Chroma extends VectorStore {

    /**
     * 向量库的embedding
     */
    private Embeddings embedding;

    /**
     * 标识一个唯一的仓库，可以看做是某个业务，向量内容的集合标识；某个知识库内容，这个知识库所有的内容都应该是相同的collectionId
     *
     * 名称的长度必须在 3 到 63 个字符之间。
     * 名称必须以小写字母或数字开头和结尾，并且可以在中间包含点、破折号和下划线。
     * 名称不能包含两个连续的点。
     * 名称不能是有效的 IP 地址。
     */
    private String collectionId;

    /**
     * 内部使用的client，不希望对外暴露
     */
    private Client _client;

    /**
     * 内部使用的对应的collection，不希望对外暴露
     */
    private tech.amikos.chromadb.Collection _collection;

    public Chroma(Embeddings embedding, String collectionId) throws ApiException {
        String serverUrl = CHROMA_SERVER_URL;
        this.collectionId = collectionId == null ? UUID.randomUUID().toString() : collectionId;
        this.embedding = embedding;
        this._client = new Client(serverUrl);
        this._collection = _client.createCollection(collectionId, null, true, new ChromaEmbeddingFunction());
    }

    public Chroma(String serverUrl, Embeddings embedding, String collectionId) throws ApiException {
        //service = new ChromaService(serverUrl, Duration.ofSeconds(Long.parseLong(OPENAI_AI_TIMEOUT)));
        this.collectionId = collectionId == null ? UUID.randomUUID().toString() : collectionId;
        this.embedding = embedding;
        this._client = new Client(serverUrl);
        this._collection = _client.createCollection(collectionId, null, true, new ChromaEmbeddingFunction());
    }

    /**
     * 添加文本向量，如果没有向量，系统会自动的使用embedding生成向量
     *
     * @param documents
     */
    @Override
    public void addDocuments(List<Document> documents) {
        // embedding
        try {
            for (Document document : documents) {
                if (StringUtils.isEmpty(document.getUniqueId())) {
                    document.setUniqueId(UUID.randomUUID().toString());
                }
                if (StringUtils.isEmpty(document.getPageContent())) {
                    continue;
                }
                if (MapUtils.isEmpty(document.getMetadata())) {
                    document.setMetadata(new java.util.HashMap<>());
                }

                List<Double> doubleList = document.getEmbedding();
                // doubleList转成FloatList
                List<Float> floatList = null;
                if (CollectionUtils.isNotEmpty(doubleList)) {
                    floatList = doubleList.stream().map(e -> e.floatValue()).collect(Collectors.toList());
                }

                Map<String, Object> metadata = document.getMetadata();
                Map<String, String> metadataString = new java.util.HashMap<>();
                // 转成Map<String,String>的形式
                metadata.keySet().forEach(key -> {
                    metadataString.put(key, metadata.get(key).toString());
                });

                _collection.add(floatList == null ? null : Arrays.asList(floatList),
                    Arrays.asList(metadataString),
                    Arrays.asList(document.getPageContent()),
                    Arrays.asList(document.getUniqueId())
                );
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加文本向量
     *
     * @param texts
     * @param metadatas
     * @param ids
     * @return
     * @throws ApiException
     */
    public List<String> addTexts(
        Iterable<String> texts,
        List<Map<String, String>> metadatas,
        List<String> ids
    ) throws ApiException {
        // Run more texts through the embeddings and add to the vectorstore.

        // Handle the case where the user doesn't provide ids on the Collection
        if (ids == null) {
            ids = new ArrayList<>();
            for (String text : texts) {
                ids.add(UUID.randomUUID().toString());
            }
        }
        List<Document> embeddings = null;
        List<String> textsList = new ArrayList<>();
        texts.forEach(textsList::add);
        if (this.embedding != null) {
            // 把texts转为List<String>
            embeddings = this.embedding.embedTexts(textsList);
        }

        List<List<Float>> embeddingsWithMetadatas = embeddings != null
            ? embeddings.stream()
            .map(document -> document.getEmbedding().toArray(new Double[] {}))
            .map(doubles -> Arrays.asList(doubles).stream()
                .map(Double::floatValue)
                .collect(Collectors.toList()))
            .collect(Collectors.toList())
            : null;

        _collection.add(
            embeddingsWithMetadatas,
            metadatas,
            textsList,
            ids
        );

        return ids;
    }

    /**
     * Chroma向量库查询
     *
     * @param query
     * @param k
     * @param maxDistanceValue
     * @param type
     * @return
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {

        try {
            QueryResponse queryResponse = _collection.query(Arrays.asList(query), k, null, null, null);

            List<Document> documents = new ArrayList<>();
            List<List<String>> ids = queryResponse.getIds();
            for (int i = 0; i < ids.get(0).size(); i++) {
                String id = ids.get(0).get(i);

                // 拿文档内容
                List<String> documentContents = queryResponse.getDocuments().get(0);
                String documentContent = documentContents.get(i);

                List<Float> distanceList = queryResponse.getDistances().get(0);
                Double distance = Double.valueOf(distanceList.get(i));
                Document document = new Document();
                document.setUniqueId(id);
                document.setPageContent(filter(documentContent));
                document.setScore(distance);
                document.setMetadata(queryResponse.getMetadatas().get(0).get(i));

                documents.add(document);
            }
            return documents;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String filter(String value) {
        value = value.replaceAll("<[^>]+>", ""); // 去掉所有HTML标签
        value = StringEscapeUtils.unescapeHtml4(value); // 去掉HTML实体
        return value;
    }

    private class ChromaEmbeddingFunction implements EmbeddingFunction {
        @Override
        public List<List<Float>> createEmbedding(List<String> documents) {
            List<Document> documentList = embedding.embedTexts(documents);

            // 合并下面两行
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
            // 这里忽略了对应的model
            List<Document> documentList = embedding.embedTexts(documents);

            // 合并下面两行
            List<List<Float>> list = documentList.stream()
                .map(document -> document.getEmbedding().toArray(new Double[] {}))
                .map(doubles -> Arrays.asList(doubles).stream()
                    .map(Double::floatValue)
                    .collect(Collectors.toList()))
                .collect(Collectors.toList());
            return list;
        }
    }
}
