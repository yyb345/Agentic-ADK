package com.alibaba.langengine.mcp.spec.schema.resources;

public interface ResourceContents {

    /**
     * The URI of this resource.
     * @return the URI of this resource.
     */
    String getUri();

    /**
     * The MIME type of this resource.
     * @return the MIME type of this resource.
     */
    String getMimeType();
}