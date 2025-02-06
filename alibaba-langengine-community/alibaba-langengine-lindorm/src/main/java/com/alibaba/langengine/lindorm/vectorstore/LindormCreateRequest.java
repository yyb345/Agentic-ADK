package com.alibaba.langengine.lindorm.vectorstore;

import lombok.Builder;
import lombok.Data;
import org.elasticsearch.client.indices.CreateIndexRequest;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class LindormCreateRequest {
    @NotNull(message = "Index name cannot be null")
    private String indexName;

    // Setting parts
    @Builder.Default
    private Integer numberOfShards = LindormConstants.LINDORM_DEFAULT_NUM_OF_SHARDS;

    @Builder.Default
    private boolean knn = true;

    private boolean knnOfflineConstruction;

    @Builder.Default
    Boolean excludeVector = false;

    // properties in mapping
    @Builder.Default
    private String vectorFieldName = LindormConstants.LINDORM_DEFAULT_VECTOR_FIELD_NAME;

    @NotNull(message = "Dimension cannot be null")
    private Integer dimension;

    @Builder.Default
    private String type = LindormConstants.LINDORM_DEFAULT_VECTOR_TYPE;

    @Builder.Default
    private String dataType = LindormConstants.LINDORM_DEFAULT_DATA_TYPE;

    @Builder.Default
    private String indexMethod = LindormConstants.LINDORM_DEFAULT_METHOD_NAME;

    @Builder.Default
    private String engine = LindormConstants.LINDORM_DEFAULT_ENGINE_TYPE;

    @Builder.Default
    private String spaceType = LindormConstants.LINDORM_DEFAULT_SPACE_TYPE;

    @Builder.Default
    private ParamsMap indexParams = HNSWParams.builder().build();

    @Data
    @Builder
    public static class HNSWParams implements ParamsMap {
        @Builder.Default
        private Integer efConstruction = LindormConstants.LINDORM_EF_CONSTRUCTION;

        @Builder.Default
        private Integer M = LindormConstants.LINDORM_DEFAULT_M ;

        public Map<String, Object> toParamsMap() {
            Map<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("ef_construction", efConstruction);
            paramsMap.put("m", M);
            return paramsMap;
        }
    }

    @Data
    @Builder
    public static class IVFPQParams implements ParamsMap {
        @NotNull
        public Integer m;

        @Builder.Default
        public Integer nlist = LindormConstants.LINDORM_DEFAULT_NLIST;

        @Builder.Default
        public Boolean centroidsUseHNSW = true;

        @Builder.Default
        public Integer centroidsHNSWM = LindormConstants.LINDORM_DEFAULT_CENTROIDS_HNSW_M;

        @Builder.Default
        public Integer centroidsHNSWEfConstruct = LindormConstants.LINDORM_DEFAULT_CENTROIDS_HNSW_EF_CONSTRUCT;

        @Builder.Default
        public Integer centroidsHNSWEFSearch = LindormConstants.LINDORM_DEFAULT_CENTROIDS_HNSW_EF_SEARCH;

        public Map<String, Object> toParamsMap() {
            Map<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("m", m);
            paramsMap.put("nlist", nlist);
            paramsMap.put("centroids_use_hnsw", centroidsUseHNSW);
            paramsMap.put("centroids_hnsw_m", centroidsHNSWM);
            paramsMap.put("centroids_hnsw_ef_construct", centroidsHNSWEfConstruct);
            paramsMap.put("centroids_hnsw_ef_search", centroidsHNSWEFSearch);
            return paramsMap;
        }
    }

    public CreateIndexRequest getCreateIndexRequest() {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
        // Create settings
        Map<String, Object> indexMap = new HashMap<>();
        indexMap.put("number_of_shards", numberOfShards);
        indexMap.put("knn", true);
        if (indexMethod.equals(LindormConstants.LINDORM_VECTOR_METHOD_IVFPQ)) {
            indexMap.put("knn.offline.construction", true);
        }
        Map<String, Object> settings = new HashMap<>();
        settings.put("index", indexMap);
        createIndexRequest.settings(settings);

        // Create map for method
        Map<String, Object> method = new HashMap<>();
        method.put("engine", engine);
        method.put("name", indexMethod);
        method.put("space_type", spaceType);
        method.put("parameters", indexParams.toParamsMap());

        // Create a map for "vector"
        Map<String, Object> vector = new HashMap<>();
        vector.put("type", type);
        vector.put("dimension", dimension);
        vector.put("data_type", dataType);
        vector.put("method", method);

        // Create a map for the properties
        Map<String, Object> properties = new HashMap<>();
        properties.put(vectorFieldName, vector);

        // Create the mappings map
        Map<String, Object> mappings = new HashMap<>();
        mappings.put("properties", properties);
        if (excludeVector) {
            Map<String, Object> source = Collections.singletonMap("excludes", new String[] {vectorFieldName});
            mappings.put("_source", source);
        }
        createIndexRequest.mapping(mappings);
        return createIndexRequest;
    }
}
