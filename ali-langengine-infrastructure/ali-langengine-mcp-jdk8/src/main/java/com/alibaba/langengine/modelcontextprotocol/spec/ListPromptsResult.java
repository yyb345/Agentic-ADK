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
 * The server's response to a prompts/list request from the client.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ListPromptsResult {
    private final List<Prompt> prompts;
    private final String nextCursor;

    public ListPromptsResult(
            @JsonProperty("prompts") List<Prompt> prompts,
            @JsonProperty("nextCursor") String nextCursor) {
        this.prompts = prompts;
        this.nextCursor = nextCursor;
    }

    public List<Prompt> prompts() {
        return prompts;
    }

    public String nextCursor() {
        return nextCursor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListPromptsResult that = (ListPromptsResult) o;
        return Objects.equals(prompts, that.prompts) &&
               Objects.equals(nextCursor, that.nextCursor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prompts, nextCursor);
    }

    @Override
    public String toString() {
        return "ListPromptsResult{" +
               "prompts=" + prompts +
               ", nextCursor='" + nextCursor + '\'' +
               '}';
    }
}
