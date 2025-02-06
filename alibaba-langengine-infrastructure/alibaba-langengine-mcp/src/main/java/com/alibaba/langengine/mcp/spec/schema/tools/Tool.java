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
package com.alibaba.langengine.mcp.spec.schema.tools;

import com.alibaba.langengine.mcp.spec.McpJsonUtil;
import com.alibaba.langengine.mcp.spec.schema.JsonSchema;
import lombok.Getter;

@Getter
public class Tool {

    /**
     * The name of the tool.
     */
    private String name;

    /**
     * A human-readable description of the tool.
     */
    private String description;

    /**
     * A JSON object defining the expected parameters for the tool.
     */
    private JsonSchema inputSchema;

    public Tool(String name, String description, String schema) {
        this.name = name;
        this.description = description;
        this.inputSchema = McpJsonUtil.parseSchema(schema);
    }

}
