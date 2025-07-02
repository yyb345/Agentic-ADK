/*
 * Copyright 2024 - 2024 the original author or authors.
 */
package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

/**
 * Marker interface for the server-side MCP transport.
 *
 * JDK 1.8 compatible version.
 *
 * @author Christian Tzolov
 * @author Dariusz Jędrzejczyk
 */
public interface McpServerTransport extends McpTransport {
    // This is a marker interface with no additional methods
}
