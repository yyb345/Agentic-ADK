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
package com.alibaba.langengine.vectorstore.tablestore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.TableStoreException;
import com.alicloud.openservices.tablestore.model.CapacityUnit;
import com.alicloud.openservices.tablestore.model.ColumnValue;
import com.alicloud.openservices.tablestore.model.CreateTableRequest;
import com.alicloud.openservices.tablestore.model.DeleteTableRequest;
import com.alicloud.openservices.tablestore.model.ListTableResponse;
import com.alicloud.openservices.tablestore.model.PrimaryKey;
import com.alicloud.openservices.tablestore.model.PrimaryKeyBuilder;
import com.alicloud.openservices.tablestore.model.PrimaryKeySchema;
import com.alicloud.openservices.tablestore.model.PrimaryKeyType;
import com.alicloud.openservices.tablestore.model.PrimaryKeyValue;
import com.alicloud.openservices.tablestore.model.PutRowRequest;
import com.alicloud.openservices.tablestore.model.ReservedThroughput;
import com.alicloud.openservices.tablestore.model.Row;
import com.alicloud.openservices.tablestore.model.RowPutChange;
import com.alicloud.openservices.tablestore.model.TableMeta;
import com.alicloud.openservices.tablestore.model.TableOptions;
import com.alicloud.openservices.tablestore.model.search.CreateSearchIndexRequest;
import com.alicloud.openservices.tablestore.model.search.DeleteSearchIndexRequest;
import com.alicloud.openservices.tablestore.model.search.DescribeSearchIndexRequest;
import com.alicloud.openservices.tablestore.model.search.DescribeSearchIndexResponse;
import com.alicloud.openservices.tablestore.model.search.FieldSchema;
import com.alicloud.openservices.tablestore.model.search.FieldType;
import com.alicloud.openservices.tablestore.model.search.IndexSchema;
import com.alicloud.openservices.tablestore.model.search.ListSearchIndexRequest;
import com.alicloud.openservices.tablestore.model.search.ListSearchIndexResponse;
import com.alicloud.openservices.tablestore.model.search.SearchHit;
import com.alicloud.openservices.tablestore.model.search.SearchIndexInfo;
import com.alicloud.openservices.tablestore.model.search.SearchQuery;
import com.alicloud.openservices.tablestore.model.search.SearchRequest;
import com.alicloud.openservices.tablestore.model.search.SearchResponse;
import com.alicloud.openservices.tablestore.model.search.query.KnnVectorQuery;
import com.alicloud.openservices.tablestore.model.search.query.QueryBuilders;
import com.alicloud.openservices.tablestore.model.search.vector.VectorDataType;
import com.alicloud.openservices.tablestore.model.search.vector.VectorMetricType;
import com.alicloud.openservices.tablestore.model.search.vector.VectorOptions;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Slf4j
public class TableStoreDB extends VectorStore {

    private Embeddings embeddings;
    private List<FieldSchema> metadataSchemaList;
    private final SyncClient client;
    private String tableName;
    private String indexName;
    private Integer dimension;
    private static final String UNIQUE_ID = "_unique_id";
    private static final String MODEL_TYPE = "_model_type";
    private static final String CONTENT = "_content";
    private static final String CHUNK_INDEX = "_chunk_index";
    private static final String ROW_CONTENT = "_row_content";
    private static final VectorMetricType DEFAULT_VECTOR_METRIC_TYPE = VectorMetricType.COSINE;

    /**
     * 调用构造函数将自动在tablestore中创建表和索引
     * @param embeddings 想要使用的embedding，会根据填入的embedding设置tablestore索引向量字段的维度，因此当embedding生成向量的维度变化时，请同步调整tablestore索引向量字段的维度。
     *                   调整方法为调用{@link TableStoreDB#deleteTableAndIndex()}删除旧的表和索引，随后调用{@link TableStoreDB#createTableAndIndex()}生成新的表和索引。
     */
    public TableStoreDB(@NonNull Embeddings embeddings) {
        this.tableName = TableStoreConfig.TABLESTORE_TABLE_NAME;
        this.indexName = TableStoreConfig.TABLESTORE_INDEX_NAME;
        this.client = new SyncClient(TableStoreConfig.TABLESTORE_ENDPOINT,
            TableStoreConfig.TABLESTORE_ACCESS_SECRET_ID,
            TableStoreConfig.TABLESTORE_ACCESS_SECRET_KEY,
            TableStoreConfig.TABLESTORE_INSTANCE_NAME
        );
        this.embeddings = embeddings;
        this.dimension = getDimensionByCurrentEmbeddings(embeddings);
        createTableAndIndex();
    }

    private List<FieldSchema> getFieldSchemas(Integer dimension) {
        List<FieldSchema> fieldSchemas = new ArrayList<>();
        fieldSchemas.add(new FieldSchema(UNIQUE_ID, FieldType.KEYWORD).setIndex(true));
        fieldSchemas.add(new FieldSchema(MODEL_TYPE, FieldType.KEYWORD).setIndex(true));
        fieldSchemas.add(new FieldSchema(CHUNK_INDEX, FieldType.LONG).setIndex(true));
        fieldSchemas.add(new FieldSchema(ROW_CONTENT, FieldType.TEXT).setIndex(true).setAnalyzer(FieldSchema.Analyzer.MaxWord));
        fieldSchemas.add(new FieldSchema(CONTENT, FieldType.VECTOR).setIndex(true).setVectorOptions(new VectorOptions(VectorDataType.FLOAT_32, dimension, DEFAULT_VECTOR_METRIC_TYPE)));
        return Collections.unmodifiableList(fieldSchemas);
    }

    /**
     * 设置新的embeddings时，会自动获取新embeddings生成向量的维度，并更新dimension
     */
    public void setEmbeddings(Embeddings embeddings) {
        this.embeddings = embeddings;
        this.dimension = getDimensionByCurrentEmbeddings(embeddings);
    }

    public static Integer getDimensionByCurrentEmbeddings(Embeddings embeddings) {
        List<Document> embeddingDocuments = embeddings.embedTexts(Lists.newArrayList("test"));
        Document document = embeddingDocuments.get(0);
        return document.getEmbedding().size();
    }

    /**
     * 默认在第一次创建{@link TableStoreDB}实例时会创建表和索引，如果表和索引已经被删除，调用此函数将再次创建表和索引。
     * 创建索引时，索引schema会根据当前dimension创建向量字段的属性值
     */
    public void createTableAndIndex() {
        createTableIfNotExist();
        createIndexIfNotExist();
    }

    private void createTableIfNotExist() {
        ListTableResponse listTableResponse = client.listTable();
        if (listTableResponse.getTableNames().contains(tableName)) {
            log.info("table:{} already exists", tableName);
            return;
        }
        TableMeta tableMeta = new TableMeta(this.tableName);
        tableMeta.addPrimaryKeyColumn(new PrimaryKeySchema(UNIQUE_ID, PrimaryKeyType.STRING));
        TableOptions tableOptions = new TableOptions(-1, 1);
        CreateTableRequest request = new CreateTableRequest(tableMeta, tableOptions);
        request.setReservedThroughput(new ReservedThroughput(new CapacityUnit(0, 0)));
        client.createTable(request);
        log.info("create table:{}", tableName);
    }

    private void createIndexIfNotExist() {
        ListSearchIndexRequest listSearchIndexRequest = new ListSearchIndexRequest();
        listSearchIndexRequest.setTableName(tableName);
        ListSearchIndexResponse listSearchIndexResponse = client.listSearchIndex(listSearchIndexRequest);
        for (SearchIndexInfo indexInfo : listSearchIndexResponse.getIndexInfos()) {
            if (indexInfo.getIndexName().equals(indexName)) {
                log.info("index:{} already exists", indexName);
                return;
            }
        }
        this.metadataSchemaList = getFieldSchemas(this.dimension);
        CreateSearchIndexRequest request = new CreateSearchIndexRequest();
        request.setTableName(tableName);
        request.setIndexName(indexName);
        IndexSchema indexSchema = new IndexSchema();
        indexSchema.setFieldSchemas(metadataSchemaList);
        request.setIndexSchema(indexSchema);
        client.createSearchIndex(request);
        log.info("create index:{}", indexName);
    }

    /**
     * 默认在第一次创建{@link TableStoreDB}实例时会创建表和索引，若您想修改索引schema（如向量字段的dimension），可以调用此函数删除已经存在的表和索引并重新创建
     */
    public void deleteTableAndIndex() {
        DeleteSearchIndexRequest deleteSearchIndexRequest = new DeleteSearchIndexRequest();
        deleteSearchIndexRequest.setTableName(tableName);
        deleteSearchIndexRequest.setIndexName(indexName);
        try {
            client.deleteSearchIndex(deleteSearchIndexRequest);
            log.info("end delete index:" + indexName);
        } catch (TableStoreException e) {
            if (!"OTSObjectNotExist".equals(e.getErrorCode())) {
                throw e;
            }
            log.info("index not exist:" + indexName);
        }
        log.info("deleteTable: " + tableName);
        DeleteTableRequest deleteTableRequest = new DeleteTableRequest(tableName);
        client.deleteTable(deleteTableRequest);
    }

    /**
     * @exception RuntimeException 提示tablestore索引的向量字段维度与embedding的维度不一致，这将导致embedding生成的向量无法正常的写入或查询。此时有两种解决方案：
     * <p>1.调用{@link TableStoreDB#deleteTableAndIndex()}删除旧的表和索引，随后调用{@link TableStoreDB#createTableAndIndex()}生成新的表和索引，使索引向量字段维度与embedding一致</p>
     * <p>2.调用{@link TableStoreDB#setEmbeddings(Embeddings)}设置新的embeddings，新的embeddings生成向量维度应与索引向量字段维度一致</p>
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        if (ifDimensionConflictWithIndexSchema()) {
            throw new RuntimeException("the dimension of the vector field in tablestore is conflicted with that of the embeddings");
        }
        documents = embeddings.embedDocument(documents);
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            if (document.getUniqueId() == null) {
                throw new IllegalArgumentException(String.format("unique id should not be null, index in the list: %d", i));
            }
            if (document.getPageContent() == null) {
                throw new IllegalArgumentException(String.format("content should not be null, index in the list: %d", i));
            }
            if (document.getEmbedding() == null) {
                throw new IllegalArgumentException(String.format("embedding should not be null, index in the list: %d", i));
            }
            PrimaryKeyBuilder primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
            primaryKeyBuilder.addPrimaryKeyColumn(UNIQUE_ID, PrimaryKeyValue.fromString(document.getUniqueId()));
            PrimaryKey primaryKey = primaryKeyBuilder.build();
            RowPutChange rowPutChange = new RowPutChange(tableName, primaryKey);
            rowPutChange.addColumn(MODEL_TYPE, ColumnValue.fromString(embeddings.getModelType()));
            if (document.getIndex() != null) {
                rowPutChange.addColumn(CHUNK_INDEX, ColumnValue.fromLong(document.getIndex()));
            }
            rowPutChange.addColumn(ROW_CONTENT, ColumnValue.fromString(document.getPageContent()));
            String embedding = convertVecToStr(document.getEmbedding());
            rowPutChange.addColumn(CONTENT, ColumnValue.fromString(embedding));
            try {
                client.putRow(new PutRowRequest(rowPutChange));
            } catch (Exception e) {
                throw new RuntimeException(String.format("add embedding data failed, id:%s, textSegment:%s,embedding:%s", document.getUniqueId(), document.getPageContent(), embedding), e);
            }
        }
    }

    private boolean ifDimensionConflictWithIndexSchema() {
        DescribeSearchIndexRequest describeSearchIndexRequest = new DescribeSearchIndexRequest();
        describeSearchIndexRequest.setTableName(tableName);
        describeSearchIndexRequest.setIndexName(indexName);
        DescribeSearchIndexResponse describeSearchIndexResponse = client.describeSearchIndex(describeSearchIndexRequest);
        for (FieldSchema fieldSchema : describeSearchIndexResponse.getSchema().getFieldSchemas()) {
            if (fieldSchema.getFieldName().equals(CONTENT)) {
                return !fieldSchema.getVectorOptions().getDimension().equals(this.dimension);
            }
        }
        throw new RuntimeException("there is no vector column in the index");
    }

    private String convertVecToStr(List<Double> vector) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (Double d : vector) {
            stringBuilder.append(d.floatValue()).append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1).append("]");
        return stringBuilder.toString();
    }

    private List<Double> convertStrToVec(String embedding) throws IllegalArgumentException {
        if (embedding == null || embedding.isEmpty()) {
            throw new IllegalArgumentException("embedding string is null or empty");
        }
        if (!embedding.startsWith("[") || !embedding.endsWith("]")) {
            throw new IllegalArgumentException("embedding string is not in the correct format: " + embedding);
        }
        embedding = embedding.substring(1, embedding.length() - 1).trim();
        String[] parts = embedding.split(",");
        if (parts.length != dimension) {
            throw new IllegalArgumentException("Number of elements (" + parts.length + ") does not match the expected dimension (" + dimension + ")");
        }
        List<Double> result = new ArrayList<>(dimension);
        for (int i = 0; i < parts.length; i++) {
            try {
                result.add(Double.parseDouble(parts[i].trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid double value at index " + i + ": " + parts[i], e);
            }
        }
        return result;
    }

    /**
     * @exception RuntimeException 提示tablestore索引的向量字段维度与embedding的维度不一致，这将导致embedding生成的向量无法正常的写入或查询。此时有两种解决方案：
     * <p>1.调用{@link TableStoreDB#deleteTableAndIndex()}删除旧的表和索引，随后调用{@link TableStoreDB#createTableAndIndex()}生成新的表和索引，使索引向量字段维度与embedding一致</p>
     * <p>2.调用{@link TableStoreDB#setEmbeddings(Embeddings)}设置新的embeddings，新的embeddings生成向量维度应与索引向量字段维度一致</p>
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        return similaritySearch(query, k, maxDistanceValue, type, null);
    }

    /**
     * @exception RuntimeException 提示tablestore索引的向量字段维度与embedding的维度不一致，这将导致embedding生成的向量无法正常的写入或查询。此时有两种解决方案：
     * <p>1.调用{@link TableStoreDB#deleteTableAndIndex()}删除旧的表和索引，随后调用{@link TableStoreDB#createTableAndIndex()}生成新的表和索引，使索引向量字段维度与embedding一致</p>
     * <p>2.调用{@link TableStoreDB#setEmbeddings(Embeddings)}设置新的embeddings，新的embeddings生成向量维度应与索引向量字段维度一致</p>
     */
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type, SearchRequest searchRequest) {
        if (ifDimensionConflictWithIndexSchema()) {
            throw new RuntimeException("the dimension of the vector field in tablestore is conflicted with that of the embeddings");
        }
        List<String> embeddingStrings = embeddings.embedQuery(query, k);
        if (embeddingStrings.isEmpty()) {
            return new ArrayList<>();
        }
        String embeddingString = embeddingStrings.get(0);
        if (type == null) {
            type = Integer.valueOf(embeddings.getModelType());
        }
        float[] queryArray = convertVecToFloatArray(convertStrToVec(embeddingString));
        KnnVectorQuery.Builder knnQueryBuilder = QueryBuilders.knnVector(CONTENT, k, queryArray).filter(QueryBuilders.term(MODEL_TYPE, type).build());
        if (maxDistanceValue != null) {
            knnQueryBuilder.minScore((float) (1 - maxDistanceValue));
        }
        if (searchRequest == null) {
            searchRequest = SearchRequest.newBuilder()
                .tableName(tableName)
                .indexName(indexName)
                .searchQuery(SearchQuery.newBuilder().query(knnQueryBuilder).getTotalCount(false).limit(k).build())
                .returnAllColumnsFromIndex(true)
                .build();
        }
        SearchResponse searchResponse = client.search(searchRequest);
        List<Document> res = new ArrayList<>(searchResponse.getSearchHits().size());
        for (SearchHit searchHit : searchResponse.getSearchHits()) {
            Row hitRow = searchHit.getRow();
            Document document = new Document();
            if (!hitRow.getColumn(CHUNK_INDEX).isEmpty()) {
                document.setIndex((int) hitRow.getColumn(CHUNK_INDEX).get(0).getValue().asLong());
            }
            document.setUniqueId(hitRow.getPrimaryKey().getPrimaryKeyColumnsMap().get(UNIQUE_ID).getValue().asString());
            if (!hitRow.getColumn(ROW_CONTENT).isEmpty()) {
                document.setPageContent(hitRow.getColumn(ROW_CONTENT).get(0).getValue().asString());
            }
            document.setScore(1 - searchHit.getScore());
            if (!hitRow.getColumn(CONTENT).isEmpty()) {
                document.setEmbedding(convertStrToVec(hitRow.getColumn(CONTENT).get(0).getValue().asString()));
            }
            res.add(document);
        }
        return res;
    }

    private float[] convertVecToFloatArray(List<Double> vector) {
        float[] result = new float[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            result[i] = vector.get(i).floatValue();
        }
        return result;
    }
}
