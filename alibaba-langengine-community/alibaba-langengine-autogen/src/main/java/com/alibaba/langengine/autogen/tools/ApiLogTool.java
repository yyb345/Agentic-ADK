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

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ApiLogTool extends StructuredTool {

    public ApiLogTool() {
        setName("ApiLogTool");
        setFunctionName("getLog");
        setHumanName("API日志查询");
        setDescription("这是一个调用日志查询接口，如果问题中包含requestId关键字,你可以请求这个工具与日志系统进行交互，调用这个工具。请先提取出requestId的值，将它赋值为value。");
        setStructuredSchema(new ApiLogSchema());
    }

    @Override
    public ToolExecuteResult execute(String message) {
        if(message==null || message.length()==0){
            return new ToolExecuteResult("");
        }

        Map<String,Object> parse = (Map<String, Object>) JSON.parse(message);
        if(parse.get("requestId")==null){
            return new ToolExecuteResult("");
        }
        String requestId = parse.get("requestId").toString();

        ApiLogResult apiLogResult = new ApiLogResult();
        apiLogResult.setApi("taobao.trade.fullinfo.get");
        apiLogResult.setApiName("1111111");
        apiLogResult.setCode("15");
        apiLogResult.setSubCode("isp.time-out");
        apiLogResult.setAppkey("12345678");
        ToolExecuteResult toolExecuteResult = new ToolExecuteResult(JSON.toJSONString(apiLogResult));
        return toolExecuteResult;
    }

}
