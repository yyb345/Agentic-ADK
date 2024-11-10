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
import com.alibaba.langengine.core.util.PythonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
public class LLMMathTool extends DefaultTool {

    public LLMMathTool() {
        setName("Calculator");
        setDescription("Useful for when you need to answer questions about math. The input parameter is a mathematical formula。");
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        if(getFunc() != null) {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("question", toolInput);
            Map<String, Object> outputs = getFunc().apply(inputs);
            return new ToolExecuteResult(outputs.get("answer").toString(), true);
        } else {
//            throw new RuntimeException("LLMMathTool error.");
            toolInput = toolInput.replaceAll("\\^", "**");
            String result = PythonUtils.invokePythonCode(PythonCodeConstants.LLMMATH_PYTHON_CODE, toolInput);
            return new ToolExecuteResult("Answer: " + result);
        }
    }
}
