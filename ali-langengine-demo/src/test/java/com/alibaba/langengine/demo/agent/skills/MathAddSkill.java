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
package com.alibaba.langengine.demo.agent.skills;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.StructuredParameter;
import com.alibaba.langengine.core.tool.StructuredSchema;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Data
@Slf4j
public class MathAddSkill extends StructuredTool {

    public MathAddSkill() {
        setName("math");
        setFunctionName("Add");
        setDescription("Adds value to a value.");
        setStructuredSchema(new MathAddSchema());
    }

    @Override
    public ToolExecuteResult execute(String toolInput) {
        log.warn(getClass() + " toolInput:" + toolInput);
        Map<String, Object> toolInputMap = JSON.parseObject(toolInput, Map.class);
        Double input = Double.parseDouble(toolInputMap.get("input").toString());
        Double account = Double.parseDouble(toolInputMap.get("Amount").toString());
        ToolExecuteResult toolExecuteResult = new ToolExecuteResult(String.valueOf(input + account));
        return toolExecuteResult;
    }

    @Data
    public class MathAddSchema extends StructuredSchema {

        public MathAddSchema() {
            StructuredParameter structuredParameter = new StructuredParameter();
            structuredParameter.setName("input");
            structuredParameter.setDescription("The value to add.");
            getParameters().add(structuredParameter);

            structuredParameter = new StructuredParameter();
            structuredParameter.setName("Amount");
            structuredParameter.setDescription("Amount to add.");
            getParameters().add(structuredParameter);
        }
    }
}
