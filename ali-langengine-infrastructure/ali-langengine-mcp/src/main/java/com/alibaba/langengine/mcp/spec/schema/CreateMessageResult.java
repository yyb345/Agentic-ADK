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
package com.alibaba.langengine.mcp.spec.schema;

import com.alibaba.langengine.mcp.spec.schema.prompts.Content;
import com.alibaba.langengine.mcp.spec.schema.prompts.Role;
import com.alibaba.langengine.mcp.spec.schema.prompts.TextContent;
import lombok.Getter;

@Getter
public class CreateMessageResult {

    Role role;

    Content content;

    String model;

    StopReason stopReason;

    public CreateMessageResult(Role role, Content content, String model, StopReason stopReason) {
        this.role = role;
        this.content = content;
        this.model = model;
        this.stopReason = stopReason;
    }

    public enum StopReason {
        END_TURN,
        STOP_SEQUENCE,
        MAX_TOKENS
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Role role = Role.assistant;
        private Content content;
        private String model;
        private StopReason stopReason = StopReason.END_TURN;

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder content(Content content) {
            this.content = content;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder stopReason(StopReason stopReason) {
            this.stopReason = stopReason;
            return this;
        }

        public Builder message(String message) {
            this.content = new TextContent(message);
            return this;
        }

        public CreateMessageResult build() {
            return new CreateMessageResult(role, content, model, stopReason);
        }
    }
}
