/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Objects;

/**
 * Represents a tool that the server provides. Tools enable servers to expose
 * executable functionality to the system. Through these tools, you can interact with
 * external systems, perform computations, and take actions in the real world.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Tool {
    private final String name;
    private final String description;
    private final JsonSchema inputSchema;
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Tool(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("inputSchema") JsonSchema inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }
    
    /**
     * Convenience constructor for creating a tool with a schema string.
     * 
     * @param name the name of the tool
     * @param description the description of the tool
     * @param schema the schema string
     */
    public Tool(String name, String description, String schema) {
        this(name, description, parseSchema(schema));
    }
    
    private static JsonSchema parseSchema(String schema) {
        try {
            return OBJECT_MAPPER.readValue(schema, JsonSchema.class);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Invalid schema: " + schema, e);
        }
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public JsonSchema inputSchema() {
        return inputSchema;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tool tool = (Tool) o;
        return Objects.equals(name, tool.name) &&
               Objects.equals(description, tool.description) &&
               Objects.equals(inputSchema, tool.inputSchema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, inputSchema);
    }

    @Override
    public String toString() {
        return "Tool{" +
               "name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", inputSchema=" + inputSchema +
               '}';
    }
}
