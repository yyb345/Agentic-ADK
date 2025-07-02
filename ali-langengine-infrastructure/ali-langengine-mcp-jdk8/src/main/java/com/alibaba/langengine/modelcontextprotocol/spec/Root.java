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
 * Represents a root directory or file that the server can operate on.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Root {
    private final String uri;
    private final String name;

    public Root(
            @JsonProperty("uri") String uri,
            @JsonProperty("name") String name) {
        this.uri = uri;
        this.name = name;
    }

    public String uri() {
        return uri;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Root root = (Root) o;
        return Objects.equals(uri, root.uri) &&
               Objects.equals(name, root.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, name);
    }

    @Override
    public String toString() {
        return "Root{" +
               "uri='" + uri + '\'' +
               ", name='" + name + '\'' +
               '}';
    }
}
