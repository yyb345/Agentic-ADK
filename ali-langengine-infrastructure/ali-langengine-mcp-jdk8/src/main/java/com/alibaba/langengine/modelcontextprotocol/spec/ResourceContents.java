/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

/**
 * The contents of a specific resource or sub-resource.
 * 
 * JDK 1.8 compatible version.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, include = As.PROPERTY)
@JsonSubTypes({ 
    @JsonSubTypes.Type(value = TextResourceContents.class, name = "text"),
    @JsonSubTypes.Type(value = BlobResourceContents.class, name = "blob") 
})
public interface ResourceContents {

    /**
     * The URI of this resource.
     * @return the URI of this resource.
     */
    String uri();

    /**
     * The MIME type of this resource.
     * @return the MIME type of this resource.
     */
    String mimeType();
}
