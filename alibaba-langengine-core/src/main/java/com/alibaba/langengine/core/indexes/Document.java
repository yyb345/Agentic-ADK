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
package com.alibaba.langengine.core.indexes;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 文档类
 *
 * @author xiaoxuan.lp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Document {

    public Document(String pageContent, Map<String, Object> metadata) {
        this.pageContent = pageContent;
        this.metadata = metadata;
    }

    /**
     * 文档信息的摘要内容，总结内容，或者是问题；
     * 有时候如果pageContent的内容太大，会导致向量检索的准确度下降；向量也比较稀疏；
     * 所以需要一个摘要内容，业务可以判断使用摘要还是使用pageContent
     * 因为metaData元数据中还可能有一些其他的信息，所以就放在metaData用于向量检索了
     */
    private String summary;

    /**
     * 当前存在总结内容的时候，必定是存在完整的内容的，因此如果说业务要用Document做一些逻辑的时候
     * 如果存放摘要内容和完整内容，可以使用这两个字段，如果不需要可以进行忽略
     */
    private String wholeContent;

    /**
     * 唯一字符串
     */
    private String uniqueId;

    @JSONField(name = "page_content")
    private String pageContent;

    @JSONField(name = "metadata")
    private Map<String, Object> metadata;

    private List<Double> embedding;

    /**
     * chunk index
     */
    private Integer index;

    /**
     * 计算得分
     */
    private Double score;

    /**
     * 文档类型分类
     */
    private String category;

    public Boolean hasMetadata() {
        return MapUtils.isNotEmpty(metadata);
    }

    public Boolean hasCategory() {
        return StringUtils.isNotEmpty(category);
    }
}
