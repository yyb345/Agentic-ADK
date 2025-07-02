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
 * Result of a resources/templates/list request.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ListResourceTemplatesResult {
    private final List<ResourceTemplate> resourceTemplates;
    private final String nextCursor;

    public ListResourceTemplatesResult(
            @JsonProperty("resourceTemplates") List<ResourceTemplate> resourceTemplates,
            @JsonProperty("nextCursor") String nextCursor) {
        this.resourceTemplates = resourceTemplates;
        this.nextCursor = nextCursor;
    }

    public List<ResourceTemplate> resourceTemplates() {
        return resourceTemplates;
    }

    public String nextCursor() {
        return nextCursor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListResourceTemplatesResult that = (ListResourceTemplatesResult) o;
        return Objects.equals(resourceTemplates, that.resourceTemplates) &&
               Objects.equals(nextCursor, that.nextCursor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceTemplates, nextCursor);
    }

    @Override
    public String toString() {
        return "ListResourceTemplatesResult{" +
               "resourceTemplates=" + resourceTemplates +
               ", nextCursor='" + nextCursor + '\'' +
               '}';
    }
}
