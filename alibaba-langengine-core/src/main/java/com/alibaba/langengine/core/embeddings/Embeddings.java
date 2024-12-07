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
package com.alibaba.langengine.core.embeddings;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.util.JacksonUtils;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 嵌入模型基类
 *
 * @author xiaoxuan.lp
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property= JacksonUtils.PROPERTY_CLASS_NAME)
public abstract class Embeddings {

    public List<Document> embedTexts(List<String> texts) {
        List<Document> documents = texts.stream().map(text -> {
            Document document = new Document();
            document.setPageContent(text);
            return document;
        }).collect(Collectors.toList());
        return embedDocument(documents);
    }

    /**
     * 用到的模型类型
     *
     * @return
     */
    public abstract String getModelType();

    /**
     * 嵌入搜索文档
     *
     * @param documents
     * @return
     */
    public abstract List<Document> embedDocument(List<Document> documents);


    /**
     * 嵌入查询文本
     *
     * @param text
     * @param recommend
     * @return
     */
    public abstract List<String> embedQuery(String text, int recommend);
}