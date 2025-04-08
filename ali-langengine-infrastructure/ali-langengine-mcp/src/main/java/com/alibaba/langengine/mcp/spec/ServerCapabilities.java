/**
 * Copyright (C) 2024 AIDC-AI
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.mcp.spec;

import lombok.Getter;

import java.util.Map;

@Getter
public class ServerCapabilities {

    private final Map<String, Object> experimental;

    private final LoggingCapabilities logging;

    private final PromptCapabilities prompts;

    private final ResourceCapabilities resources;

    private final ToolCapabilities tools;

    public ServerCapabilities(Map<String, Object> experimental, LoggingCapabilities logging, PromptCapabilities prompts, ResourceCapabilities resources, ToolCapabilities tools) {
        this.experimental = experimental;
        this.logging = logging;
        this.prompts = prompts;
        this.resources = resources;
        this.tools = tools;
    }

    @Getter
    public static class PromptCapabilities {

        private final Boolean listChanged;

        public PromptCapabilities(Boolean listChanged) {
            this.listChanged = listChanged;
        }
    }

    @Getter
    public static class ResourceCapabilities {
        private final Boolean subscribe;
        private final Boolean listChanged;

        public ResourceCapabilities(Boolean subscribe, Boolean listChanged) {
            this.subscribe = subscribe;
            this.listChanged = listChanged;
        }

    }

    @Getter
    public static class ToolCapabilities {
        private final Boolean listChanged;

        public ToolCapabilities(Boolean listChanged) {
            this.listChanged = listChanged;
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