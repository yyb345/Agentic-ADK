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
 * Binary contents of a resource.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class BlobResourceContents implements ResourceContents {
    private final String uri;
    private final String mimeType;
    private final String blob;

    public BlobResourceContents(
            @JsonProperty("uri") String uri,
            @JsonProperty("mimeType") String mimeType,
            @JsonProperty("blob") String blob) {
        this.uri = uri;
        this.mimeType = mimeType;
        this.blob = blob;
    }

    @Override
    public String uri() {
        return uri;
    }

    @Override
    public String mimeType() {
        return mimeType;
    }

    public String blob() {
        return blob;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlobResourceContents that = (BlobResourceContents) o;
        return Objects.equals(uri, that.uri) &&
               Objects.equals(mimeType, that.mimeType) &&
               Objects.equals(blob, that.blob);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, mimeType, blob);
    }

    @Override
    public String toString() {
        return "BlobResourceContents{" +
               "uri='" + uri + '\'' +
               ", mimeType='" + mimeType + '\'' +
               ", blob='" + blob + '\'' +
               '}';
    }
}
