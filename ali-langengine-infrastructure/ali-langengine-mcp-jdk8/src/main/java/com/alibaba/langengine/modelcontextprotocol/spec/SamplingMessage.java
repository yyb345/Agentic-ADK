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
 * A message for sampling.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SamplingMessage {
    private final Role role;
    private final Content content;

    public SamplingMessage(
            @JsonProperty("role") Role role,
            @JsonProperty("content") Content content) {
        this.role = role;
        this.content = content;
    }

    public Role role() {
        return role;
    }

    public Content content() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SamplingMessage that = (SamplingMessage) o;
        return role == that.role &&
               Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, content);
    }

    @Override
    public String toString() {
        return "SamplingMessage{" +
               "role=" + role +
               ", content=" + content +
               '}';
    }
}
