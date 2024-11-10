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
package com.alibaba.langengine.core.agent.structured2;

/**
 * PromptConstants
 *
 * @author xiaoxuan.lp
 */
public class PromptConstants {

    public static final String HUMAN_MESSAGE_TEMPLATE = "{input}\n\n{agent_scratchpad}";

    public static final String PREFIX = "Respond to the human as helpfully and accurately as possible. You have access to the following tools:";
    public static final String PREFIX_CH = "尽可能有益且准确地回应人类。 您可以使用以下工具：";

    public static final String FORMAT_INSTRUCTIONS = "Use a json blob to specify a tool by providing an action key (tool name) and an action_input key (tool input).\n" +
            "\n" +
            "Valid \"action\" values: \"Final Answer\" or {tool_names}\n" +
            "\n" +
            "Provide only ONE action per $JSON_BLOB, as shown:\n" +
            "\n" +
            "```\n" +
            "{{{{\n" +
            "  \"action\": $TOOL_NAME,\n" +
            "  \"action_input\": $INPUT\n" +
            "}}}}\n" +
            "```\n" +
            "\n" +
            "Follow this format:\n" +
            "\n" +
            "Question: input question to answer\n" +
            "Thought: consider previous and subsequent steps\n" +
            "Action:\n" +
            "```\n" +
            "$JSON_BLOB\n" +
            "```\n" +
            "Observation: action result\n" +
            "... (repeat Thought/Action/Observation N times)\n" +
            "Thought: I know what to respond\n" +
            "Action:\n" +
            "```\n" +
            "{{{{\n" +
            "  \"action\": \"Final Answer\",\n" +
            "  \"action_input\": \"Final response to human\"\n" +
            "}}}}\n" +
            "```";

    public static final String FORMAT_INSTRUCTIONS_CH = "使用 json blob 通过提供 action 键（工具名称）和 action_input 键（工具输入）来指定工具。\n" +
            "\n" +
            "有效的 \"action\" 值: \"Final Answer\" 或者 {tool_names}\n" +
            "\n" +
            "每个 $JSON_BLOB 仅提供一个操作，如下所示：\n" +
            "\n" +
            "```\n" +
            "{{{{\n" +
            "  \"action\": $TOOL_NAME,\n" +
            "  \"action_input\": $INPUT\n" +
            "}}}}\n" +
            "```\n" +
            "\n" +
            "请遵循以下格式：\n" +
            "\n" +
            "Question: 输入要回答的问题\n" +
            "Thought: 考虑之前和之后的步骤\n" +
            "Action:\n" +
            "```\n" +
            "$JSON_BLOB\n" +
            "```\n" +
            "Observation: action的结果\n" +
            "... (重复 Thought/Action/Observation N 次)\n" +
            "Thought: 我知道该回应什么\n" +
            "Action:\n" +
            "```\n" +
            "{{{{\n" +
            "  \"action\": \"Final Answer\",\n" +
            "  \"action_input\": \"对人类的最终反应\"\n" +
            "}}}}\n" +
            "```";

    public static final String SUFFIX_MEMORY = "Begin! Reminder to ALWAYS respond with a valid json blob of a single action. Use tools if necessary. Respond directly if appropriate. Format is Action:```$JSON_BLOB```then Observation:.\n" +
            "\n" +
            "Question: {input}\n" +
            "{chat_history}\n" +
            "Thought: ";

    public static final String SUFFIX_MEMORY_CH = "开始！ 提醒始终使用单个操作的有效 json blob 进行响应。 必要时使用工具。 如果合适的话直接回复。 格式为 Action:```$JSON_BLOB```然后是 Observation:。\n" +
            "\n" +
            "Question: {input}\n" +
            "{chat_history}\n" +
            "Thought: ";

    public static final String SUFFIX = "Begin! Reminder to ALWAYS respond with a valid json blob of a single action. Use tools if necessary. Respond directly if appropriate. Format is Action:```$JSON_BLOB```then Observation:.\n" +
            "\n" +
            "Question: {input}\n" +
            "Thought: {agent_scratchpad}";

    public static final String SUFFIX_CH = "开始！ 提醒始终使用单个操作的有效 json blob 进行响应。 必要时使用工具。 如果合适的话直接回复。 格式为 Action:```$JSON_BLOB```然后是 Observation:。\n" +
            "\n" +
            "Question: {input}\n" +
            "Thought: {agent_scratchpad}";
}
