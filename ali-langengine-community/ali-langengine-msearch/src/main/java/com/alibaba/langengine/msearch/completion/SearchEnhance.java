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

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class SearchEnhance {

    @JSONField(name = "vector_recall_ratio")
    private Double vectorRecallRatio = 0.7d;

    @JSONField(name = "acc_sorting")
    private Integer accSorting = 200;

    @JSONField(name = "search_size")
    private Integer searchSize = 3;

    @JSONField(name = "custom_sorting")
    private String customSorting = "{\"default\": {\"features\": [{\"name\": \"vector_index\", \"weights\": 0.5, \"threshold\": 0, \"norm\": true}, {\"name\": \"static_value\", \"field\": \"_rc_t_score\", \"weights\": 0.04, \"threshold\": 0, \"norm\": false}, {\"name\": \"query_match_ratio\", \"field\": \"question\", \"weights\": 0.5, \"threshold\": 0.0, \"norm\": false}, {\"name\": \"query_match_ratio\", \"field\": \"sim_question\", \"weights\": 0.5, \"threshold\": 0.0, \"norm\": false}, {\"name\": \"char_edit_similarity\", \"field\": \"question\", \"weights\": 0.6, \"threshold\": 0.0, \"norm\": false}, {\"name\": \"char_lcs_match_ratio\", \"field\": \"question\", \"weights\": 0.25, \"threshold\": 0.0, \"norm\": false}, {\"name\": \"overlap_coefficient\", \"weights\": 0.1, \"field\": \"answer\", \"threshold\": 0, \"norm\": false}], \"aggregate_algo\": \"weight_avg\"}}";

    private String filters = "";
}
