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

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Data
public abstract class StructuredTool<T extends StructuredSchema> extends DefaultTool {

    private T structuredSchema;

    public String formatStructSchema() {
        if(structuredSchema == null
                || structuredSchema.getParameters() == null
                || structuredSchema.getParameters().size() == 0) {
            return "{}";
        }
        return JSON.toJSONString(structuredSchema.getParameters());
    }

    public String formatSemantickernelBasicPrompt() {
        return formatSemantickernelBasicPrompt(null);
    }

    public String formatSemantickernelBasicPrompt(String inputParams) {
        String skFunction = String.format("%s_%s", getName(), getFunctionName());
        String description = String.format("description: %s", getDescription());
        StringBuilder builder = new StringBuilder();
        builder.append(skFunction + "\n");
        builder.append(description + "\n");
        if(structuredSchema == null
                || structuredSchema.getParameters() == null
                || structuredSchema.getParameters().size() == 0) {
            return builder.toString();
        }
        if(!StringUtils.isEmpty(inputParams)) {
            builder.append(inputParams + ":\n");
        } else {
            builder.append("args:\n");
        }
        structuredSchema.getParameters().forEach(param ->
                builder.append(String.format("- %s: %s\n", param.getName(), param.getDescription()))
        );
        return builder.toString();
    }

    public String formatSemantickernelActionPrompt() {
        String skFunction = String.format("%s_%s", getName(), getFunctionName());
        String description = getDescription();

        StringBuilder builder = new StringBuilder();
        builder.append("// " + description + "\n");
        builder.append(skFunction + "\n");
        if(structuredSchema == null
                || structuredSchema.getParameters() == null
                || structuredSchema.getParameters().size() == 0) {
            return builder.toString();
        }
        structuredSchema.getParameters().forEach(param ->
                builder.append(String.format("Parameter \\\"%s\\\": %s\n", param.getName(), param.getDescription()))
        );
        return builder.toString();
    }

    public String formatSemantickernelStepwisePrompt() {
        String skFunction = String.format("%s_%s", getName(), getFunctionName());
        String description = getDescription();

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%s: %s\n", skFunction, description));
        if(structuredSchema == null
                || structuredSchema.getParameters() == null
                || structuredSchema.getParameters().size() == 0) {
            return builder.toString();
        }
        builder.append("  inputs:\n");
        structuredSchema.getParameters().forEach(param ->
                builder.append(String.format("    - %s: %s\n", param.getName(), param.getDescription()))
        );

        return  builder.toString();
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            onToolStart(this, toolInput, executionContext);

            ToolExecuteResult toolExecuteResult;
            if(executionContext != null && executionContext.getToolExecuteResult() != null) {
                toolExecuteResult = executionContext.getToolExecuteResult();
            } else {
                toolExecuteResult = execute(toolInput);
            }

            onToolEnd(this, toolInput, toolExecuteResult, executionContext);

            return toolExecuteResult;
        } catch (Throwable e) {
            onToolError(this, e, executionContext);
            throw e;
        }
    }

    public abstract ToolExecuteResult execute(String toolInput);
}
