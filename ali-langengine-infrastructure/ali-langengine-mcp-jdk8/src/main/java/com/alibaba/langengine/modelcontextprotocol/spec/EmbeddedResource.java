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
 * Embedded resource content implementation.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EmbeddedResource implements Content {
    private final List<Role> audience;
    private final Double priority;
    private final ResourceContents resource;

    public EmbeddedResource(
            @JsonProperty("audience") List<Role> audience,
            @JsonProperty("priority") Double priority,
            @JsonProperty("resource") ResourceContents resource) {
        this.audience = audience;
        this.priority = priority;
        this.resource = resource;
    }

    public List<Role> audience() {
        return audience;
    }

    public Double priority() {
        return priority;
    }

    public ResourceContents resource() {
        return resource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmbeddedResource that = (EmbeddedResource) o;
        return Objects.equals(audience, that.audience) &&
               Objects.equals(priority, that.priority) &&
               Objects.equals(resource, that.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(audience, priority, resource);
    }

    @Override
    public String toString() {
        return "EmbeddedResource{" +
               "audience=" + audience +
               ", priority=" + priority +
               ", resource=" + resource +
               '}';
    }
}
