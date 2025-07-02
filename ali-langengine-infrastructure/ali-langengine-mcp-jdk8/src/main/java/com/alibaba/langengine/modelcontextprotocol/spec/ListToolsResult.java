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
 * The server's response to a tools/list request from the client.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ListToolsResult {
    private final List<Tool> tools;
    private final String nextCursor;

    public ListToolsResult(
            @JsonProperty("tools") List<Tool> tools,
            @JsonProperty("nextCursor") String nextCursor) {
        this.tools = tools;
        this.nextCursor = nextCursor;
    }

    public List<Tool> tools() {
        return tools;
    }

    public String nextCursor() {
        return nextCursor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListToolsResult that = (ListToolsResult) o;
        return Objects.equals(tools, that.tools) &&
               Objects.equals(nextCursor, that.nextCursor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tools, nextCursor);
    }

    @Override
    public String toString() {
        return "ListToolsResult{" +
               "tools=" + tools +
               ", nextCursor='" + nextCursor + '\'' +
               '}';
    }
}
