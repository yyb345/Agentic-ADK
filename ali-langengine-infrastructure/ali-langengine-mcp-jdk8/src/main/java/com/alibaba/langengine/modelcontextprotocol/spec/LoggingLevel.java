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
