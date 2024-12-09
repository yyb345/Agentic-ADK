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
package com.alibaba.langengine.msearch.completion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * CompletionRequest
 *
 * @author xiaoxuan.lp
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CompletionRequest {

    /**
     * 包括prompt，session_id，plugin_schemas，max_tokens，stream，tenant_id，app_id，round_id
     * plugin_schemas参数：三级参数（search_enhance）、四级参数（vector_recall_ratio、acc_sorting、filters、custom_sorting）
     */
    Map<String, Object> input;

    /**
     * 包括top_p，temperature，allow_direct_answer，num_doc
     */
    private Map<String, Object> parameters;

    /**
     * debug
     */
    private boolean debug = false;
}
