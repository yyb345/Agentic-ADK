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
package com.alibaba.langengine.dashscope.model.agent;

public class DashScopePromptConstants {

    public static final String TOOL_DESC = "{name_for_model}: Call this tool to interact with the {name_for_human} API. What is the {name_for_human} API useful for? {description_for_model} Parameters: {parameters} Format the arguments as a JSON object.\n";

    public static final String PREFIX = "Answer the following questions as best you can. You have access to the following tools:";

    public static final String PREFIX_CH = "尽可能回答下面的问题，你可以使用下面这些工具，调用参数请用JSON方式表示：";

    public static final String FORMAT_INSTRUCTIONS = "Use the following format:\n\n" +
            "Question: the input question you must answer\n" +
            "Thought: you should always think about what to do\n" +
            "Action: the action to take, should be one of [{tool_names}]\n" +
            "Action Input: the input to the action\n" +
            "Observation: the result of the action\n" +
            "... (this Thought/Action/Action Input/Observation can be repeated zero or more times)\n" +
            "Thought: I now know the final answer\n" +
            "Final Answer: the final answer to the original input question\n";

    public static final String FORMAT_INSTRUCTIONS_CH =
            "Question: 你需要回答的问题\n" +
            "Thought: 你应该时刻思考该做什么\n" +
            "Action: 你需要使用的工具，应该是 [{tool_names}] 其中之一\n" +
            "Action Input: 使用的工具的输入参数\n" +
            "Observation: 工具的输出结果\n" +
            "... (你可以零次或者多次使用Thought/Action/Action Input/Observation来一步一步的思考如何回答问题。)\n" +
            "Thought: 我现在知道了最终的答案\n" +
            "Final Answer: 原始输入问题的最终答案\n"
            ;

    public static final String SUFFIX = "Begin!\n" +
            "\n" +
            "Question: {input}\n" +
            "{agent_scratchpad}";

    public static final String SUFFIX_CH = "开始回答!\n" +
            "\n" +
            "Question: {input}\n" +
            "{agent_scratchpad}";
}
