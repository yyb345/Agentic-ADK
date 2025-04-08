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
package com.alibaba.langengine.core.agent.planexecute;

/**
 * PromptConstants
 *
 * @author xiaoxuan.lp
 */
public class PromptConstants {

    public static final String SYSTEM_PROMPT = "Let's first understand the problem and devise a plan to solve the problem. Please output the plan starting with the header 'Plan:' and then followed by a numbered list of steps. Please make the plan the minimum number of steps required to accurately complete the task. If the task is a question, the final step should almost always be 'Given the above steps taken, please respond to the users original question'. At the end of your plan, say '<END_OF_PLAN>'";
    public static final String SYSTEM_PROMPT_CH = "让我们首先了解问题并制定解决问题的计划。 请输出以标题“计划：”开头的计划，然后是编号的步骤列表。 请制定准确完成任务所需的最少步骤数的计划。 如果任务是一个问题，那么最后一步几乎总是“鉴于采取了上述步骤，请回答用户最初的问题”。 在计划结束时，说“<END_OF_PLAN>”";

    public static final String HUMAN_MESSAGE_TEMPLATE = "Previous steps: {previous_steps}\n" +
            "\n" +
            "Current objective: {current_step}\n" +
            "\n" +
            "{agent_scratchpad}";
    public static final String HUMAN_MESSAGE_TEMPLATE_CH = "之前的步骤：{previous_steps}\n" +
            "\n" +
            "当前目标：{current_step}\n" +
            "\n" +
            "{agent_scratchpad}";

    public static final String TASK_PREFIX = "{objective}\n" +
            "\n";
}
