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
 * The client's response to a roots/list request from the server. This result contains
 * an array of Root objects, each representing a root directory or file that the
 * server can operate on.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ListRootsResult {
    private final List<Root> roots;

    public ListRootsResult(@JsonProperty("roots") List<Root> roots) {
        this.roots = roots;
    }

    public List<Root> roots() {
        return roots;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListRootsResult that = (ListRootsResult) o;
        return Objects.equals(roots, that.roots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roots);
    }

    @Override
    public String toString() {
        return "ListRootsResult{" +
               "roots=" + roots +
               '}';
    }
}
