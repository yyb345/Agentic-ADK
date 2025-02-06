package com.alibaba.langengine.lindorm.vectorstore;


import com.google.gson.Gson;
import lombok.Builder;
import lombok.Data;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class LindormSearchRequest {
    @NotNull(message = "topK can not be null")
    private Integer topK;

    @NotNull(message = "Index name can not be null")
    private String indexName;

    @NotNull(message = "Vector can not be null")
    private List<Float> vector;

    @Builder.Default
    private String vectorFieldName = LindormConstants.LINDORM_DEFAULT_VECTOR_FIELD_NAME;

    private Integer type;

    private List<String> includesFields;
    private List<String> excludesFields;

    @Builder.Default
    private ParamsMap searchParams = HNSWSearchParams.builder().build();

    @Data
    @Builder
    public static class HNSWSearchParams implements ParamsMap {
        @Builder.Default
        private String filterType = LindormConstants.LINDORM_HYBRID_SEARCH_EFFICIENT_FILTER;
        @Builder.Default
        private Integer efSearch = LindormConstants.LINDORM_DEFAULT_EF_SEARCH;
        @Builder.Default
        private Float minScore = 0f;

        @Override
        public Map<String, Object> toParamsMap() {
            Map<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("min_score", minScore);
            paramsMap.put("filter_type", filterType);
            paramsMap.put("ef_search", efSearch);
            return paramsMap;
        }
    }

    @Data
    @Builder
    public static class IVFPQSearchParams implements ParamsMap {
        @Builder.Default
        private String filterType = LindormConstants.LINDORM_HYBRID_SEARCH_EFFICIENT_FILTER;
        @Builder.Default
        private Integer reorderFactor = LindormConstants.LINDORM_DEFAULT_REORDER_FACTOR;
        @Builder.Default
        private Boolean clientRefactor = true;
        @Builder.Default
        private Float minScore = 0f;

        private String kExpandScope;
        private Integer nprobe;

        @Override
        public Map<String, Object> toParamsMap() {
            Map<String, Object> paramsMap = new HashMap<>();
            if (minScore > 0) {
                paramsMap.put("min_score", minScore);
            }
            if (nprobe != null) {
                paramsMap.put("nprobe", nprobe);
            }
            paramsMap.put("filter_type", filterType);
            paramsMap.put("reorder_factor", reorderFactor);
            if (filterType.equals(LindormConstants.LINDORM_HYBRID_SEARCH_EFFICIENT_FILTER) && kExpandScope != null) {
                paramsMap.put("k_expand_scope", kExpandScope);
            }
            return paramsMap;
        }
    }


    public SearchRequest getSearchRequest() {
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        Map<String, Object> knnBody = new HashMap<>();
        knnBody.put("vector", vector);
        knnBody.put("k", topK);
        Map<String, Object> queryBody = Collections.singletonMap("knn", Collections.singletonMap(vectorFieldName, knnBody));
        searchSourceBuilder.query(QueryBuilders.wrapperQuery(new Gson().toJson(queryBody)));
        Map<String, String> ext = new HashMap<>();
        searchParams.toParamsMap().forEach((k, v) -> ext.put(k, v != null ? v.toString() : ""));
        searchSourceBuilder.ext(Collections.singletonList(new LVectorExtBuilder("lvector", ext)));
        searchRequest.source(searchSourceBuilder);
        searchRequest.indices(indexName);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.fetchSource(new String[]{"type", "index", "metadata", "page_content"}, null);
        if (type != null) {
            sourceBuilder.query(QueryBuilders.termQuery("type", type));
        }
        searchRequest.source(sourceBuilder);
        return searchRequest;
    }
}
