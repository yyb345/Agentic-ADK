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
 * A prompt or prompt template that the server offers.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Prompt {
    private final String name;
    private final String description;
    private final List<PromptArgument> arguments;

    public Prompt(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("arguments") List<PromptArgument> arguments) {
        this.name = name;
        this.description = description;
        this.arguments = arguments;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public List<PromptArgument> arguments() {
        return arguments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prompt prompt = (Prompt) o;
        return Objects.equals(name, prompt.name) &&
               Objects.equals(description, prompt.description) &&
               Objects.equals(arguments, prompt.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, arguments);
    }

    @Override
    public String toString() {
        return "Prompt{" +
               "name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", arguments=" + arguments +
               '}';
    }
}
