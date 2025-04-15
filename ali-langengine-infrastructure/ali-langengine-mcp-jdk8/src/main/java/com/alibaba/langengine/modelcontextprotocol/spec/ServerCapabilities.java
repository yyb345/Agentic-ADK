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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * Server capabilities for the Model Context Protocol.
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ServerCapabilities {
    private final Map<String, Object> experimental;
    private final LoggingCapabilities logging;
    private final PromptCapabilities prompts;
    private final ResourceCapabilities resources;
    private final ToolCapabilities tools;

    public ServerCapabilities(
            @JsonProperty("experimental") Map<String, Object> experimental,
            @JsonProperty("logging") LoggingCapabilities logging,
            @JsonProperty("prompts") PromptCapabilities prompts,
            @JsonProperty("resources") ResourceCapabilities resources,
            @JsonProperty("tools") ToolCapabilities tools) {
        this.experimental = experimental;
        this.logging = logging;
        this.prompts = prompts;
        this.resources = resources;
        this.tools = tools;
    }

    public Map<String, Object> experimental() {
        return experimental;
    }

    public LoggingCapabilities logging() {
        return logging;
    }

    public PromptCapabilities prompts() {
        return prompts;
    }

    public ResourceCapabilities resources() {
        return resources;
    }

    public ToolCapabilities tools() {
        return tools;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerCapabilities that = (ServerCapabilities) o;
        return Objects.equals(experimental, that.experimental) &&
               Objects.equals(logging, that.logging) &&
               Objects.equals(prompts, that.prompts) &&
               Objects.equals(resources, that.resources) &&
               Objects.equals(tools, that.tools);
    }

    @Override
    public int hashCode() {
        return Objects.hash(experimental, logging, prompts, resources, tools);
    }

    @Override
    public String toString() {
        return "ServerCapabilities{" +
               "experimental=" + experimental +
               ", logging=" + logging +
               ", prompts=" + prompts +
               ", resources=" + resources +
               ", tools=" + tools +
               '}';
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public static class LoggingCapabilities {
        public LoggingCapabilities() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o != null && getClass() == o.getClass();
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "LoggingCapabilities{}";
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public static class PromptCapabilities {
        private final Boolean listChanged;

        public PromptCapabilities(@JsonProperty("listChanged") Boolean listChanged) {
            this.listChanged = listChanged;
        }

        public Boolean listChanged() {
            return listChanged;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PromptCapabilities that = (PromptCapabilities) o;
            return Objects.equals(listChanged, that.listChanged);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listChanged);
        }

        @Override
        public String toString() {
            return "PromptCapabilities{" +
                   "listChanged=" + listChanged +
                   '}';
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public static class ResourceCapabilities {
        private final Boolean subscribe;
        private final Boolean listChanged;

        public ResourceCapabilities(
                @JsonProperty("subscribe") Boolean subscribe,
                @JsonProperty("listChanged") Boolean listChanged) {
            this.subscribe = subscribe;
            this.listChanged = listChanged;
        }

        public Boolean subscribe() {
            return subscribe;
        }

        public Boolean listChanged() {
            return listChanged;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ResourceCapabilities that = (ResourceCapabilities) o;
            return Objects.equals(subscribe, that.subscribe) &&
                   Objects.equals(listChanged, that.listChanged);
        }

        @Override
        public int hashCode() {
            return Objects.hash(subscribe, listChanged);
        }

        @Override
        public String toString() {
            return "ResourceCapabilities{" +
                   "subscribe=" + subscribe +
                   ", listChanged=" + listChanged +
                   '}';
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public static class ToolCapabilities {
        private final Boolean listChanged;

        public ToolCapabilities(@JsonProperty("listChanged") Boolean listChanged) {
            this.listChanged = listChanged;
        }

        public Boolean listChanged() {
            return listChanged;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ToolCapabilities that = (ToolCapabilities) o;
            return Objects.equals(listChanged, that.listChanged);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listChanged);
        }

        @Override
        public String toString() {
            return "ToolCapabilities{" +
                   "listChanged=" + listChanged +
                   '}';
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Map<String, Object> experimental;
        private LoggingCapabilities logging = new LoggingCapabilities();
        private PromptCapabilities prompts;
        private ResourceCapabilities resources;
        private ToolCapabilities tools;

        public Builder experimental(Map<String, Object> experimental) {
            this.experimental = experimental;
            return this;
        }

        public Builder logging() {
            this.logging = new LoggingCapabilities();
            return this;
        }

        public Builder prompts(Boolean listChanged) {
            this.prompts = new PromptCapabilities(listChanged);
            return this;
        }

        public Builder resources(Boolean subscribe, Boolean listChanged) {
            this.resources = new ResourceCapabilities(subscribe, listChanged);
            return this;
        }

        public Builder tools(Boolean listChanged) {
            this.tools = new ToolCapabilities(listChanged);
            return this;
        }

        public ServerCapabilities build() {
            return new ServerCapabilities(experimental, logging, prompts, resources, tools);
        }
    }
}
