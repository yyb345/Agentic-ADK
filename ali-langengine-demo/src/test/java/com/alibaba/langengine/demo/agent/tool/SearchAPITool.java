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
import lombok.Data;

import java.util.Map;
import java.util.TreeMap;

@Data
public class SearchAPITool extends DefaultTool {

    public SearchAPITool() {
        setName("search");
        setDescription("useful for when you need to answer questions about current events. You should ask targeted questions");
        Map<String, Object> args = getArgs();
        Map<String, Object> queryMap = new TreeMap<>();
        queryMap.put("title", "Query");
        queryMap.put("type", "string");
        args.put("query", queryMap);
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        String result = null;
        if(toolInput.equals("San Francisco weather report today")
            || toolInput.equals("weather in San Francisco today")
            || toolInput.indexOf("San Francisco") >= 0) {
            result = "Current Weather ; 54°F  Sunny ; RealFeel 66°. Pleasant. RealFeel Guide. Pleasant. 63° to 81°. Most consider this temperature range ideal. LEARN MORE. RealFeel ...";
        }
        return new ToolExecuteResult(result);
    }
}
