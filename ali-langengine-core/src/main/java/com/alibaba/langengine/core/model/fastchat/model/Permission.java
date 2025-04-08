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
package com.alibaba.langengine.core.model.fastchat.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Permission {
    /**
     * An identifier for this model permission
     */
    public String id;

    /**
     * The type of object returned, should be "model_permission"
     */
    public String object;

    /**
     * The creation time in epoch seconds.
     */
    public long created;

    @JsonProperty("allow_create_engine")
    public boolean allowCreateEngine;

    @JsonProperty("allow_sampling")
    public boolean allowSampling;

    @JsonProperty("allow_log_probs")
    public boolean allowLogProbs;

    @JsonProperty("allow_search_indices")
    public boolean allowSearchIndices;

    @JsonProperty("allow_view")
    public boolean allowView;

    @JsonProperty("allow_fine_tuning")
    public boolean allowFineTuning;

    public String organization;

    public String group;

    @JsonProperty("is_blocking")
    public boolean isBlocking;

}
