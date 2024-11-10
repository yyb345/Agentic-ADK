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
package com.alibaba.langengine.tool;

import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;

public class ApiLogTool extends StructuredTool {

    public ApiLogTool(){
        setName("ApiLogTool");
        setFunctionName("getLog");
//        this.setDescription("This is a logging query interface that you can request to interact with the logging system. You can call this tool multiple times \n"+
//                "When can  use this tool : If question includes the word requestId. Then You can call this interface to obtain detailed records of calls. If requestId is involved in the questioning, you can extract requestId to call this interface to obtain more detailed journey information. Call parameters: [{ \"requestId \": \" \", \"type \": \"String \", \"description \": \"call request id \"}]"
//                );
        setDescription("这是一个调用日志查询接口，如果[{question}]中包含requestId关键字,你可以请求这个工具与日志系统进行交互，调用这个工具。\n" +
                "请先提取出requestId的值，将它赋值为value。调用参数：[{\"requestId\": \"value\", \"type\": \"String\", \"description\": \"调用请求id\"}]。");


    }


    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public void setDescription(String description) {
       super.setDescription(description);
    }

    @Override
    public ToolExecuteResult execute(String toolInput) {

        System.out.println(toolInput);
        ToolExecuteResult toolExecuteResult = new ToolExecuteResult("Final answer:" + toolInput);
        toolExecuteResult.setInterrupted(true);
        return toolExecuteResult;
    }
}
