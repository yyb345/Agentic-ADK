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

import static com.alibaba.langengine.core.agent.semantickernel.PromptConstants.*;

/**
 * 行动计划允许从众多功能中选择一个来实现给定的目标。
 * 规划器实现意图检测模式，使用注册的函数在内核中查看是否有相关的，提供调用的指令功能以及选择它的基本原理。 策划者还可以返回
 * 如果没有相关的内容可用，则"无功能"。
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class ActionPlanner extends BasePlanner {

    public ActionPlanner() {
        this(false);
    }

    public ActionPlanner(boolean isCH) {
        setPromptTemplate(isCH ? ACTION_PLANNER_PROMPT_TEMPLATE : ACTION_PLANNER_PROMPT_TEMPLATE);
    }

    @Override
    public AgentOutputParser getPlannerOutputParser() {
        ActionPlannerOutputParser outputParser = new ActionPlannerOutputParser();
        outputParser.setToolMap(getToolMap());
        return outputParser;
    }
}
