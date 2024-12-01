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
package com.alibaba.langengine.agentframework.model.domain;

import lombok.Data;

/**
 * 知识库参数
 *
 * @author xiaoxuan.lp
 */
@Data
public class KnowledgeParam {

    /**
     * 参数名称
     */
    private String paramName;

    /**
     * 参数描述
     */
    private String paramDesc;

    /**
     * 参数数据类型
     */
    private String dataType;

    /**
     * 是否必填
     */
    private Boolean isRequired;

    /**
     * 赋值方式
     */
    private String assignPattern;

    /**
     * 值
     */
    private String value;

    /**
     * 是否多字段搜索
     */
    private Boolean multiValue;
}
