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
import lombok.Data;

@Data
public class CreateAppTool extends StructuredTool<CreateAppSchema> {

    public CreateAppTool() {
        setName("create_application");
        setDescription("create_application(applicationName: str, param: dict) - 用于生成一个应用程序的方法，applicationName为应用名称，param为应用的字段具体信息");
    }

    @Override
    public ToolExecuteResult execute(String toolInput) {
        String output = "Answer: " + toolInput;
        return new ToolExecuteResult(output, false);
    }
}
