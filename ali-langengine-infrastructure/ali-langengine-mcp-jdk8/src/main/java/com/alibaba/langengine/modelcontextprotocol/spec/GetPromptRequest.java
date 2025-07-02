/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * Used by the client to get a prompt provided by the server.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class GetPromptRequest implements McpSchema.Request {
    private final String name;
    private final Map<String, Object> arguments;

    public GetPromptRequest(
            @JsonProperty("name") String name,
            @JsonProperty("arguments") Map<String, Object> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public String name() {
        return name;
    }

    public Map<String, Object> arguments() {
        return arguments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetPromptRequest that = (GetPromptRequest) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, arguments);
    }

    @Override
    public String toString() {
        return "GetPromptRequest{" +
               "name='" + name + '\'' +
               ", arguments=" + arguments +
               '}';
    }
}
