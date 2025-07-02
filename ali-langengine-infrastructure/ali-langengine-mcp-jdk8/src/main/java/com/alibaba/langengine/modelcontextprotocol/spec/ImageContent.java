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
 * Image content implementation.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ImageContent implements Content {
    private final List<Role> audience;
    private final Double priority;
    private final String data;
    private final String mimeType;

    public ImageContent(
            @JsonProperty("audience") List<Role> audience,
            @JsonProperty("priority") Double priority,
            @JsonProperty("data") String data,
            @JsonProperty("mimeType") String mimeType) {
        this.audience = audience;
        this.priority = priority;
        this.data = data;
        this.mimeType = mimeType;
    }

    public List<Role> audience() {
        return audience;
    }

    public Double priority() {
        return priority;
    }

    public String data() {
        return data;
    }

    public String mimeType() {
        return mimeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageContent that = (ImageContent) o;
        return Objects.equals(audience, that.audience) &&
               Objects.equals(priority, that.priority) &&
               Objects.equals(data, that.data) &&
               Objects.equals(mimeType, that.mimeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(audience, priority, data, mimeType);
    }

    @Override
    public String toString() {
        return "ImageContent{" +
               "audience=" + audience +
               ", priority=" + priority +
               ", data='" + data + '\'' +
               ", mimeType='" + mimeType + '\'' +
               '}';
    }
}
