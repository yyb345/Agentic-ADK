/**
 * Copyright (C) 2024 AIDC-AI
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.mcp.spec.schema;

import com.alibaba.langengine.mcp.spec.*;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * A request from the server to sample an LLM via the client.
 * The client has full discretion over which model to select.
 * The client should also inform the user before beginning sampling to allow them to inspect the request
 * (human in the loop) and decide whether to approve it.
 */
@Getter
public class CreateMessageRequest {

    private List<SamplingMessage> messages;
    private ModelPreferences modelPreferences;
    private String systemPrompt;
    private ContextInclusionStrategy includeContext;
    private Double temperature;
    private int maxTokens;
    private List<String> stopSequences;
    private Map<String, Object> metadata;

    public enum ContextInclusionStrategy {
        NONE,
        THIS_SERVER,
        ALL_SERVERS
    }

    public CreateMessageRequest(List<SamplingMessage> messages, ModelPreferences modelPreferences,
                                String systemPrompt, ContextInclusionStrategy includeContext,
                                Double temperature, int maxTokens,
                                List<String> stopSequences, Map<String, Object> metadata) {
        this.messages = messages;
        this.modelPreferences = modelPreferences;
        this.systemPrompt = systemPrompt;
        this.includeContext = includeContext;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.stopSequences = stopSequences;
        this.metadata = metadata;
    }
}
