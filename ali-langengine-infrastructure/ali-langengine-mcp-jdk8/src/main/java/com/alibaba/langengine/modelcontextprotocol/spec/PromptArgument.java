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
 * Describes an argument that a prompt can accept.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PromptArgument {
    private final String name;
    private final String description;
    private final Boolean required;

    public PromptArgument(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("required") Boolean required) {
        this.name = name;
        this.description = description;
        this.required = required;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Boolean required() {
        return required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromptArgument that = (PromptArgument) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(description, that.description) &&
               Objects.equals(required, that.required);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, required);
    }

    @Override
    public String toString() {
        return "PromptArgument{" +
               "name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", required=" + required +
               '}';
    }
}
