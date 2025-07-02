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
 * A hint for model preferences.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ModelHint {
    private final String name;

    public ModelHint(@JsonProperty("name") String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    /**
     * Factory method to create a new ModelHint.
     * 
     * @param name the name of the hint
     * @return a new ModelHint
     */
    public static ModelHint of(String name) {
        return new ModelHint(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelHint modelHint = (ModelHint) o;
        return Objects.equals(name, modelHint.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "ModelHint{" +
               "name='" + name + '\'' +
               '}';
    }
}
