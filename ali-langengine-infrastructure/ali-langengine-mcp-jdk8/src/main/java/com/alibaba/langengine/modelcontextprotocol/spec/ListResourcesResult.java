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
 * Result of a resources/list request.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ListResourcesResult {
    private final List<Resource> resources;
    private final String nextCursor;

    public ListResourcesResult(
            @JsonProperty("resources") List<Resource> resources,
            @JsonProperty("nextCursor") String nextCursor) {
        this.resources = resources;
        this.nextCursor = nextCursor;
    }

    public List<Resource> resources() {
        return resources;
    }

    public String nextCursor() {
        return nextCursor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListResourcesResult that = (ListResourcesResult) o;
        return Objects.equals(resources, that.resources) &&
               Objects.equals(nextCursor, that.nextCursor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resources, nextCursor);
    }

    @Override
    public String toString() {
        return "ListResourcesResult{" +
               "resources=" + resources +
               ", nextCursor='" + nextCursor + '\'' +
               '}';
    }
}
