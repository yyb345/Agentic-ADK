package com.alibaba.langengine.lindorm.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.search.SearchHit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Data
@Slf4j
public class LindormDB extends VectorStore {
    private Embeddings embedding;
    private RestHighLevelClient client;
    private String indexName;
    private final Map<String, Boolean> indexExistenceCache = new ConcurrentHashMap<>();

    public LindormDB(String indexName) {
        this(indexName, LindormConfig.LINDORM_USERNAME, LindormConfig.LINDORM_PASSWORD,
                LindormConfig.LINDORM_ENDPOINT, LindormConfig.LINDORM_SEARCH_PORT);
    }

    public LindormDB(String indexName, String username, String password, String endpoint, Integer port) {
        this.indexName = indexName;
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(endpoint, port));
        restClientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        });
        client = new RestHighLevelClient(restClientBuilder);
    }


    /**
     * Create index
     *
     * @param createIndexRequest
     */
    public void createIndex(LindormCreateRequest createIndexRequest) {
        try {
            CreateIndexResponse createResponse = client.indices().create(createIndexRequest.getCreateIndexRequest(), RequestOptions.DEFAULT);
            if (createResponse.isAcknowledged()) {
                log.info("Index {} created successfully", createIndexRequest.getIndexName());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check if index exists
     *
     * @param indexName
     * @return
     * @throws IOException
     */
    public boolean indexExists(String indexName) throws IOException {
        GetIndexRequest request = new GetIndexRequest(indexName);
        return client.indices().exists(request, RequestOptions.DEFAULT);
    }

    /**
     * Ensure index exists, if not, create the index with default hnsw index and parameters
     *
     * @param indexName
     * @param dimension
     * @throws IOException
     */
    protected void ensureIndexExists(String indexName, Integer dimension) throws IOException {
        if (!indexExistenceCache.containsKey(indexName)) {
            synchronized (this) {
                if (!indexExistenceCache.containsKey(indexName)) {
                    boolean exists = indexExists(indexName);
                    if (!exists) {
                        LindormCreateRequest.HNSWParams hnswParams = LindormCreateRequest.HNSWParams.builder().build();
                        LindormCreateRequest createRequest =
                                LindormCreateRequest.builder()
                                        .indexName(indexName)
                                        .dimension(dimension)
                                        .indexParams(hnswParams).build();
                        createIndex(createRequest);
                    }
                    indexExistenceCache.put(indexName, true);
                }
            }
        }
    }

    /**
     * Add documents to the index, if index not exists, create it
     *
     * @param documents
     * @return
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if(documents == null || documents.isEmpty()) {
            return;
        }
        documents = embedding.embedDocument(documents);
        if(documents.isEmpty()) {
            return;
        }
        String indexName = getIndexName();
        Integer dimension = documents.get(0).getEmbedding().size();
        try {
            ensureIndexExists(indexName, dimension);
            // Build Bulk Request
            BulkRequest bulkRequest = new BulkRequest();
            for (Document document : documents) {
                LindormUpsertRequest upsertRequest = LindormUpsertRequest.builder()
                        .indexName(indexName)
                        .id(document.getUniqueId())
                        .modelType(Long.valueOf(embedding.getModelType()))
                        .vector(document.getEmbedding())
                        .chunkIdx(document.getIndex())
                        .pageContent(document.getPageContent())
                        .metadata(document.getMetadata())
                        .build();
                bulkRequest.add(upsertRequest.getUpsertRequest());
            }
            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (bulkResponse.hasFailures()) {
                log.error("Error while adding documents: {}", bulkResponse.buildFailureMessage());
            }
            log.info("Added {} documents", bulkResponse.getItems().length);
        } catch (IOException e) {
            log.error("Error while adding documents", e);
            throw new RuntimeException("Failed to add documents", e);
        }
    }

    /**
     * Search documents by vector
     *
     * @param query
     * @param k
     * @param maxDistanceValue
     * @param type
     * @return
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        List<String> embeddingStrings = embedding.embedQuery(query, k);
        if (embeddingStrings.isEmpty() || !embeddingStrings.get(0).startsWith("[")) {
            return new ArrayList<>();
        }
        String embeddingString = embeddingStrings.get(0);
        List<String> embeddings = JSON.parseArray(embeddingString, String.class);

        LindormSearchRequest searchRequest = LindormSearchRequest.builder()
                .indexName(getIndexName())
                .topK(k)
                .vector(embeddings.stream().map(Float::parseFloat).collect(Collectors.toList()))
                .type(type)
                .build();
        try {
            SearchResponse searchResponse = client.search(searchRequest.getSearchRequest(), RequestOptions.DEFAULT);
            List<Document> documentList = parseSearchResponse(searchResponse);
            if (maxDistanceValue != null) {
                documentList = documentList.stream()
                        .filter(document -> document.getScore() != null && document.getScore() < maxDistanceValue)
                        .collect(Collectors.toList());
            }
            return documentList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected List<Document> parseSearchResponse(SearchResponse searchResponse) {
        List<Document> documents = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Document document = new Document();
            document.setUniqueId(hit.getId());
            document.setScore((double)hit.getScore());
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            if (sourceAsMap != null) {
                document.setPageContent((String) sourceAsMap.get("page_content"));
                Object index = sourceAsMap.get("index");
                if (index instanceof Integer) {
                    document.setIndex((Integer) index);
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> metadata = (Map<String, Object>) sourceAsMap.get("metadata");
                document.setMetadata(metadata);
                @SuppressWarnings("unchecked")
                List<Double> embedding = (List<Double>) sourceAsMap.get("vector");
                document.setEmbedding(embedding);
            }
            documents.add(document);
        }
        return documents;
    }

    /**
     * Request to build index
     *
     * @param indexName
     * @param fieldName
     * @param removeOldIndex
     * @return
     * @throws IOException
     */
    public HttpEntity requestBuildIndex(String indexName, String fieldName, boolean removeOldIndex) throws IOException {
        Request request = new Request("POST", "/_plugins/_vector/index/build");
        String jsonString = String.format(
                "{ \"indexName\": \"%s\", \"fieldName\": \"%s\", \"removeOldIndex\": \"%s\" }",
                indexName, fieldName, removeOldIndex
        );
        request.setJsonEntity(jsonString);
        Response response = client.getLowLevelClient().performRequest(request);
        return response.getEntity();
    }

    /**
     * Get index build task status
     *
     * @param indexName
     * @param fieldName
     * @return
     * @throws IOException
     */
    public HttpEntity getIndexBuildTaskStatus(String indexName, String fieldName) throws IOException {
        Request taskStatusRequest = new Request("GET", "/_plugins/_vector/index/tasks");
        String jsonString = String.format(
                "{ \"indexName\": \"%s\", \"fieldName\": \"%s\", \"taskIds\": \"[%s]\" }",
                indexName, fieldName, "default_"+indexName+"_"+fieldName);
        taskStatusRequest.setJsonEntity(jsonString);
        Response response = client.getLowLevelClient().performRequest(taskStatusRequest);
        return response.getEntity();
    }

    /**
     * Build index and Wait. Offline operation
     *
     * @param indexName: index name
     * @param fieldName: vector field name
     * @param removeOldIndex: whether remove old index when build
     * @param interval: interval between each request
     * @param timeout: timeout
     * @throws IOException
     * @throws InterruptedException
     */
    public void buildIndex(String indexName, String fieldName, Boolean removeOldIndex, Integer interval, Long timeout)
            throws IOException, InterruptedException, TimeoutException {
        HttpEntity buildResponse = requestBuildIndex(indexName, fieldName, removeOldIndex);
        System.out.println("Index build requested: " + buildResponse);
        long startTime = System.currentTimeMillis();

        while (true) {
            Thread.sleep(interval);
            HttpEntity statusResponse = getIndexBuildTaskStatus(indexName, fieldName);
            String responseBody = EntityUtils.toString(statusResponse);
            System.out.println("Current build status: " + responseBody);
            if (responseBody.contains("FINISH")) {
                System.out.println("Index build completed successfully.");
                return;
            } else if (responseBody.contains("FAIL")) {
                throw new IOException("Index build failed: " + responseBody);
            }
            if (System.currentTimeMillis() - startTime > timeout) {
                throw new TimeoutException("Index build timed out after " + (timeout / 1000) + " seconds");
            }
        }
    }

    /**
     * Build index until success
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public void buildIndex() throws IOException, InterruptedException, TimeoutException {
        buildIndex(getIndexName(), LindormConstants.LINDORM_DEFAULT_VECTOR_FIELD_NAME, true, 1000, 600000L);
    }
}
