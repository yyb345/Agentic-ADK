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
 * The server's response to a tools/call request from the client.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CallToolResult {
    private final List<Content> content;
    private final Boolean isError;

    public CallToolResult(
            @JsonProperty("content") List<Content> content,
            @JsonProperty("isError") Boolean isError) {
        this.content = content;
        this.isError = isError;
    }

    public List<Content> content() {
        return content;
    }

    public Boolean isError() {
        return isError;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallToolResult that = (CallToolResult) o;
        return Objects.equals(content, that.content) &&
               Objects.equals(isError, that.isError);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, isError);
    }

    @Override
    public String toString() {
        return "CallToolResult{" +
               "content=" + content +
               ", isError=" + isError +
               '}';
    }
}
