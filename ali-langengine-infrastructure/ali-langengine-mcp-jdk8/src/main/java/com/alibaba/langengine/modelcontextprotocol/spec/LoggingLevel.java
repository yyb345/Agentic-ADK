/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Logging levels for MCP logging.
 * 
 * JDK 1.8 compatible version.
 */
public enum LoggingLevel {
    @JsonProperty("debug") DEBUG(0),
    @JsonProperty("info") INFO(1),
    @JsonProperty("notice") NOTICE(2),
    @JsonProperty("warning") WARNING(3),
    @JsonProperty("error") ERROR(4),
    @JsonProperty("critical") CRITICAL(5),
    @JsonProperty("alert") ALERT(6),
    @JsonProperty("emergency") EMERGENCY(7);

    private final int level;

    LoggingLevel(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }
}
