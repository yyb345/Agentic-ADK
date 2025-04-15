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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * The Model Context Protocol (MCP) provides a standardized way for servers to send
 * structured log messages to clients. Clients can control logging verbosity by
 * setting minimum log levels, with servers sending notifications containing severity
 * levels, optional logger names, and arbitrary JSON-serializable data.
 * 
 * JDK 1.8 compatible version.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class LoggingMessageNotification {
    private final LoggingLevel level;
    private final String logger;
    private final String data;

    public LoggingMessageNotification(
            @JsonProperty("level") LoggingLevel level,
            @JsonProperty("logger") String logger,
            @JsonProperty("data") String data) {
        this.level = level;
        this.logger = logger;
        this.data = data;
    }

    public LoggingLevel level() {
        return level;
    }

    public String logger() {
        return logger;
    }

    public String data() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoggingMessageNotification that = (LoggingMessageNotification) o;
        return level == that.level &&
               Objects.equals(logger, that.logger) &&
               Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, logger, data);
    }

    @Override
    public String toString() {
        return "LoggingMessageNotification{" +
               "level=" + level +
               ", logger='" + logger + '\'' +
               ", data='" + data + '\'' +
               '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LoggingLevel level = LoggingLevel.INFO;
        private String logger = "server";
        private String data;

        public Builder level(LoggingLevel level) {
            this.level = level;
            return this;
        }

        public Builder logger(String logger) {
            this.logger = logger;
            return this;
        }

        public Builder data(String data) {
            this.data = data;
            return this;
        }

        public LoggingMessageNotification build() {
            return new LoggingMessageNotification(level, logger, data);
        }
    }
}
