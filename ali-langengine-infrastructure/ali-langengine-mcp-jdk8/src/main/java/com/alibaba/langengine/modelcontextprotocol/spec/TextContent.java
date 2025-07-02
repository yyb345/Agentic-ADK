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
 * Text content implementation.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TextContent implements Content {
    private final List<Role> audience;
    private final Double priority;
    private final String text;

    public TextContent(
            @JsonProperty("audience") List<Role> audience,
            @JsonProperty("priority") Double priority,
            @JsonProperty("text") String text) {
        this.audience = audience;
        this.priority = priority;
        this.text = text;
    }

    /**
     * Convenience constructor for creating text content with just the text.
     * 
     * @param content the text content
     */
    public TextContent(String content) {
        this(null, null, content);
    }

    public List<Role> audience() {
        return audience;
    }

    public Double priority() {
        return priority;
    }

    public String text() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextContent that = (TextContent) o;
        return Objects.equals(audience, that.audience) &&
               Objects.equals(priority, that.priority) &&
               Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(audience, priority, text);
    }

    @Override
    public String toString() {
        return "TextContent{" +
               "audience=" + audience +
               ", priority=" + priority +
               ", text='" + text + '\'' +
               '}';
    }
}
