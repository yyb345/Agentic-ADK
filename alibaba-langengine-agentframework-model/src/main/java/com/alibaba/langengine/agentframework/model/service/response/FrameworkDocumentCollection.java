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
package com.alibaba.langengine.agentframework.model.service.response;

import com.alibaba.langengine.agentframework.model.constant.KnowledgeConstants;
import com.alibaba.langengine.agentframework.model.domain.ContextSerialization;
import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class FrameworkDocumentCollection<T> implements ContextSerialization {

    private String knowledgeType;

    private List<T> documents;

    @Override
    public String toString() {
        if (KnowledgeConstants.KNOWLEDGE_TYPE_DOCUMENT.equals(knowledgeType)) {
            return documents.stream()
                .map(document -> {
                    if (document != null && document instanceof Map) {
                        Object pageContent = ((Map<String, Object>)document).get("pageContent");
                        return pageContent != null ? pageContent.toString() : null;
                    }
                    if (document != null && document instanceof String) {
                        return document.toString();
                    }

                    if (document instanceof Document) {
                        return ((Document)document).getPageContent();
                    }

                    return null;
                })
                .filter(e -> e != null)
                .collect(Collectors.joining("\n"));
        } else if (KnowledgeConstants.KNOWLEDGE_TYPE_TABLE.equals(knowledgeType)) {
            return documents.stream()
                .map(document -> {
                        if (document != null && document instanceof Map) {
                            Object metadata = ((Map<String, Object>)document).get("metadata");
                            return metadata != null ? JSON.toJSONString(metadata) : null;
                        }
                        if (document != null && document instanceof String) {
                            return document.toString();
                        }

                        if (document instanceof Document) {
                            return ((Document)document).getPageContent();
                        }
                        return null;
                    }
                ).filter(e -> e != null)
                .collect(Collectors.joining("\n"));
        }

        return null;

    }
}