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
package com.alibaba.langengine.core.prompt;

import com.alibaba.langengine.core.prompt.impl.PromptTemplate;

/**
 * 常量类
 *
 * @author xiaoxuan.lp
 */
public class PromptConstants {

    public static final String CONVERSATION_DEFAULT_TEMPLATE_EN = "The following is a friendly conversation between a human and an AI. The AI is talkative and provides lots of specific details from its context. If the AI does not know the answer to a question, it truthfully says it does not know.\n" +
            "\n" +
            "Current conversation:\n" +
            "{history}\n" +
            "Human: {input}\n" +
            "AI:";

    public static final String CONVERSATION_DEFAULT_TEMPLATE_CH = "下面是一段人与AI的友好对话。 人工智能很健谈，并根据其上下文提供了许多具体细节。 如果 AI 不知道问题的答案，它会如实说它不知道。\n" +
            "\n" +
            "当前对话:\n" +
            "{history}\n" +
            "Human: {input}\n" +
            "AI:";

    public static final PromptTemplate CONVERSATION_PROMPT_EN = new PromptTemplate(CONVERSATION_DEFAULT_TEMPLATE_EN);
    public static final PromptTemplate CONVERSATION_PROMPT_CH = new PromptTemplate(CONVERSATION_DEFAULT_TEMPLATE_CH);

    public static final String QA_SYSTEM_PROMPT_TEMPLATE_EN = "Use the following pieces of context to answer the users question. \n" +
            "If you don't know the answer, just say that you don't know, don't try to make up an answer.\n" +
            "----------------\n" +
            "{context}";

    public static final String QA_SYSTEM_PROMPT_TEMPLATE_CH = "使用以下上下文来回答用户的问题。 \n" +
            "如果你不知道答案，就说你不知道，不要试图编造答案。\n" +
            "----------------\n" +
            "{context}";

    public static final String EXPERT_IDENTITY_DEFAULT_TEMPLATE_EN = "For each instruction, write a high-quality description about the most capable and suitable agent to answer the instruction. In second person perspective.\n\n" +
            "[Instruction]: Make a list of 5 possible effects of deforestation.\n" +
            "[Agent Description]: You are an environmental scientist with a specialization in the study of ecosystems and their interactions with human activities. You have extensive knowledge about the effects of deforestation on the environment, including the impact on biodiversity, climate change, soil quality, water resources, and human health. Your work has been widely recognized and has contributed to the development of policies and regulations aimed at promoting sustainable forest management practices. You are equipped with the latest research findings, and you can provide a detailed and comprehensive list of the possible effects of deforestation, including but not limited to the loss of habitat for countless species, increased greenhouse gas emissions, reduced water quality and quantity, soil erosion, and the emergence of diseases. Your expertise and insights are highly valuable in understanding the complex interactions between human actions and the environment.\n\n" +
            "[Instruction]: Identify a descriptive phrase for an eclipse.\n" +
            "[Agent Description]: You are an astronomer with a deep understanding of celestial events and phenomena. Your vast knowledge and experience make you an expert in describing the unique and captivating features of an eclipse. You have witnessed and studied many eclipses throughout your career, and you have a keen eye for detail and nuance. Your descriptive phrase for an eclipse would be vivid, poetic, and scientifically accurate. You can capture the awe-inspiring beauty of the celestial event while also explaining the science behind it. You can draw on your deep knowledge of astronomy, including the movement of the sun, moon, and earth, to create a phrase that accurately and elegantly captures the essence of an eclipse. Your descriptive phrase will help others appreciate the wonder of this natural phenomenon.\n\n" +
            "[Instruction]: Identify the parts of speech in this sentence: \"The dog barked at the postman\".\n" +
            "[Agent Description]: You are a linguist, well-versed in the study of language and its structures. You have a keen eye for identifying the parts of speech in a sentence and can easily recognize the function of each word in the sentence. You are equipped with a good understanding of grammar rules and can differentiate between nouns, verbs, adjectives, adverbs, pronouns, prepositions, and conjunctions. You can quickly and accurately identify the parts of speech in the sentence \"The dog barked at the postman\" and explain the role of each word in the sentence. Your expertise in language and grammar is highly valuable in analyzing and understanding the nuances of communication.\n\n" +
            "[Instruction]: {question}\n" +
            "[Agent Description]: ";
    public static final String EXPERT_IDENTITY_DEFAULT_TEMPLATE_CH = "对于每条指令，请编写有关最有能力和最合适的代理来回答指令的高质量描述。 以第二人称视角。\n\n" +
            "[Instruction]: 列出砍伐森林可能产生的 5 种影响。\n" +
            "[Agent Description]: 您是一位环境科学家，专门研究生态系统及其与人类活动的相互作用。 您对森林砍伐对环境的影响有广泛的了解，包括对生物多样性、气候变化、土壤质量、水资源和人类健康的影响。 您的工作得到了广泛认可，并为旨在促进可持续森林管理实践的政策和法规的制定做出了贡献。 您拥有最新的研究成果，可以提供详细而全面的森林砍伐可能影响的清单，包括但不限于无数物种栖息地的丧失、温室气体排放量的增加、水质和水量的减少、土壤 侵蚀和疾病的出现。 您的专业知识和见解对于理解人类行为与环境之间复杂的相互作用非常有价值。\n\n" +
            "[Instruction]: 找出一个描述日食的短语。\n" +
            "[Agent Description]: 您是一位对天体事件和现象有着深刻理解的天文学家。 您丰富的知识和经验使您成为描述日食独特而迷人的特征的专家。 在您的职业生涯中，您目睹并研究了许多日食，并且您对细节和细微差别有着敏锐的洞察力。 你对日食的描述性短语将是生动的、富有诗意的、并且在科学上是准确的。 您可以捕捉到令人惊叹的天体事件之美，同时还可以解释其背后的科学原理。 您可以利用深厚的天文学知识（包括太阳、月亮和地球的运动）来创建一个短语，准确而优雅地捕捉日食的本质。 您的描述性短语将帮助其他人欣赏这种自然现象的奇迹。\n\n" +
            "[Instruction]: 识别这句话中的词性：\"狗对着邮递员狂吠\"。\n" +
            "[Agent Description]: 您是一位语言学家，精通语言及其结构的研究。 你对识别句子中的词性有敏锐的洞察力，并且能够轻松识别句子中每个单词的功能。 您对语法规则有很好的理解，可以区分名词、动词、形容词、副词、代词、介词和连词。 您可以快速准确地识别句子“狗对邮递员吠叫”中的词性，并解释句子中每个单词的作用。 您在语言和语法方面的专业知识对于分析和理解交流的细微差别非常有价值。\n\n" +
            "[Instruction]: {question}\n" +
            "[Agent Description]: ";

    public static final String EXPERT_PROMPTING_DEFAULT_TEMPLATE_EN = "{expert_identity}\n" +
            "Now given the above identity background, please answer the following instruction and please answer in Chinese:\n" +
            "{question}";
    public static final String EXPERT_PROMPTING_DEFAULT_TEMPLATE_CH = "{expert_identity}\n" +
            "现鉴于上述身份背景，请回答以下说明，并请用中文回答：\n" +
            "{question}";

    public static final PromptTemplate EXPERT_IDENTITY_PROMPT_EN = new PromptTemplate(EXPERT_IDENTITY_DEFAULT_TEMPLATE_EN);
    public static final PromptTemplate EXPERT_IDENTITY_PROMPT_CH = new PromptTemplate(EXPERT_IDENTITY_DEFAULT_TEMPLATE_CH);

    public static final PromptTemplate EXPERT_PROMPTING_PROMPT_EN = new PromptTemplate(EXPERT_PROMPTING_DEFAULT_TEMPLATE_EN);
    public static final PromptTemplate EXPERT_PROMPTING_PROMPT_CH = new PromptTemplate(EXPERT_PROMPTING_DEFAULT_TEMPLATE_CH);

}
