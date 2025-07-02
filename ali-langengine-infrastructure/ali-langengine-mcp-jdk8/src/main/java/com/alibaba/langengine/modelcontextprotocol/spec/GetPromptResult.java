/*
 * Copyright 2024-2024 the original author or authors.
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
