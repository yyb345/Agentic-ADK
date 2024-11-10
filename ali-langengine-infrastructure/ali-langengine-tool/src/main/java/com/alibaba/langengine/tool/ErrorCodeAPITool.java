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

import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ErrorCodeAPITool extends DefaultTool {

    public ErrorCodeAPITool() {
        setName("ErrorCodeAPITool");
        setDescription("An error message troubleshooting tool. Useful when you need to answer a question about a failed API call. The input should be some error code information in json format.");
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.warn("toolInput:" + toolInput);
        return new ToolExecuteResult("Answer: 当前系统有异常，请稍后再试." + System.currentTimeMillis(), true);
    }
}
