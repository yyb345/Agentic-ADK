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

/**
 * 大模型生成的OpenSearch的语句片段
 *
 * @author pingkuang 2023/11/20
 */
@Data
public class OpenSearchSegment {

    private String op;

    private List<List<String>> result;
}
