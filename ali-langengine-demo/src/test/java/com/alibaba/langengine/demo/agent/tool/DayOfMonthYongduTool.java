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

import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class DayOfMonthYongduTool extends DefaultTool {

    public DayOfMonthYongduTool() {
        setName("DayOfMonthYongduTool");
        setDescription("分析某个地区某月份最拥堵是哪一天");
        Map<String, Object> args = getArgs();
        Map<String, Object> queryMap = new TreeMap<>();
        queryMap.put("month", "某月份（用数字）");
        queryMap.put("type", "integer");
        queryMap.put("area", "某地区");
        queryMap.put("type", "string");
        args.put("query", queryMap);
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.warn("toolInput:" + toolInput);
        return new ToolExecuteResult("2023-08-05", true);
    }
}
