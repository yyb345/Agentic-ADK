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
 * Optional annotations for the client. The client can use annotations to inform how
 * objects are used or displayed.
 *
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Annotations {
    private final List<Role> audience;
    private final Double priority;

    public Annotations(
            @JsonProperty("audience") List<Role> audience,
            @JsonProperty("priority") Double priority) {
        this.audience = audience;
        this.priority = priority;
    }

    public List<Role> audience() {
        return audience;
    }

    public Double priority() {
        return priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Annotations that = (Annotations) o;
        return Objects.equals(audience, that.audience) &&
               Objects.equals(priority, that.priority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(audience, priority);
    }

    @Override
    public String toString() {
        return "Annotations{" +
               "audience=" + audience +
               ", priority=" + priority +
               '}';
    }
}
