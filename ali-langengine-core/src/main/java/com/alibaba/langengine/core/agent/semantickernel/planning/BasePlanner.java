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
package com.alibaba.langengine.core.agent.semantickernel.planning;

import com.alibaba.langengine.core.agent.AgentOutputParser;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.prompt.PromptConverter;
import lombok.Data;

import java.util.Map;

/**
 * A simplified version of SequentialPlanner that strings together a set of functions.
 * SequentialPlanner 的简化版本，将一组函数串在一起。
 *
 * @author xiaoxuan.lp
 */
@Data
public abstract class BasePlanner {

    /**
     * 工具集
     */
    private Map<String, BaseTool> toolMap;

    /**
     * 计划任务模版
     */
    private PromptTemplate promptTemplate;

    /**
     * 创建计划
     *
     * @param inputs
     * @return
     */
    public void createPlan(Map<String, Object> inputs) {
        String realTemplate = PromptConverter.replacePrompt(promptTemplate.getTemplate(), inputs);
        promptTemplate.setTemplate(realTemplate);
    }

    public abstract AgentOutputParser getPlannerOutputParser();
}
