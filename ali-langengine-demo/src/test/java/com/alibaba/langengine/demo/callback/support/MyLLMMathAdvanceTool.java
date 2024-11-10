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
package com.alibaba.langengine.demo.callback.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.core.util.PythonUtils;
import com.alibaba.langengine.tool.PythonCodeConstants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Data
@Slf4j
public class MyLLMMathAdvanceTool extends StructuredTool {

    private BaseLanguageModel llm;

    public MyLLMMathAdvanceTool() {
        setName("Calculator");
        setDescription("Useful for when you need to answer questions about math.");
    }

    @Override
    public ToolExecuteResult execute(String toolInput) {
//        throw new RuntimeException("LLMMathAdvanceTool error.");
        Map<String, Object> expression = JSON.parseObject(toolInput, new TypeReference<Map>(){});
        if(expression != null && expression.containsKey("expression")) {
            toolInput = (String) expression.get("expression");
        }
        toolInput = toolInput.replaceAll("\\^", "**");
        String returnString = PythonUtils.invokePythonCode(PythonCodeConstants.LLMMATH_PYTHON_CODE, toolInput);
        return new ToolExecuteResult("Answer: " + returnString);
    }

    public static void main(String[] args) {
        MyLLMMathAdvanceTool llmMathAdvanceTool = new MyLLMMathAdvanceTool();
        ToolExecuteResult toolExecuteResult = llmMathAdvanceTool.execute("3^0.43");
        System.out.println("toolExecuteResult:" + toolExecuteResult.getOutput());
    }
}
