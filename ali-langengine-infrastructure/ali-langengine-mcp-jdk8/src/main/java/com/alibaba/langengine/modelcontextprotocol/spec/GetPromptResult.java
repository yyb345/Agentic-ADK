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
import java.util.Objects;

/**
 * The server's response to a prompts/get request from the client.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class GetPromptResult {
    private final String description;
    private final List<PromptMessage> messages;

    public GetPromptResult(
            @JsonProperty("description") String description,
            @JsonProperty("messages") List<PromptMessage> messages) {
        this.description = description;
        this.messages = messages;
    }

    public String description() {
        return description;
    }

    public List<PromptMessage> messages() {
        return messages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetPromptResult that = (GetPromptResult) o;
        return Objects.equals(description, that.description) &&
               Objects.equals(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, messages);
    }

    @Override
    public String toString() {
        return "GetPromptResult{" +
               "description='" + description + '\'' +
               ", messages=" + messages +
               '}';
    }
}
