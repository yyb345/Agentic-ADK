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
package com.alibaba.langengine.core.agent.selfask;

import com.alibaba.langengine.core.prompt.impl.PromptTemplate;

import java.util.Arrays;

/**
 * PromptConstants
 *
 * @author xiaoxuan.lp
 */
public class PromptConstants {

    public static final String DEFAULT_TEMPLATE = "Question: Who lived longer, Muhammad Ali or Alan Turing?\n" +
            "Are follow up questions needed here: Yes.\n" +
            "Follow up: How old was Muhammad Ali when he died?\n" +
            "Intermediate answer: Muhammad Ali was 74 years old when he died.\n" +
            "Follow up: How old was Alan Turing when he died?\n" +
            "Intermediate answer: Alan Turing was 41 years old when he died.\n" +
            "So the final answer is: Muhammad Ali\n" +
            "\n" +
            "Question: When was the founder of craigslist born?\n" +
            "Are follow up questions needed here: Yes.\n" +
            "Follow up: Who was the founder of craigslist?\n" +
            "Intermediate answer: Craigslist was founded by Craig Newmark.\n" +
            "Follow up: When was Craig Newmark born?\n" +
            "Intermediate answer: Craig Newmark was born on December 6, 1952.\n" +
            "So the final answer is: December 6, 1952\n" +
            "\n" +
            "Question: Who was the maternal grandfather of George Washington?\n" +
            "Are follow up questions needed here: Yes.\n" +
            "Follow up: Who was the mother of George Washington?\n" +
            "Intermediate answer: The mother of George Washington was Mary Ball Washington.\n" +
            "Follow up: Who was the father of Mary Ball Washington?\n" +
            "Intermediate answer: The father of Mary Ball Washington was Joseph Ball.\n" +
            "So the final answer is: Joseph Ball\n" +
            "\n" +
            "Question: Are both the directors of Jaws and Casino Royale from the same country?\n" +
            "Are follow up questions needed here: Yes.\n" +
            "Follow up: Who is the director of Jaws?\n" +
            "Intermediate answer: The director of Jaws is Steven Spielberg.\n" +
            "Follow up: Where is Steven Spielberg from?\n" +
            "Intermediate answer: The United States.\n" +
            "Follow up: Who is the director of Casino Royale?\n" +
            "Intermediate answer: The director of Casino Royale is Martin Campbell.\n" +
            "Follow up: Where is Martin Campbell from?\n" +
            "Intermediate answer: New Zealand.\n" +
            "So the final answer is: No\n" +
            "\n" +
            "Question: {input}\n" +
            "Are followup questions needed here:{agent_scratchpad}";
    public static final String DEFAULT_TEMPLATE_CH =
            "请按照下面的方式进行回答问题\n" +
            "'''\n" +
            "问题: 问题的描述\n" +
            "是否需要进一步追问: 是的 或者 不是\n" +
            "追问: 需要追加的问题\n" +
            "中间回答: 追问问题的结果，追问和中间回答可以有多轮\n" +
            "最终答案: 结合中间答案给出问题的最终答案\n" +
            "'''\n" +
            "下面是几个例子：\n" +
            "问题: 谁活得更久, Muhammad Ali 还是 Alan Turing?\n" +
            "是否需要进一步追问: 是的\n" +
            "追问: Muhammad Ali什么时候死的?\n" +
            "中间回答: Muhammad Ali是74岁死的\n" +
            "追问: Alan Turing什么时候死的?\n" +
            "中间回答: Alan Turing是41岁时候死的.\n" +
            "最终答案: Muhammad Ali\n" +
            "\n" +
            "问题: craigslist的创始人什么时候生的?\n" +
            "是否需要进一步追问: 是的\n" +
            "追问: 谁是craigslist的创始人?\n" +
            "中间回答:  craigslist的创始人是Craig Newmark\n" +
            "追问: Craig Newmark什么时候生的？\n" +
            "中间回答: Craig Newmark是1951年12月6日出生的.\n" +
            "最终答案: 1951年12月6日\n" +
            "\n" +
            "问题: craigslist的创始人什么时候生的?\n" +
            "是否需要进一步追问: 是的\n" +
            "追问: 谁是craigslist的创始人?\n" +
            "中间回答:  craigslist的创始人是Craig Newmark\n" +
            "追问: Craig Newmark什么时候生的？\n" +
            "中间回答: Craig Newmark是1951年12月6日出生的.\n" +
            "最终答案: 1951年12月6日\n" +
            "\n" +
            "问题: George Washington的外祖父是谁?\n" +
            "是否需要进一步追问: 是的\n" +
            "追问:  George Washington的妈妈是谁?\n" +
            "中间回答:  George Washington的妈妈是Mary Ball Washington\n" +
            "追问: Mary Ball Washington的爸爸是谁？\n" +
            "中间回答: Mary Ball Washington的爸爸是Joseph Ball\n" +
            "最终答案: Joseph Ball\n" +
            "\n" +
            "问题: George Washington的外祖父是谁?\n" +
            "是否需要进一步追问: 是的\n" +
            "追问:  George Washington的妈妈是谁?\n" +
            "中间回答:  George Washington的妈妈是Mary Ball Washington\n" +
            "追问: Mary Ball Washington的爸爸是谁？\n" +
            "中间回答: Mary Ball Washington的爸爸是Joseph Ball\n" +
            "最终答案: Joseph Ball\n" +
            "\n" +
            "问题: Jaws和Casino Royale的董事都来自同一个国家吗?\n" +
            "是否需要进一步追问: 是的\n" +
            "追问:  Jaws的董事是谁？\n" +
            "中间回答:  Jaws的董事是Steven Spielberg\n" +
            "追问: Steven Spielberg来自哪个国家？\n" +
            "中间回答: Steven Spielberg来自美国\n" +
            "追问: Casino Royale的董事是谁？\n" +
            "中间回答: Casino Royale的董事是Martin Campbell\n" +
            "追问: Martin Campbell来自哪个国家\n" +
            "中间回答: Martin Campbell来自新西兰\n" +
            "最终答案: 不是\n" +
            "\n" +
            "问题: {input}\n" +
            "是否需要进一步追问: {agent_scratchpad}";

    public static final PromptTemplate PROMPT = new PromptTemplate(DEFAULT_TEMPLATE, Arrays.asList(new String[]{ "input", "agent_scratchpad" }));
    public static final PromptTemplate PROMPT_CH = new PromptTemplate(DEFAULT_TEMPLATE_CH, Arrays.asList(new String[]{ "input", "agent_scratchpad" }));
}
