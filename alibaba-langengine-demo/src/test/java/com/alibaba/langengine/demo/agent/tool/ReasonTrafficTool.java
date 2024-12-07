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
public class ReasonTrafficTool extends StructuredTool {

    public ReasonTrafficTool() {
        setName("ReasonTrafficTool");
        setHumanName("城市拥堵原因查询");
        setDescription("分析这一天拥堵的原因");
    }

    @Override
    public String formatStructSchema() {
        ReasonTrafficSchema schema = new ReasonTrafficSchema();
        schema.setTrafficStatus("路况状态");
        return JSON.toJSONString(schema);
    }

    @Override
    public ToolExecuteResult execute(String toolInput) {
        log.warn("toolInput:" + toolInput);
        return new ToolExecuteResult("因为有大型活动，造成了交通拥堵");
    }
}
