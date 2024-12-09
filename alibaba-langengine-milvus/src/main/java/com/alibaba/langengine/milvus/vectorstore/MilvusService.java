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
package com.alibaba.langengine.milvus.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.google.common.collect.Lists;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.*;
import io.milvus.param.ConnectParam;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.*;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.partition.CreatePartitionParam;
import io.milvus.param.partition.HasPartitionParam;
import io.milvus.response.SearchResultsWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author: andrea.phl
 * @create: 2023-12-19 10:44
 **/
@Slf4j
@Data
public class MilvusService {

    private static final String EXCEPTION_COLLECTION_NOT_LOADED = "collection not loaded";
    
    private String collection;
    
    private String partition;

    private MilvusParam milvusParam;

    final MilvusServiceClient milvusClient;
    
    public MilvusService(String serverUrl, String collection, String partition, MilvusParam milvusParam) {
        this.collection = collection;
        this.partition = partition;
        this.milvusParam = milvusParam;
        int port = 19530;
        String host = StringUtils.defaultIfEmpty(serverUrl, "127.0.0.1");
        int separatorIndex = host.indexOf(":");
        if (separatorIndex > 0) {
            port = Integer.parseInt(host.substring(separatorIndex + 1));
            host = host.substring(0, separatorIndex);
        }
        milvusClient = new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withHost(host)
                        .withPort(port)
                        .build()
        );
        log.info("MilvusService host=" + host + ", port=" + port + ", collection=" + collection + ", partition=" + StringUtils.defaultString(partition));
    }

    /**
     * 添加文档到Milvus
     * @param documents 文档列表
     */
    public void addDocuments(List<Document> documents) {
        MilvusParam param = loadParam();
        String fieldNameUniqueId = param.getFieldNameUniqueId();
        String fieldNamePageContent = param.getFieldNamePageContent();
        String fieldNameEmbedding = param.getFieldNameEmbedding();

        List<Long> uniqueIdList = Lists.newArrayList();
        List<List<Float>> embeddingsList = Lists.newArrayList();
        List<String> pageContentList = Lists.newArrayList();
        for (Document document : documents) {
            uniqueIdList.add(NumberUtils.toLong(document.getUniqueId()));
            List<Float> embeddings = Lists.newArrayList();
            for (Double embedding : document.getEmbedding()) {
                embeddings.add((float) (double) embedding);
            }
            embeddingsList.add(embeddings);
            pageContentList.add(document.getPageContent());
        }

        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field(fieldNameUniqueId, uniqueIdList));
        fields.add(new InsertParam.Field(fieldNameEmbedding, embeddingsList));
        fields.add(new InsertParam.Field(fieldNamePageContent, pageContentList));

        InsertParam.Builder insertParamBuilder = InsertParam.newBuilder()
                .withCollectionName(collection)
                .withFields(fields);
        if (StringUtils.isNotBlank(partition)) {
            insertParamBuilder.withPartitionName(partition);
        }
        InsertParam insertParam = insertParamBuilder.build();
        R<MutationResult> result = milvusClient.insert(insertParam);
    }

    /**
     * 向量检索
     * @param embeddings 向量
     * @param k 检索数量
     * @return 文档列表
     */
    public List<Document> similaritySearch(List<Float> embeddings, int k) {
        MilvusParam param = loadParam();
        MilvusParam.InitParam initParam = param.getInitParam();
        String fieldNameUniqueId = param.getFieldNameUniqueId();
        String fieldNamePageContent = param.getFieldNamePageContent();
        String fieldNameEmbedding = param.getFieldNameEmbedding();
        Map<String, Object> searchParams = param.getSearchParams();

        List<String> searchOutputFields = Lists.newArrayList(fieldNameUniqueId, fieldNamePageContent);

        SearchParam.Builder searchParamBuilder = SearchParam.newBuilder()
                .withCollectionName(collection)
                .withConsistencyLevel(ConsistencyLevelEnum.STRONG)
                .withMetricType(initParam.getIndexEmbeddingsMetricType())
                .withOutFields(searchOutputFields)
                .withTopK(k)
                .withVectors(Collections.singletonList(embeddings))
                .withVectorFieldName(fieldNameEmbedding)
                .withParams(JSON.toJSONString(searchParams));
        if (StringUtils.isNotBlank(partition)) {
            searchParamBuilder.withPartitionNames(Lists.newArrayList(partition));
        }
        SearchParam searchParam = searchParamBuilder.build();
        R<SearchResults> respSearch = milvusClient.search(searchParam);
        if (respSearch.getStatus() == R.Status.UnexpectedError.getCode()) {
            String exceptionMessage = respSearch.getException().getMessage();
            if (StringUtils.isNotEmpty(exceptionMessage) && exceptionMessage.contains(EXCEPTION_COLLECTION_NOT_LOADED)) {
                log.info("similaritySearch collection not loaded, start reload collection: " + collection);
                loadCollection();
                respSearch = milvusClient.search(searchParam);
            }
        }
        List<Document> documents = Lists.newArrayList();
        if (respSearch.getData() != null) {
            SearchResultsWrapper wrapperSearch = new SearchResultsWrapper(respSearch.getData().getResults());
            List<SearchResultsWrapper.IDScore> idScores = wrapperSearch.getIDScore(0);
            for (SearchResultsWrapper.IDScore idScore : idScores) {
                Document document = new Document();
                document.setScore((double) idScore.getScore());
                Map<String, Object> fieldValues = idScore.getFieldValues();
                document.setUniqueId(MapUtils.getString(fieldValues, fieldNameUniqueId));
                document.setPageContent(MapUtils.getString(fieldValues, fieldNamePageContent));
                documents.add(document);
            }
        }
        return documents;
    }

    /**
     * init会在Collection不存在的情况下创建Milvus的Collection，
     * 1. 根据embedding模型结果维度创建embeddings向量字段
     * 2. 创建int64的content_id字段
     * 3. 创建长度8192的row_content字符串字段
     * 4. 对embeddings字段创建索引
     * 如果需要自定义Collection，请按照上面的字段类型规范进行提前创建:
     * 1. 你线下创建Collection(可以修改字段的长度，字段名称，但字段类型不可变），建议以content_id作为主键，这样在文档更新的时候可以覆盖
     * 2. 你同时需要创建Index
     */
    public void init(Embeddings embedding) {
        try {
            // 如果没有Collection, 则创建Collection
            if (!hasCollection()) {
                // 创建Collection
                createCollection(embedding);
                // 创建索引
                createIndex();
            }
            // 如果没有分区, 则创建分区
            if (StringUtils.isNotBlank(partition) && !hasPartition()) {
                createPartition();
            }
        } catch (Exception e) {
            log.error("init milvus failed", e);
        }
    }

    /**
     * 加载指定参数 (指定参数不存在使用默认参数)
     * @return MilvusParam
     */
    private MilvusParam loadParam() {
        if (milvusParam == null) {
            milvusParam = new MilvusParam();
        }
        return milvusParam;
    }

    /**
     * 创建Collection
     * @param embedding 参考embedding模型
     */
    public void createCollection(Embeddings embedding) {
        MilvusParam param = loadParam();
        MilvusParam.InitParam initParam = param.getInitParam();
        String fieldNameUniqueId = param.getFieldNameUniqueId();
        String fieldNamePageContent = param.getFieldNamePageContent();
        String fieldNameEmbedding = param.getFieldNameEmbedding();
        int embeddingsDimension = initParam.getFieldEmbeddingsDimension();
        if (initParam.getFieldEmbeddingsDimension() <= 0) {
            //使用embedding进行embedding确认向量的维度数
            List<Document> embeddingDocuments = embedding.embedTexts(Lists.newArrayList("test"));
            Document document = embeddingDocuments.get(0);
            embeddingsDimension = document.getEmbedding().size();
        }

        FieldType fieldTypeId = null;
        FieldType fieldTypeUniqueId;
        if (initParam.isFieldUniqueIdAsPrimaryKey()) {
            fieldTypeUniqueId = FieldType.newBuilder()
                    .withName(fieldNameUniqueId)
                    .withDataType(DataType.Int64)
                    .withPrimaryKey(true)
                    .withAutoID(false)
                    .build();
        } else {
            fieldTypeId = FieldType.newBuilder()
                    .withName("_id")
                    .withDataType(DataType.Int64)
                    .withPrimaryKey(true)
                    .withAutoID(true)
                    .build();
            fieldTypeUniqueId = FieldType.newBuilder()
                    .withName(fieldNameUniqueId)
                    .withDataType(DataType.Int64)
                    .build();
        }

        FieldType fieldTypePageContent = FieldType.newBuilder()
                .withName(fieldNamePageContent)
                .withDataType(DataType.VarChar)
                .withMaxLength(initParam.getFieldPageContentMaxLength())
                .build();
        FieldType fieldTypeEmbedding = FieldType.newBuilder()
                .withName(fieldNameEmbedding)
                .withDataType(DataType.FloatVector)
                .withDimension(embeddingsDimension)
                .build();
        CreateCollectionParam.Builder createCollectionReqBuilder = CreateCollectionParam.newBuilder()
                .withCollectionName(collection)
                .withDescription("init by alibaba-langengine")
                .withShardsNum(initParam.getShardsNum())
                .addFieldType(fieldTypeUniqueId)
                .addFieldType(fieldTypePageContent)
                .addFieldType(fieldTypeEmbedding)
                .withEnableDynamicField(true);
        if (fieldTypeId != null) {
            createCollectionReqBuilder.addFieldType(fieldTypeId);
        }
        CreateCollectionParam createCollectionReq = createCollectionReqBuilder.build();
        try {
            R<RpcStatus> collection = milvusClient.createCollection(createCollectionReq);
            log.info("createCollection status=" + collection.getStatus() + ", data=" + JSON.toJSONString(collection.getData()));
        } catch (Exception e) {
            log.error("createCollection failed", e);
        }
    }

    private void createIndex() {
        try {
            MilvusParam param = loadParam();
            MilvusParam.InitParam initParam = param.getInitParam();
            String fieldNameEmbedding = param.getFieldNameEmbedding();
            R<RpcStatus> result = milvusClient.createIndex(
                    CreateIndexParam.newBuilder()
                            .withCollectionName(collection)
                            .withFieldName(fieldNameEmbedding)
                            .withIndexType(initParam.getIndexEmbeddingsIndexType())
                            .withMetricType(initParam.getIndexEmbeddingsMetricType())
                            .withExtraParam(JSON.toJSONString(initParam.getIndexEmbeddingsExtraParam()))
                            .withSyncMode(Boolean.FALSE)
                            .build()
            );
            log.info("createIndex status=" + result.getStatus() + ", data=" + JSON.toJSONString(result.getData()));
        } catch (Exception e) {
            log.error("createIndex failed", e);
        }
    }

    private void loadCollection() {
        try {
            R<RpcStatus> result = milvusClient.loadCollection(
                    LoadCollectionParam.newBuilder()
                            .withCollectionName(collection)
                            .build()
            );
            log.info("loadCollection status=" + result.getStatus() + ", data=" + JSON.toJSONString(result.getData()));
        } catch (Exception e) {
            log.error("loadCollection failed", e);
        }
    }

    public boolean hasCollection() {
        // 创建检查集合是否加载的参数
        HasCollectionParam hasCollectionParam = HasCollectionParam.newBuilder()
                .withCollectionName(collection)
                .build();

        // 检查集合是否存在
        R<Boolean> result = milvusClient.hasCollection(hasCollectionParam);
        log.info("hasCollection result status=" + result.getStatus() + ", data=" + JSON.toJSONString(result.getData()));
        return result.getData();
    }

    private boolean hasPartition() {
        try {
            R<Boolean> result = milvusClient.hasPartition(
                    HasPartitionParam.newBuilder()
                            .withCollectionName(collection)
                            .withPartitionName(partition)
                            .build()
            );
            log.info("checkPartition status=" + result.getStatus() + ", data=" + JSON.toJSONString(result.getData()));
            return result.getData();
        } catch (Exception e) {
            log.error("checkPartition failed", e);
        }
        return false;
    }

    public void createPartition() {
        try {
            R<RpcStatus> result = milvusClient.createPartition(
                    CreatePartitionParam.newBuilder()
                            .withCollectionName(collection)
                            .withPartitionName(partition)
                            .build()
            );
            log.info("createPartition status=" + result.getStatus() + ", data=" + JSON.toJSONString(result.getData()));
        } catch (Exception e) {
            log.error("createPartition failed", e);
        }
    }

    protected void dropCollection() {
        R<RpcStatus> result = milvusClient.dropCollection(
                DropCollectionParam.newBuilder()
                        .withCollectionName(collection)
                        .build()
        );
        log.info("dropCollection status=" + result.getStatus() + ", data=" + JSON.toJSONString(result.getData()));
    }

    private boolean hasLoadCollection() {
        try {
            // 你可以检查加载状态
            GetLoadStateParam param = GetLoadStateParam.newBuilder()
                    .withCollectionName(collection)
                    .build();
            R<GetLoadStateResponse> response = milvusClient.getLoadState(param);
            log.info("hasLoadCollection result status=" + response.getStatus() + ", state=" + JSON.toJSONString(response.getData().getState()));
            return response.getData().getState() == LoadState.LoadStateLoaded;
        } catch (Exception e) {
            log.error("hasLoadCollection failed", e);
        }
        return false;
    }

    protected void relaseCollection() {
        R<RpcStatus> release = milvusClient.releaseCollection(
                ReleaseCollectionParam.newBuilder()
                        .withCollectionName(collection)
                        .build());
        log.info("release status=" + release.getStatus() + ", data=" + JSON.toJSONString(release.getData()));
    }
}
