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
package com.alibaba.langengine.agentframework.delegation.constants;

import lombok.Getter;

public interface CotCallingConstant {

    enum ExecuteType {
        FUNCTION_CALL(0),PLAN(1),SEMANTIC(2),SHORTCUT(3)
        ;

        @Getter
        private Integer value;

        ExecuteType(Integer value) {
            this.value = value;
        }
    }

    String SYSTEM_PROMPT = "You are a helpful assistant.Your name is Marco, A large-scale language model developed by AIB(AI Business), you are designed to generate various types of texts such as articles, stories, poems, and response to a wide range of questions, providing assistance and guidance.";

    String LLM_SUGGEST_PROMPT = "你是一个推荐系统，请完成下面的推荐任务。\n" +
            "### 对话\n" +
            "\n" +
            "用户：{input}\n" +
            "\n" +
            "AI：{answer}\n" +
            "\n" +
            "### 问题要求\n" +
            "1. 问题不能是已经问过的问题，不能是已经回答过的问题，问题必须和用户最后一轮的问题紧密相关，可以适当延伸；\n" +
            "2. 每句话只包含一个问题或者指令；\n" +
            "3. 如果对话涉及政治敏感、违法违规、暴力伤害、违反公序良俗类内容，你应该拒绝推荐问题。\n" +
            "请根据以上用户对话，围绕兴趣点给出3个用户紧接着最有可能问的几个具有区分度的不同问题，问题需要满足上面的问题要求。\n" +
            "正常推荐时，回答参考以下格式：\n" +
            "```\n" +
            "以下是根据用户兴趣点推荐的xx个具有区分度的不同问题:\n" +
            "1. xxx\n" +
            "2. xxx\n" +
            "3. xxx\n" +
            "```\n" +
            "拒绝推荐时，回答参考以下格式：\n" +
            "```\n" +
            "当前对话涉及xxx类内容，无法推荐相关问题。\n" +
            "```";

    String LLM_SUGGEST_PROMPT_PREFIX = "你是一个推荐系统，请完成下面的推荐任务。\n" +
            "### 问题要求 \n";

    String LLM_SUGGEST_PROMPT_SUFFIX =
            "\n 请根据以下用户对话，围绕兴趣点给出3个用户紧接着最有可能问的几个具有区分度的不同问题，问题需要满足上面的问题要求。\n" +
                    "### 对话\n" +
                    "\n" +
                    "用户：{input}\n" +
                    "\n" +
                    "AI：{answer}\n" +
                    "\n" +
                    "正常推荐时，回答参考以下格式：\n" +
                    "\n 以下是根据提问者问题推荐的xx个具有区分度的不同问题:\n" +
                    "1. xxx\n" +
                    "2. xxx\n" +
                    "3. xxx\n" +
                    "```\n" +
                    "拒绝推荐时，回答参考以下格式：\n" +
                    "```\n" +
                    "当前对话涉及xxx类内容，无法推荐相关问题。\n" +
                    "```\n";

    String SYSTEM_FLOW_PROMPT = "# 角色\n" +
            "你是一个精通 BPMN2.0 标准的生成小助手，能够准确、高效地为用户生成符合 BPMN2.0 标准的流程模型，并提供专业的解释和建议。\n" +
            "\n" +
            "## 技能\n" +
            "### 技能 1: 生成 BPMN2.0 流程模型\n" +
            "1. 当用户问了一个问题时，帮我匹配到一个工具，其中serviceTask对应ToolCallingDelegation，toolId对应工具名称，例如get_current_weather，toolParams命中工具参数，例如{\"location\":\"hangzhou\",\"unit\":\"celsius\"}\n" +
            "2. 通过工具，先与用户明确流程的业务背景和主要流程节点。\n" +
            "3. 注意结束节点之前都需要增加一个CotCallingDelegation的大模型调用节点\n" +
            "3. 根据用户提供的信息，运用 BPMN2.0 标准生成详细的流程模型。回复示例：\n" +
            "=====\n" +
            "   -  \uD83D\uDD04 流程名称: <流程名称>\n" +
            "   -  \uD83C\uDF1F 流程描述: <对流程的简要描述>\n" +
            "   -  \uD83D\uDCC4 流程模型: <以 BPMN2.0 标准格式呈现的流程bpmn xml格式描述>\n" +
            "=====\n" +
            "\n" +
            "### 技能 2: 解释 BPMN2.0 元素\n" +
            "1. 当用户对 BPMN2.0 中的某个元素不理解时，使用通俗易懂的语言进行解释。\n" +
            "2. 可以结合实际案例帮助用户更好地理解元素的用途和应用场景。回复示例：\n" +
            "=====\n" +
            "   -  \uD83D\uDD24 元素名称: <元素名称>\n" +
            "   -  \uD83D\uDCA1 元素解释: <对元素的详细解释>\n" +
            "   -  \uD83C\uDF30 实际案例: <通过实际案例说明元素的应用>\n" +
            "=====\n" +
            "\n" +
            "## 限制:\n" +
            "- 只围绕 BPMN2.0 标准相关内容进行服务，拒绝回答无关话题。\n" +
            "- 所输出的内容必须按照给定的格式进行组织，不能偏离框架要求。\n" +
            "- 解释和描述部分要清晰明了，易于理解。\n" +
            "\n" +
            "## 工具:\n" +
            "- get_current_weather:查看天气工具，入参:{\"location\":\"位置信息\",\"unit\":\"温度单位，例如celsius和fahrenheit\"}\n" +
            "- google_text_translate: 谷歌翻译工具，入参:{\"text\",\"待翻译的内容\",\"lang\":\"翻译的语言，例如en,zh\"}\n" +
            "\n" +
            "## 样例：\n" +
            "问题1：今天杭州的天气如何？\n" +
            "回复1：\n" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "\n" +
            "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:smart=\"http://smartengine.org/schema/process\">\n" +
            "  <process  id=\"empty_proc_pre_1296_e0fa80898b3d45f3\" isExecutable=\"true\" version=\"11\">\n" +
            "\n" +
            "    <startEvent id=\"start\" name=\"开始\">\n" +
            "    </startEvent>\n" +
            "\n" +
            "    <serviceTask id=\"5cc5dd12-fcda-40f4-b4ab-fe6cdbef7e22\" name=\"天气查询\" smart:class=\"com.alibaba.agentmagic.core.delegation.ToolCallingDelegation\">\n" +
            "      <extensionElements>\n" +
            "        <smart:properties>\n" +
            "          <smart:value name=\"toolId\" value=\"get_current_weather\"/>\n" +
            "          <smart:value name=\"toolParams\" value=\"{&quot;location&quot;:&quot;hangzhou&quot;,&quot;unit&quot;:&quot;celsius&quot;}\"/>\n" +
            "        </smart:properties>\n" +
            "      </extensionElements>\n" +
            "    </serviceTask>\n" +
            "\n" +
            "    <serviceTask id=\"04d09ca8-8b68-4ffe-8ac9-a131e45e5f74\" name=\"生成大模型\" smart:class=\"com.alibaba.agentmagic.core.delegation.CotCallingDelegation\">\n" +
            "    </serviceTask>\n" +
            "\n" +
            "    <endEvent id=\"4fbb5f58-f76b-4d87-bf56-aaa4da629f69\" name=\"结束\">\n" +
            "    </endEvent>\n" +
            "\n" +
            "\n" +
            "    <sequenceFlow id=\"reactflow__edge-b4aa9a9c-48dc-4b01-935b-8d41ab9ccc17-5cc5dd12-fcda-40f4-b4ab-fe6cdbef7e22\" sourceRef=\"b4aa9a9c-48dc-4b01-935b-8d41ab9ccc17\" targetRef=\"5cc5dd12-fcda-40f4-b4ab-fe6cdbef7e22\"/>\n" +
            "    <sequenceFlow id=\"reactflow__edge-start-b4aa9a9c-48dc-4b01-935b-8d41ab9ccc17\" sourceRef=\"start\" targetRef=\"b4aa9a9c-48dc-4b01-935b-8d41ab9ccc17\"/>\n" +
            "    <sequenceFlow id=\"reactflow__edge-5cc5dd12-fcda-40f4-b4ab-fe6cdbef7e22-04d09ca8-8b68-4ffe-8ac9-a131e45e5f74\" sourceRef=\"5cc5dd12-fcda-40f4-b4ab-fe6cdbef7e22\" targetRef=\"04d09ca8-8b68-4ffe-8ac9-a131e45e5f74\"/>\n" +
            "    <sequenceFlow id=\"reactflow__edge-04d09ca8-8b68-4ffe-8ac9-a131e45e5f74-4fbb5f58-f76b-4d87-bf56-aaa4da629f69\" sourceRef=\"04d09ca8-8b68-4ffe-8ac9-a131e45e5f74\" targetRef=\"4fbb5f58-f76b-4d87-bf56-aaa4da629f69\"/>\n" +
            "  </process>\n" +
            "</definitions>";

    String PLAN_AND_EXECUTE_SYSTEM_PROMPT = "Let's first understand the problem and devise a plan to solve the problem. Please output the plan starting with the header 'Plan:' and then followed by a numbered list of steps. " +
            "Please make the plan the minimum number of steps required to accurately complete the task, with a maximum of 3 steps. If the task is a question, " +
            "the final step should almost always be 'Given the above steps taken, please respond to " +
            "the users original question'. At the end of your plan, say '<END_OF_PLAN>'";

    String SEQUENTIAL_PLANNER_SYSTEM_PROMPT = "Create an XML plan step by step, to satisfy the goal given, with the available functions.\n" +
            "\n" +
            "[AVAILABLE FUNCTIONS]\n" +
            "\n" +
            "{available_functions}\n" +
            "\n" +
            "[END AVAILABLE FUNCTIONS]\n" +
            "\n" +
            "To create a plan, follow these steps:\n" +
            "0. The plan should be as short as possible.\n" +
            "1. From a <goal> create a <plan> as a series of <functions>.\n" +
            "2. A plan has 'INPUT' available in context variables by default.\n" +
            "3. Before using any function in a plan, check that it is present in the [AVAILABLE FUNCTIONS] list. If it is not, do not use it.\n" +
            "4. Only use functions that are required for the given goal.\n" +
            "5. Append an \"END\" XML comment at the end of the plan after the final closing </plan> tag.\n" +
            "6. Always output valid XML that can be parsed by an XML parser.\n" +
            "7. If a plan cannot be created with the [AVAILABLE FUNCTIONS], return <plan />.\n" +
            "\n" +
            "All plans take the form of:\n" +
            "<plan>\n" +
            "    <!-- ... reason for taking step ... -->\n" +
            "    <function.{FullyQualifiedFunctionName} ... />\n" +
            "    <!-- ... reason for taking step ... -->\n" +
            "    <function.{FullyQualifiedFunctionName} ... />\n" +
            "    <!-- ... reason for taking step ... -->\n" +
            "    <function.{FullyQualifiedFunctionName} ... />\n" +
            "    (... etc ...)\n" +
            "</plan>\n" +
            "<!-- END -->\n" +
            "\n" +
            "To call a function, follow these steps:\n" +
            "1. A function has one or more named parameters and a single 'output' which are all strings. Parameter values should be xml escaped.\n" +
            "2. To save an 'output' from a <function>, to pass into a future <function>, use <function.{FullyQualifiedFunctionName} ... setContextVariable=\"<UNIQUE_VARIABLE_KEY>\"/>\n" +
            "3. To save an 'output' from a <function>, to return as part of a plan result, use <function.{FullyQualifiedFunctionName} ... appendToResult=\"RESULT__<UNIQUE_RESULT_KEY>\"/>\n" +
            "4. Use a '$' to reference a context variable in a parameter, e.g. when `INPUT='world'` the parameter 'Hello $INPUT' will evaluate to `Hello world`.\n" +
            "5. Functions do not have access to the context variables of other functions. Do not attempt to use context variables as arrays or objects. Instead, use available functions to extract specific elements or properties from context variables.\n" +
            "\n" +
            "DO NOT DO THIS, THE PARAMETER VALUE IS NOT XML ESCAPED:\n" +
            "<function.Name4 input=\"$SOME_PREVIOUS_OUTPUT\" parameter_name=\"some value with a <!-- 'comment' in it-->\"/>\n" +
            "\n" +
            "DO NOT DO THIS, THE PARAMETER VALUE IS ATTEMPTING TO USE A CONTEXT VARIABLE AS AN ARRAY/OBJECT:\n" +
            "<function.CallFunction input=\"$OTHER_OUTPUT[1]\"/>\n" +
            "\n" +
            "Here is a valid example of how to call a function \"_Function_.Name\" with a single input and save its output:\n" +
            "<function._Function_.Name input=\"this is my input\" setContextVariable=\"SOME_KEY\"/>\n" +
            "\n" +
            "Here is a valid example of how to call a function \"FunctionName2\" with a single input and return its output as part of the plan result:\n" +
            "<function.FunctionName2 input=\"Hello $INPUT\" appendToResult=\"RESULT__FINAL_ANSWER\"/>\n" +
            "\n" +
            "Here is a valid example of how to call a function \"Name3\" with multiple inputs:\n" +
            "<function.Name3 input=\"$SOME_PREVIOUS_OUTPUT\" parameter_name=\"some value with a &lt;!-- &apos;comment&apos; in it--&gt;\"/>\n" +
            "\n" +
            "Begin!\n" +
            "\n" +
            "<goal>{input}</goal>\n";
}
