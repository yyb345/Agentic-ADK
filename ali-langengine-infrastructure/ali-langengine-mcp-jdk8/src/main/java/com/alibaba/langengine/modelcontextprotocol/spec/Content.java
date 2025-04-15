/*
 * Copyright 2025 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
