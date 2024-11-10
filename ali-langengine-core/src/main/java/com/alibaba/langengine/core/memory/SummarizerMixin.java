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
package com.alibaba.langengine.core.memory;

/**
 * @author aihe.ah
 * @time 2023/11/13
 * 功能说明：
 */

import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.prompt.BasePromptTemplate;
import com.alibaba.langengine.core.messages.SystemMessage;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;

import com.google.common.collect.Lists;
import lombok.Data;

/**
 * SummarizerMixin类
 */
@Data
public class SummarizerMixin {

    private Class<? extends BaseMessage> summaryMessageCls = SystemMessage.class;

    public static String _DEFAULT_SUMMARY_PROMPT =
        "Progressively summarize the lines of conversation provided, adding onto"
            + " the previous summary returning a new summary.\n"
            + "\n"
            + "EXAMPLE\n"
            + "Current summary:\n"
            + "The human asks what the AI thinks of artificial intelligence. The AI thinks artificial intelligence is"
            + " a "
            + "force for good.\n"
            + "\n"
            + "New lines of conversation:\n"
            + "Human: Why do you think artificial intelligence is a force for good?\n"
            + "AI: Because artificial intelligence will help humans reach their full potential.\n"
            + "\n"
            + "New summary:\n"
            + "The human asks what the AI thinks of artificial intelligence. The AI thinks artificial intelligence is"
            + " a "
            + "force for good because it will help humans reach their full potential.\n"
            + "END OF EXAMPLE\n"
            + "\n"
            + "Current summary:\n"
            + "{summary}\n"
            + "\n"
            + "New lines of conversation:\n"
            + "{new_lines}\n"
            + "\n"
            + "New summary:";

    public static BasePromptTemplate prompt = new PromptTemplate(_DEFAULT_SUMMARY_PROMPT,
        Lists.newArrayList("summary", "new_lines"));

    public static String _DEFAULT_SUMMARY_PROMPT_CN =
        "逐步总结提供的对话内容，添加至"
            + "先前的总结，并返回一个新的总结。\n"
            + "\n"
            + "示例\n"
            + "当前总结:\n"
            + "人类询问AI对人工智能的看法。AI认为人工智能是"
            + "一种"
            + "积极的力量。\n"
            + "\n"
            + "新的对话内容:\n"
            + "人类: 你为什么认为人工智能是一种积极的力量？\n"
            + "AI: 因为人工智能将帮助人类发挥他们的全部潜力。\n"
            + "\n"
            + "新的总结:\n"
            + "人类询问AI对人工智能的看法。AI认为人工智能是"
            + "一种"
            + "积极的力量，因为它将帮助人类发挥他们的全部潜力。\n"
            + "示例结束\n"
            + "\n"
            + "当前总结:\n"
            + "{summary}\n"
            + "\n"
            + "新的对话内容:\n"
            + "{new_lines}\n"
            + "\n"
            + "新的总结:";

    public static BasePromptTemplate promptCn = new PromptTemplate(_DEFAULT_SUMMARY_PROMPT_CN,
        Lists.newArrayList("summary", "new_lines"));
}