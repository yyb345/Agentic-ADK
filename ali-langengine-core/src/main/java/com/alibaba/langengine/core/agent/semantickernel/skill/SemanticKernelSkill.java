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
package com.alibaba.langengine.core.agent.semantickernel.skill;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SemanticKernel技能基类
 *
 * @author xiaoxuan.lp 
 */
@Slf4j
@Data
public abstract class SemanticKernelSkill extends StructuredTool {

    private LLMChain llmChain;

    private List<String> stop;

    @Override
    public ToolExecuteResult execute(String toolInput) {
        log.warn(getClass() + " toolInput:" + toolInput);
        Map<String, Object> toolInputMap = JSON.parseObject(toolInput, Map.class);
        Map<String, Object> inputs = new HashMap<>();
        inputs.putAll(toolInputMap);
        if(getStop() != null && getStop().size() > 0) {
            inputs.put("stop", getStop());
        }
        Map<String, Object> outputs = getLlmChain().run(inputs);
        ToolExecuteResult toolExecuteResult = new ToolExecuteResult(outputs.get("text").toString());
        return toolExecuteResult;
    }
}
