/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.alibaba.langengine.modelcontextprotocol.spec;

/**
 * Base for objects that include optional annotations for the client. The client can
 * use annotations to inform how objects are used or displayed.
 * 
 * JDK 1.8 compatible version.
 */
public interface Annotated {
    /**
     * Get the annotations for this object.
     * @return the annotations
     */
    Annotations annotations();
}
