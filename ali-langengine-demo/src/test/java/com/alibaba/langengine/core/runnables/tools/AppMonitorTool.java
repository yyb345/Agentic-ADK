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
package com.alibaba.langengine.core.runnables.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AppMonitorTool extends StructuredTool {


    public AppMonitorTool() {
        setName("AppMonitorTool");
        setFunctionName("getMonitorData");
        setDescription("这是一个应用监控接口，如果问题中包含appkey关键字,你可以请求这个工具与应用监控系统进行交互，调用这个工具。请先提取出appkey的值，将它赋值为value。");
        setStructuredSchema(new AppMonitorSchema());
    }

    @Override
    public ToolExecuteResult execute(String toolInput) {
        if(toolInput==null || toolInput.length()==0){
            return new ToolExecuteResult("");
        }

        Map<String,Object> parse;
        Object obj = JSON.parse(toolInput);
        if(obj instanceof JSONArray) {
            List<Map<String,Object>> parseList = (List<Map<String,Object>>)JSON.parse(toolInput);
            parse = parseList.get(0);
        } else {
            parse = (Map<String, Object>) JSON.parse(toolInput);
        }
        if(parse.get("appkey")==null){
            return new ToolExecuteResult("");
        }
        String appkey = parse.get("appkey").toString();

        AppMonitorResult appMonitorResult = new AppMonitorResult();
        appMonitorResult.setAppkey(appkey);
        appMonitorResult.setRt("100ms");
        appMonitorResult.setAppkey(appkey);
        appMonitorResult.setSuccessRate("60.01%");
        appMonitorResult.setQps("200");
        ToolExecuteResult toolExecuteResult = new ToolExecuteResult(JSON.toJSONString(appMonitorResult));
//        toolExecuteResult.setInterrupted(true);
        return toolExecuteResult;
    }

}
