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
package com.alibaba.langengine.core.tool;

import com.alibaba.langengine.core.callback.ExecutionContext;
import lombok.Data;

@Data
public class DefaultTool extends BaseTool {

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            onToolStart(this, toolInput, executionContext);

            ToolExecuteResult toolExecuteResult;
            if(executionContext != null && executionContext.getToolExecuteResult() != null) {
                toolExecuteResult = executionContext.getToolExecuteResult();
            } else {
                if(getBasicFunc() != null) {
                    toolExecuteResult = new ToolExecuteResult(getBasicFunc().apply(toolInput));
                } else {
                    toolExecuteResult = run(toolInput);
                }
            }

            onToolEnd(this, toolInput, toolExecuteResult, executionContext);

            return toolExecuteResult;
        } catch (Throwable e) {
            onToolError(this, e, executionContext);
            throw e;
        }
    }
}