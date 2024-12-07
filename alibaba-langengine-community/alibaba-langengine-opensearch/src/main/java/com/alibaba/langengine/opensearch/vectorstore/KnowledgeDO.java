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

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
public class KnowledgeDO {

    private Long id;

    private String namespace;

    private String contentId;

    private Integer type;

    private String content;

    private Integer idx;

    private String rowContent;

    private Double distance;

    private String metadata;

    Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("id", Optional.ofNullable(id).map(String::valueOf).orElse(null));
        map.put("namespace", namespace);
        map.put("content_id", Optional.ofNullable(contentId).map(String::valueOf).orElse(null));
        map.put("type", Optional.ofNullable(type).map(String::valueOf).orElse(null));
        map.put("content", content);
        map.put("idx", Optional.ofNullable(idx).map(String::valueOf).orElse(null));
        map.put("row_content", rowContent);
        map.put("distance", Optional.ofNullable(distance).map(String::valueOf).orElse(null));
        map.put("metadata", metadata);
        return map;
    }

}