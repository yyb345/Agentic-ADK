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
 * Base class for paginated requests.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PaginatedRequest {
    private final String cursor;

    public PaginatedRequest(@JsonProperty("cursor") String cursor) {
        this.cursor = cursor;
    }

    public String cursor() {
        return cursor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaginatedRequest that = (PaginatedRequest) o;
        return Objects.equals(cursor, that.cursor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cursor);
    }

    @Override
    public String toString() {
        return "PaginatedRequest{" +
               "cursor='" + cursor + '\'' +
               '}';
    }
}
