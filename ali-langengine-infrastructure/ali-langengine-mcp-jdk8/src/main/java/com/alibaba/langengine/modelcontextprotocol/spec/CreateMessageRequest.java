/*
 * Copyright 2025 Alibaba Group Holding Ltd.
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
package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Request to create a message using sampling.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CreateMessageRequest implements McpSchema.Request {
    private final List<SamplingMessage> messages;
    private final ModelPreferences modelPreferences;
    private final String systemPrompt;
    private final ContextInclusionStrategy includeContext;
    private final Double temperature;
    private final int maxTokens;
    private final List<String> stopSequences;
    private final Map<String, Object> metadata;

    public CreateMessageRequest(
            @JsonProperty("messages") List<SamplingMessage> messages,
            @JsonProperty("modelPreferences") ModelPreferences modelPreferences,
            @JsonProperty("systemPrompt") String systemPrompt,
            @JsonProperty("includeContext") ContextInclusionStrategy includeContext,
            @JsonProperty("temperature") Double temperature,
            @JsonProperty("maxTokens") int maxTokens,
            @JsonProperty("stopSequences") List<String> stopSequences,
            @JsonProperty("metadata") Map<String, Object> metadata) {
        this.messages = messages;
        this.modelPreferences = modelPreferences;
        this.systemPrompt = systemPrompt;
        this.includeContext = includeContext;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.stopSequences = stopSequences;
        this.metadata = metadata;
    }

    public List<SamplingMessage> messages() {
        return messages;
    }

    public ModelPreferences modelPreferences() {
        return modelPreferences;
    }

    public String systemPrompt() {
        return systemPrompt;
    }

    public ContextInclusionStrategy includeContext() {
        return includeContext;
    }

    public Double temperature() {
        return temperature;
    }

    public int maxTokens() {
        return maxTokens;
    }

    public List<String> stopSequences() {
        return stopSequences;
    }

    public Map<String, Object> metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateMessageRequest that = (CreateMessageRequest) o;
        return maxTokens == that.maxTokens &&
               Objects.equals(messages, that.messages) &&
               Objects.equals(modelPreferences, that.modelPreferences) &&
               Objects.equals(systemPrompt, that.systemPrompt) &&
               includeContext == that.includeContext &&
               Objects.equals(temperature, that.temperature) &&
               Objects.equals(stopSequences, that.stopSequences) &&
               Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messages, modelPreferences, systemPrompt, includeContext,
                            temperature, maxTokens, stopSequences, metadata);
    }

    @Override
    public String toString() {
        return "CreateMessageRequest{" +
               "messages=" + messages +
               ", modelPreferences=" + modelPreferences +
               ", systemPrompt='" + systemPrompt + '\'' +
               ", includeContext=" + includeContext +
               ", temperature=" + temperature +
               ", maxTokens=" + maxTokens +
               ", stopSequences=" + stopSequences +
               ", metadata=" + metadata +
               '}';
    }

    /**
     * Context inclusion strategy for sampling.
     */
    public enum ContextInclusionStrategy {
        @JsonProperty("none") NONE,
        @JsonProperty("thisServer") THIS_SERVER,
        @JsonProperty("allServers") ALL_SERVERS
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<SamplingMessage> messages;
        private ModelPreferences modelPreferences;
        private String systemPrompt;
        private ContextInclusionStrategy includeContext;
        private Double temperature;
        private int maxTokens;
        private List<String> stopSequences;
        private Map<String, Object> metadata;

        public Builder messages(List<SamplingMessage> messages) {
            this.messages = messages;
            return this;
        }

        public Builder modelPreferences(ModelPreferences modelPreferences) {
            this.modelPreferences = modelPreferences;
            return this;
        }

        public Builder systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        public Builder includeContext(ContextInclusionStrategy includeContext) {
            this.includeContext = includeContext;
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder stopSequences(List<String> stopSequences) {
            this.stopSequences = stopSequences;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public CreateMessageRequest build() {
            return new CreateMessageRequest(messages, modelPreferences, systemPrompt,
                    includeContext, temperature, maxTokens, stopSequences, metadata);
        }
    }
}
