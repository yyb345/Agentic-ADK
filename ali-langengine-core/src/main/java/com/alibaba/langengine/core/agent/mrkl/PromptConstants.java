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
package com.alibaba.langengine.core.agent.mrkl;

/**
 * PromptConstants
 *
 * @author xiaoxuan.lp
 */
public class PromptConstants {

    public static final String PREFIX = "Answer the following questions as best you can. You have access to the following tools:";
    public static final String PREFIX_CH = "尽可能回答以下问题。 您可以使用以下工具：";

    public static final String FORMAT_INSTRUCTIONS = "Use the following format:\n" +
            "\n" +
            "Question: the input question you must answer\n" +
            "Thought: you should always think about what to do\n" +
            "Action: the action to take, should be one of [{tool_names}, Final Answer]\n" +
            "Action Input: the input to the action\n" +
            "Observation: the result of the action\n" +
            "... (this Thought/Action/Action Input/Observation can repeat N times)\n" +
            "Thought: I now know the final answer\n" +
            "Final Answer: the final answer to the original input question";
    public static final String FORMAT_INSTRUCTIONS_CH = "使用以下格式：\n" +
            "\n" +
            "Question: 您必须回答的输入问题\n" +
            "Thought: 你应该时刻思考该做什么\n" +
            "Action: 要使用的工具应该是[{tool_names}, Final Answer]之一\n" +
            "Action Input:使用的工具的输入参数\n" +
            "Observation: 工具的输出结果\n" +
            "... (这个 Thought/Action/Action Input/Observation 可以重复n次)\n" +
            "Thought: 我现在知道了最终的答案\n" +
            "Final Answer: 原始输入问题的最终答案";

    public static final String SUFFIX = "Begin!\n" +
            "\n" +
            "Question: {input}\n" +
            "Thought:{agent_scratchpad}";
    public static final String SUFFIX_CH = "开始回答!\n" +
            "\n" +
            "Question: {input}\n" +
            "Thought:{agent_scratchpad}";
}
