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

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * OpenSearch的配置信息
 * 如果只是用于生成Query语句，不需要执行检索，以下参数除appName、fetchFields外可以不填
 *
 * @author pingkuang 2023/11/20
 */
@Data
@Accessors(chain = true)
public class OpenSearchConfig {

    /**
     * OpenSearch应用名称
     */
    private String appName;

    /**
     * accessKey
     */
    private String accessKey;

    /**
     * secret
     */
    private String secret;

    /**
     * 应用的API访问地址.
     */
    private String host = "http://opensearch-cn-internal.aliyuncs.com";

    /**
     * 返回字段
     */
    private List<String> fetchFields;

    /**
     * 默认返回条数
     */
    private Integer defaultReturnRecords = 10;

    /**
     * 最大返回条数
     */
    private Integer maxReturnRecords = 10;
}
