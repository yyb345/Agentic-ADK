/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Base class for paginated results.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PaginatedResult {
    private final String nextCursor;

    public PaginatedResult(@JsonProperty("nextCursor") String nextCursor) {
        this.nextCursor = nextCursor;
    }

    public String nextCursor() {
        return nextCursor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaginatedResult that = (PaginatedResult) o;
        return Objects.equals(nextCursor, that.nextCursor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nextCursor);
    }

    @Override
    public String toString() {
        return "PaginatedResult{" +
               "nextCursor='" + nextCursor + '\'' +
               '}';
    }
}
