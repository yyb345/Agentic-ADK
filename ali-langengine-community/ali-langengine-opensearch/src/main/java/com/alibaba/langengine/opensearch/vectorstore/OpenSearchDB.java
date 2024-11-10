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
package com.alibaba.langengine.opensearch.vectorstore;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.search.swift.protocol.SwiftMessage;
import com.aliyun.ha3engine.vector.Client;
import com.aliyun.ha3engine.vector.models.Config;
import com.aliyun.ha3engine.vector.models.QueryRequest;
import com.aliyun.ha3engine.vector.models.SearchResponse;
import com.google.protobuf.ByteString;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author cuzz.lb
 * @date 2023/11/10 16:10
 */
@Data
@Slf4j
public class OpenSearchDB extends VectorStore {

    private Embeddings embedding;

    private static final String FIELD_SEPARATOR = (char) 31 + "\n";
    private static final String DOC_SEPARATOR = (char) 30 + "\n";
    private static final String VECTOR_SEPARATOR = ",";

    private Client client;

    @Override
    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        documents = embedding.embedDocument(documents);
        List<KnowledgeDO> knowledgeDOList = new ArrayList<>();
        for (Document document : documents) {
            KnowledgeDO knowledgeDO = new KnowledgeDO();
            if (!StringUtils.isEmpty(document.getUniqueId())) {
                knowledgeDO.setContentId(document.getUniqueId());
            }
            knowledgeDO.setType(Integer.valueOf(embedding.getModelType()));
            String embeddingString = document.getEmbedding().stream().map(Double::floatValue).map(String::valueOf).collect(Collectors.joining(VECTOR_SEPARATOR));
            knowledgeDO.setContent(embeddingString);
            knowledgeDO.setMetadata(JSONObject.toJSONString(document.getMetadata()));
            if (document.getMetadata() != null && document.getMetadata().containsKey("namespace")) {
                knowledgeDO.setNamespace(document.getMetadata().get("namespace").toString());
            }

            knowledgeDO.setIdx(document.getIndex());
            knowledgeDO.setRowContent(document.getPageContent());
            knowledgeDOList.add(knowledgeDO);
        }
        insertHa3AutoClose(knowledgeDOList);
    }


    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        return similaritySearch(query, k, maxDistanceValue, type, null, null);
    }

    public List<Document> similaritySearch(String query, int k, String namespace) {
        return similaritySearch(query, k, null, null, namespace, null);
    }

    /**
     * 返回与查询最相似的文档
     *
     * @param query
     * @param k
     * @param maxDistanceValue
     * @return
     */
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, String namespace) {
        return similaritySearch(query, k, maxDistanceValue, null, namespace, null);
    }

    /**
     * 返回与查询最相似的文档
     *
     * @param query
     * @param k
     * @param type
     * @return
     */
    public List<Document> similaritySearch(String query, int k, Integer type, String namespace) {
        return similaritySearch(query, k, null, type, namespace, null);
    }

    /**
     * 通过 namespace
     *
     * @param query
     * @param k
     * @param maxDistanceValue
     * @param type
     * @param namespace
     * @return
     */
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type, String namespace, String filter) {
        List<String> embeddingStrings = embedding.embedQuery(query, k);
        if (embeddingStrings.size() == 0 || !embeddingStrings.get(0).startsWith("[")) {
            return new ArrayList<>();
        }

        String embeddingString = embeddingStrings.get(0);

        if (type == null) {
            type = Integer.valueOf(embedding.getModelType());
        }

        QueryRequest request = new QueryRequest();
        if (StringUtils.isNotBlank(namespace)) {
            request.setNamespace(namespace);
        }
        // 必填项, 查询的表名
        request.setTableName(OpenSearchConfig.OPENSEARCH_DATASOURCE_TABLE_NAME);
        List<Float> floats = JSONObject.parseArray(embeddingString, Float.class);
        request.setVector(floats);
        request.setTopK(k); // 非必填项, 返回个数
        request.setIncludeVector(true); // 非必填项, 是否返回文档中的向量信息
        if(StringUtils.isEmpty(filter)) {
            request.setFilter(String.format("type=\"%s\"", type));
        } else {
            request.setFilter(filter);
        }
        request.setOutputFields(Arrays.asList("row_content", "type", "idx", "metadata"));
        SearchResponse response = this.query(request);
        List<Document> documentList = convertDocumentList(response);

        //欧式 TODO
        if (maxDistanceValue != null) {
            documentList = documentList.stream().filter(document -> document.getScore() != null
                            && document.getScore() < maxDistanceValue)
                    .collect(Collectors.toList());
        }
        //内积 TODO IOC

        return documentList;
    }


    private List<Document> convertDocumentList(SearchResponse response) {
        String body = response.getBody();
        // 解析body中的result
        JSONObject jsonObject = JSONObject.parseObject(body);
        JSONArray result = jsonObject.getJSONArray("result");
        List<Document> documentDTOList = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            Document documentDTO = new Document();
            JSONObject doc = result.getJSONObject(i);
            documentDTO.setUniqueId(doc.getString("id"));
            documentDTO.setScore(doc.getDouble("score"));
            documentDTO.setEmbedding(doc.getJSONArray("vector").toJavaList(Double.class));
            JSONObject fields = doc.getJSONObject("fields");
            documentDTO.setPageContent(fields.getString("row_content"));
            documentDTO.setIndex(fields.getInteger("idx"));
            // 把 metadata string 转换成map，如果为空，就转为空map,放到documentDTO中
            String metadataString = fields.getString("metadata");
            if (fields.containsKey("metadata") && StringUtils.isNotEmpty(metadataString)) {
                try {
                    Map<String, Object> metadata = JSONObject.parseObject(metadataString, Map.class);
                    documentDTO.setMetadata(metadata);
                } catch (Exception e) {
                    log.error("metadata string parse error", e);
                }
            }
            if (documentDTO.getMetadata() == null) {
                documentDTO.setMetadata(new HashMap<>());
            }


            documentDTOList.add(documentDTO);
        }
        return documentDTOList;
    }

    public SearchResponse query(QueryRequest queryRequest) {
        if (client == null) {
            initOpenSearch();
        }

        try {
            return client.query(queryRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void initOpenSearch() {
        /*
          初始化问天引擎client
         */
        Config config = new Config();


        config.setInstanceId(OpenSearchConfig.OPENSEARCH_INSTANCE_ID);
        config.setEndpoint(OpenSearchConfig.OPENSEARCH_ENDPOINT);

        try {
            client = new Client(config);
        } catch (Exception e) {
            log.error("初始化openSearch失败", e);
        }
    }

    public void insertHa3AutoClose(List<KnowledgeDO> knowledgeDOList) {
        for (KnowledgeDO doc : knowledgeDOList) {
            // 把 doc 转为 hashMap
            log.warn("Send msg to ha3, msg={}", doc);
            SwiftMessage.WriteMessageInfo msgInfo = constructBsDocMsg(doc.toMap(), "content_id", "add");
            try {
                AutoCloseSwiftWriter.write(OpenSearchConfig.OPENSARCH_SWIFT_SERVER_ROOT, OpenSearchConfig.OPENSARCH_SWIFT_TOPIC, msgInfo);
            } catch (Exception e) {
                log.error("Send msg to ha3, msg={}", doc, e);
            }
        }

    }


    private SwiftMessage.WriteMessageInfo constructBsDocMsg(Map<String, String> doc, String hashKey, String operation) {

        // operation 表示该doc指定的操作，常用值有 : {add, delete, update_field}
        // hashKey 指定doc中用来作为HashStr的key值
        SwiftMessage.WriteMessageInfo.Builder msg = SwiftMessage.WriteMessageInfo.newBuilder();
        msg.setHashStr(ByteString.copyFromUtf8(doc.get(hashKey))); // 设置hashkey，swift根据给字符串hash到不同的partition
        // 按照ha3文档格式组装swift消息。
        StringBuilder msgData = new StringBuilder();
        // 设置CMD字段，注意每个字段后需要添加field 分隔符
        msgData.append("CMD=");
        msgData.append(operation);
        msgData.append(FIELD_SEPARATOR);
        // 设置doc的其它字段
        for (Map.Entry<String, String> entry : doc.entrySet()) {
            msgData.append(entry.getKey()).append("=").append(entry.getValue()).append(FIELD_SEPARATOR);
        }
        // 设置doc结束的分隔符
        msgData.append(DOC_SEPARATOR);
        msg.setData(ByteString.copyFromUtf8(msgData.toString()));
        return msg.build();
    }
}
