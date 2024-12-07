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

public class DocTool extends StructuredTool {

    public DocTool(){
        this.setName("DocTool");
        this.setDescription("这是一个文档查询接口，你可以请求这个工具与文档系统进行交互。你可以多次调用这个工具。\n" +
                "这个工具能够干什么：你可以调用此接口获取文档，你可以提取出用户的提问来调用此接口来获取更详细的文档信息。调用参数：[{\"docTitle\": \"xx\", \"type\": \"String\", \"description\": \"文档标题\"}]。");    }


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
        return new ToolExecuteResult("Final answer:"+toolInput);
    }
}
