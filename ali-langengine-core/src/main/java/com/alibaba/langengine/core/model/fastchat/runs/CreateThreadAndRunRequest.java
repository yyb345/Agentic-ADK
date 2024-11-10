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
import com.alibaba.langengine.core.model.fastchat.threads.ThreadRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2023-11-16 23:08
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateThreadAndRunRequest {
    
    @JsonProperty("assistant_id")
    private String assistantId;
    
    private ThreadRequest thread;

    private String model;
    
    private String instructions;

    private List<Tool> tools;

    private Map<String, String> metadata;
}
