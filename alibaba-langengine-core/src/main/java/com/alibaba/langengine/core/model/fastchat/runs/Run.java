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
package com.alibaba.langengine.core.model.fastchat.runs;

import com.alibaba.langengine.core.model.fastchat.assistants.Tool;
import com.alibaba.langengine.core.model.fastchat.common.LastError;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Run {

    private String id;

    private String object;

    @JsonProperty("created_at")
    private Integer createdAt;

    @JsonProperty("thread_id")
    private String threadId;

    @JsonProperty("assistant_id")
    private String assistantId;

    /**
     * The status of the run,
     * which can be either
     * queued, in_progress, requires_action, cancelling, cancelled, failed, completed, or expired
     */
    private String status;

    @JsonProperty("required_action")
    private RequiredAction requiredAction;

    @JsonProperty("last_error")
    private LastError lastError;

    @JsonProperty("expires_at")
    private Integer expiresAt;

    @JsonProperty("started_at")
    private Integer startedAt;
    
    @JsonProperty("cancelled_at")
    private Integer cancelledAt;

    @JsonProperty("failed_at")
    private Integer failedAt;
    
    @JsonProperty("completed_at")
    private Integer completedAt;

    private String model;

    private String instructions;

    private List<Tool> tools;
    
    @JsonProperty("file_ids")
    private List<String> fileIds;

    private Map<String, String> metadata;
}
