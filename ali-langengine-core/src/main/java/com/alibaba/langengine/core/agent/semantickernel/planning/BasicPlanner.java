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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static com.alibaba.langengine.core.agent.semantickernel.PromptConstants.BASIC_PLANNER_PROMPT_TEMPLATE;
import static com.alibaba.langengine.core.agent.semantickernel.PromptConstants.BASIC_PLANNER_PROMPT_TEMPLATE_CH;

/**
 * BasicPlanner 生成一个基于 JSON 的计划，旨在按顺序解决所提供的问题并按顺序进行评估。
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class BasicPlanner extends BasePlanner {

    public BasicPlanner() {
        this(false);
    }

    public BasicPlanner(boolean isCH) {
        setPromptTemplate(isCH ? BASIC_PLANNER_PROMPT_TEMPLATE_CH : BASIC_PLANNER_PROMPT_TEMPLATE);
    }

    public AgentOutputParser getPlannerOutputParser() {
        BasicPlannerOutputParser outputParser = new BasicPlannerOutputParser();
        outputParser.setToolMap(getToolMap());
        return outputParser;
    }
}