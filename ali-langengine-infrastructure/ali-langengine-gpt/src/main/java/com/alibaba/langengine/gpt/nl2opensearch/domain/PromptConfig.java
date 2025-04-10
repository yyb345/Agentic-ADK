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
package com.alibaba.langengine.gpt.nl2opensearch.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Prompt的配置信息
 *
 * @author pingkuang 2023/11/20
 */
@Data
@Accessors(chain = true)
public class PromptConfig {

    /**
     * 索引描述
     */
    private String queryDescText;

    /**
     * 过滤描述
     */
    private String filterDescText;

    /**
     * 排序描述
     */
    private String sortDectText;

    /**
     * 样例述
     */
    private String sampleText;

    /**
     * 角色
     */
    private String character;

    /**
     * 查询字段描述
     */
    private String fetchFieldsDescText;
}
