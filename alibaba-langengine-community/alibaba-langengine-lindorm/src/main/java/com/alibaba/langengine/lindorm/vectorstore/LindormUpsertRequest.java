package com.alibaba.langengine.lindorm.vectorstore;

import lombok.Builder;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Data
@Builder
public class LindormUpsertRequest {
    private String id;

    @NotNull(message = "Index name can not be null")
    private String indexName;

    @NotNull(message = "Embedding Model Type can not be null")
    private Long modelType;

    @NotNull(message = "Vector can not be null")
    private List<Double> vector;

    @NotNull(message = "Chunk index can not be null")
    private Integer chunkIdx;

    private String pageContent;

    private Map<String, Object> metadata;

    public IndexRequest getUpsertRequest() {
        IndexRequest indexRequest = new IndexRequest(indexName);
        String documentId = Optional.ofNullable(id)
                .map(String::valueOf)
                .orElse(UUID.randomUUID().toString());

        Map<String, Object> fieldMap = new HashMap<>();
        fieldMap.put(LindormConstants.LINDORM_DEFAULT_VECTOR_FIELD_NAME, vector);
        fieldMap.put("type", modelType);
        if (chunkIdx != null) {
            fieldMap.put("index", chunkIdx);
        }
        fieldMap.put("metadata", Optional.ofNullable(metadata).orElse(new HashMap<>()));
        fieldMap.put("page_content", Optional.ofNullable(pageContent).orElse(""));
        indexRequest.source(fieldMap);
        indexRequest.id(documentId);
        return indexRequest;
    }
}
