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
package com.alibaba.langengine.demo.agent.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SuggestTrafficTool extends StructuredTool {

    public SuggestTrafficTool() {
        setName("SuggestTrafficTool");
        setHumanName("城市拥堵建议");
        setDescription("根据拥堵的原因给出适当的交通建议");
    }

    @Override
    public String formatStructSchema() {
        SuggestTrafficSchema schema = new SuggestTrafficSchema();
        schema.setReason("交通拥堵原因");
        return JSON.toJSONString(schema);
    }

    @Override
    public ToolExecuteResult execute(String toolInput) {
        log.warn("toolInput:" + toolInput);
        return new ToolExecuteResult("建议您错峰出行");
    }
}
