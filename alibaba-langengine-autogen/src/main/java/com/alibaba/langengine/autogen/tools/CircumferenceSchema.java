/**
 * Copyright (C) 2024 AIDC-AI
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
package com.alibaba.langengine.autogen.tools;

import com.alibaba.langengine.core.tool.StructuredParameter;
import com.alibaba.langengine.core.tool.StructuredSchema;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CircumferenceSchema extends StructuredSchema {

    public CircumferenceSchema() {
        StructuredParameter structuredParameter = new StructuredParameter();
        structuredParameter.setName("radius");
        structuredParameter.setDescription("半径");
        structuredParameter.setRequired(true);

        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        structuredParameter.setSchema(schema);

        getParameters().add(structuredParameter);
    }
}
