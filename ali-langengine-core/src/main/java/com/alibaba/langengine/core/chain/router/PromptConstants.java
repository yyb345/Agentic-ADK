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
package com.alibaba.langengine.core.chain.router;

public class PromptConstants {

    public static final String MULTI_PROMPT_ROUTER_TEMPLATE = "Given a query to a question answering system select the system best suited " +
            "for the input. You will be given the names of the available systems and a description " +
            "of what questions the system is best suited for. You may also revise the original " +
            "input if you think that revising it will ultimately lead to a better response.\n" +
            "\n" +
            "<< FORMATTING >>\n" +
            "Return a markdown code snippet with a JSON object formatted to look like:\n" +
            "```json\n" +
            "{{{{\n" +
            "    \"destination\": string, \\\\ name of the question answering system to use or \"DEFAULT\"\n" +
            "    \"next_inputs\": string \\\\ a potentially modified version of the original input\n" +
            "}}}}\n" +
            "```\n" +
            "\n" +
            "REMEMBER: \"destination\" MUST be one of the candidate prompt names specified below OR " +
            "it can be \"DEFAULT\" if the input is not well suited for any of the candidate prompts.\n" +
            "REMEMBER: \"next_inputs\" can just be the original input if you don't think any " +
            "modifications are needed.\n" +
            "\n" +
            "<< CANDIDATE PROMPTS >>\n" +
            "{destinations}\n" +
            "\n" +
            "<< INPUT >>\n" +
            "{input}\n" +
            "\n" +
            "<< OUTPUT >>";

    public static final String MULTI_PROMPT_ROUTER_TEMPLATE_CN = "给定语言模型的原始文本输入，选择最适合的模型提示 " +
            "输入。 您将获得可用提示的名称和" +
            "该提示最适合什么。 如果您" +
            "认为修改它最终会导致语言得到更好的响应" +
            "模型。\n" +
            "\n" +
            "<< 格式化 >>\n" +
            "返回一个 Markdown 代码片段，其中 JSON 对象的格式如下：\n" +
            "```json\n" +
            "{{{{\n" +
            "     “destination”：字符串, \\\\ 要使用的提示名称或“DEFAULT”\n" +
            "     “next_inputs”：字符串 \\\\ 原始输入的可能修改版本\n" +
            "}}}}\n" +
            "````\n" +
            "\n" +
            "请记住：“目的地”必须是下面指定的候选提示名称之一或 " +
            "如果输入不太适合任何候选提示，则它可以是“默认”。\n" +
            "请记住：如果您认为没有任何\\，“next_inputs”可以只是原始输入\n" +
            "需要修改。\n" +
            "\n" +
            "<< 候选人提示 >>\n" +
            "{destinations}\n" +
            "\n" +
            "<< 输入 >>\n" +
            "{input}\n" +
            "\n" +
            "<< 输出 >>";
}
