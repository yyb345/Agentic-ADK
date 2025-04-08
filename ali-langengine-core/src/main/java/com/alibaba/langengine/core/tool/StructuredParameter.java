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
package com.alibaba.langengine.core.tool;

import lombok.Data;

import java.util.Map;

/**
 * 结构化参数
 *
 * @author xiaoxuan.lp
 */
@Data
public class StructuredParameter {

    /**
     * 参数名称，例如：search_query
     */
    private String name;

    /**
     * 参数描述，例如：搜索关键词或短语
     */
    private String description;

    /**
     * 是否必填
     */
    private Boolean required;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 参数schema
     */
    private Map<String, Object> schema;
}
