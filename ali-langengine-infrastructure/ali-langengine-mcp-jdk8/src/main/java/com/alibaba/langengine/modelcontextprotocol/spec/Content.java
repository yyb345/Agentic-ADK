/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Content interface for different types of content (text, image, resource).
 * 
 * JDK 1.8 compatible version.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ 
    @JsonSubTypes.Type(value = TextContent.class, name = "text"),
    @JsonSubTypes.Type(value = ImageContent.class, name = "image"),
    @JsonSubTypes.Type(value = EmbeddedResource.class, name = "resource") 
})
public interface Content {

    /**
     * Get the type of content.
     * @return the content type
     */
    default String type() {
        if (this instanceof TextContent) {
            return "text";
        }
        else if (this instanceof ImageContent) {
            return "image";
        }
        else if (this instanceof EmbeddedResource) {
            return "resource";
        }
        throw new IllegalArgumentException("Unknown content type: " + this);
    }
}
