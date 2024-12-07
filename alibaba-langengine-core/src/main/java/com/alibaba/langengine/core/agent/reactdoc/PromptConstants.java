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
package com.alibaba.langengine.core.agent.reactdoc;

import com.alibaba.langengine.core.prompt.impl.PromptTemplate;

import java.util.Arrays;

/**
 * PromptConstants
 *
 * @author xiaoxuan.lp
 */
public class PromptConstants {

    public static final String DEFAULT_TEMPLATE = "Question: What is the elevation range for the area that the eastern sector of the Colorado orogeny extends into?\n" +
            "Thought: I need to search Colorado orogeny, find the area that the eastern sector of the Colorado orogeny extends into, then find the elevation range of the area.\n" +
            "Action: Search[Colorado orogeny]\n" +
            "Observation: The Colorado orogeny was an episode of mountain building (an orogeny) in Colorado and surrounding areas.\n" +
            "Thought: It does not mention the eastern sector. So I need to look up eastern sector.\n" +
            "Action: Lookup[eastern sector]\n" +
            "Observation: (Result 1 / 1) The eastern sector extends into the High Plains and is called the Central Plains orogeny.\n" +
            "Thought: The eastern sector of Colorado orogeny extends into the High Plains. So I need to search High Plains and find its elevation range.\n" +
            "Action: Search[High Plains]\n" +
            "Observation: High Plains refers to one of two distinct land regions\n" +
            "Thought: I need to instead search High Plains (United States).\n" +
            "Action: Search[High Plains (United States)]\n" +
            "Observation: The High Plains are a subregion of the Great Plains. From east to west, the High Plains rise in elevation from around 1,800 to 7,000 ft (550 to 2,130 m).[3]\n" +
            "Thought: High Plains rise in elevation from around 1,800 to 7,000 ft, so the answer is 1,800 to 7,000 ft.\n" +
            "Action: Finish[1,800 to 7,000 ft]\n" +
            "\n" +
            "Question: Musician and satirist Allie Goertz wrote a song about the \"The Simpsons\" character Milhouse, who Matt Groening named after who?\n" +
            "Thought: The question simplifies to \"The Simpsons\" character Milhouse is named after who. I only need to search Milhouse and find who it is named after.\n" +
            "Action: Search[Milhouse]\n" +
            "Observation: Milhouse Mussolini Van Houten is a recurring character in the Fox animated television series The Simpsons voiced by Pamela Hayden and created by Matt Groening.\n" +
            "Thought: The paragraph does not tell who Milhouse is named after, maybe I can look up \"named after\".\n" +
            "Action: Lookup[named after]\n" +
            "Observation: (Result 1 / 1) Milhouse was named after U.S. president Richard Nixon, whose middle name was Milhous.\n" +
            "Thought: Milhouse was named after U.S. president Richard Nixon, so the answer is Richard Nixon.\n" +
            "Action: Finish[Richard Nixon]\n" +
            "\n" +
            "Question: Which documentary is about Finnish rock groups, Adam Clayton Powell or The Saimaa Gesture?\n" +
            "Thought: I need to search Adam Clayton Powell and The Saimaa Gesture, and find which documentary is about Finnish rock groups.\n" +
            "Action: Search[Adam Clayton Powell]\n" +
            "Observation: Could not find [Adam Clayton Powell]. Similar: [’Adam Clayton Powell III’, ’Seventh Avenue (Manhattan)’, ’Adam Clayton Powell Jr. State Office Building’, ’Isabel Washington Powell’, ’Adam Powell’, ’Adam Clayton Powell (film)’, ’Giancarlo Esposito’].\n" +
            "Thought: To find the documentary, I can search Adam Clayton Powell (film).\n" +
            "Action: Search[Adam Clayton Powell (film)]\n" +
            "Observation: Adam Clayton Powell is a 1989 American documentary film directed by Richard Kilberg. The film is about the rise and fall of influential African-American politician Adam Clayton Powell Jr.[3][4] It was later aired as part of the PBS series The American Experience.\n" +
            "Thought: Adam Clayton Powell (film) is a documentary about an African-American politician, not Finnish rock groups. So the documentary about Finnish rock groups must instead be The Saimaa Gesture.\n" +
            "Action: Finish[The Saimaa Gesture]\n" +
            "\n" +
            "Question: What profession does Nicholas Ray and Elia Kazan have in common?\n" +
            "Thought: I need to search Nicholas Ray and Elia Kazan, find their professions, then find the profession they have in common.\n" +
            "Action: Search[Nicholas Ray]\n" +
            "Observation: Nicholas Ray (born Raymond Nicholas Kienzle Jr., August 7, 1911 - June 16, 1979) was an American film director, screenwriter, and actor best known for the 1955 film Rebel Without a Cause.\n" +
            "Thought: Professions of Nicholas Ray are director, screenwriter, and actor. I need to search Elia Kazan next and find his professions.\n" +
            "Action: Search[Elia Kazan]\n" +
            "Observation: Elia Kazan was an American film and theatre director, producer, screenwriter and actor.\n" +
            "Thought: Professions of Elia Kazan are director, producer, screenwriter, and actor. So profession Nicholas Ray and Elia Kazan have in common is director, screenwriter, and actor.\n" +
            "Action: Finish[director, screenwriter, actor]\n" +
            "\n" +
            "Question: Which magazine was started first Arthur’s Magazine or First for Women?\n" +
            "Thought: I need to search Arthur’s Magazine and First for Women, and find which was started first.\n" +
            "Action: Search[Arthur’s Magazine]\n" +
            "Observation: Arthur’s Magazine (1844-1846) was an American literary periodical published in Philadelphia in the 19th century.\n" +
            "Thought: Arthur’s Magazine was started in 1844. I need to search First for Women next.\n" +
            "Action: Search[First for Women]\n" +
            "Observation: First for Women is a woman’s magazine published by Bauer Media Group in the USA.[1] The magazine was started in 1989.\n" +
            "Thought: First for Women was started in 1989. 1844 (Arthur’s Magazine) < 1989 (First for Women), so Arthur’s Magazine was started first.\n" +
            "Action: Finish[Arthur’s Magazine]\n" +
            "\n" +
            "Question: Were Pavel Urysohn and Leonid Levin known for the same type of work?\n" +
            "Thought: I need to search Pavel Urysohn and Leonid Levin, find their types of work, then find if they are the same.\n" +
            "Action: Search[Pavel Urysohn]\n" +
            "Observation: Pavel Samuilovich Urysohn (February 3, 1898 - August 17, 1924) was a Soviet mathematician who is best known for his contributions in dimension theory.\n" +
            "Thought: Pavel Urysohn is a mathematician. I need to search Leonid Levin next and find its type of work.\n" +
            "Action: Search[Leonid Levin]\n" +
            "Observation: Leonid Anatolievich Levin is a Soviet-American mathematician and computer scientist.\n" +
            "Thought: Leonid Levin is a mathematician and computer scientist. So Pavel Urysohn and Leonid Levin have the same type of work.\n" +
            "Action: Finish[yes]\n" +
            "\nQuestion: {input}\n" +
            "{agent_scratchpad}";
    public static final String DEFAULT_TEMPLATE_CH = "Question: 科罗拉多造山带东段延伸的区域的海拔范围是多少？\n" +
            "Thought: 我需要搜索科罗拉多造山带，找到科罗拉多造山带东段延伸到的区域，然后找到该区域的高程范围。\n" +
            "Action: 搜索[科罗拉多造山运动]\n" +
            "Observation: 科罗拉多造山运动是科罗拉多州及周边地区造山运动（造山运动）的一次事件。\n" +
            "Thought: 它没有提到东区。 所以我需要查找东区。\n" +
            "Action: 查找[东区]\n" +
            "Observation: （结果1 / 1）东段延伸至高原，称为中原造山运动。\n" +
            "Thought: 科罗拉多造山运动的东段延伸至高原。 所以我需要搜索高原并找到它的海拔范围。\n" +
            "Action: 搜索[高地平原]\n" +
            "Observation: 高原是指两个不同的陆地区域之一\n" +
            "Thought: 我需要搜索高地平原（美国）。\n" +
            "Action: 搜索[高地平原（美国）]\n" +
            "Observation: 高地平原是大平原的一个分区。 从东到西，高原的海拔从 1,800 英尺左右上升到 7,000 英尺（550 到 2,130 米）。[3]\n" +
            "Thought: 高原的海拔从大约 1,800 英尺上升到 7,000 英尺，所以答案是 1,800 到 7,000 英尺。\n" +
            "Action: 完成[1,800 至 7,000 英尺]\n" +
            "\n" +
            "Question: 音乐家兼讽刺作家艾莉·戈尔茨写了一首关于《辛普森一家》角色米尔豪斯的歌曲，马特·格勒宁以谁的名字命名了米尔豪斯？\n" +
            "Thought: 问题简化为“辛普森一家”角色米尔豪斯是以谁的名字命名的。 我只需要搜索米尔豪斯并找到它是以谁命名的。\n" +
            "Action: 搜索[米尔豪斯]\n" +
            "Observation: 米尔豪斯·墨索里尼·范·豪顿是福克斯动画电视剧《辛普森一家》中的一个经常出现的角色，由帕梅拉·海登配音，马特·格罗宁创作。\n" +
            "Thought: 该段没有告诉米尔豪斯是以谁的名字命名的，也许我可以查一下“命名为”。\n" +
            "Action: 查找[命名为]\n" +
            "Observation: （结果 1 / 1）米尔豪斯是以美国总统理查德·尼克松的名字命名的，他的中间名是米尔豪斯。\n" +
            "Thought: 米尔豪斯是以美国总统理查德·尼克松的名字命名的，所以答案是理查德·尼克松。\n" +
            "Action: 完成[理查德·尼克松]\n" +
            "\n" +
            "Question: 哪部纪录片是关于芬兰摇滚乐队 Adam Clayton Powell 或 The Saimaa Gesture 的？\n" +
            "Thought: 我需要搜索 Adam Clayton Powell 和 The Saimaa Gesture，找到哪部纪录片是关于芬兰摇滚乐队的。\n" +
            "Action: 搜索[亚当·克莱顿·鲍威尔]\n" +
            "Observation: 找不到[亚当·克莱顿·鲍威尔]。 类似：[“亚当·克莱顿·鲍威尔三世”、“第七大道（曼哈顿）”、“小亚当·克莱顿·鲍威尔国家办公大楼”、“伊莎贝尔·华盛顿·鲍威尔”、“亚当·鲍威尔”、“亚当·克莱顿·鲍威尔（电影）”、“ 吉安卡洛·埃斯波西托']。\n" +
            "Thought: 要找到这部纪录片，我可以搜索亚当·克莱顿·鲍威尔（电影）。\n" +
            "Action: 搜索[亚当·克莱顿·鲍威尔（电影）]\n" +
            "Observation: 《亚当·克莱顿·鲍威尔》是一部 1989 年的美国纪录片，由理查德·基尔伯格执导。 这部电影讲述了颇具影响力的非裔美国政治家小亚当·克莱顿·鲍威尔的兴衰[3][4] 它后来作为 PBS 系列“美国经历”的一部分播出。\n" +
            "Thought: 亚当·克莱顿·鲍威尔（电影）是一部关于非裔美国政治家的纪录片，而不是芬兰摇滚乐队的纪录片。 所以关于芬兰摇滚乐队的纪录片必须是《The Saimaa Gesture》。\n" +
            "Action: 完成[塞马手势]\n" +
            "\n" +
            "Question: 尼古拉斯·雷和埃利亚·卡赞的职业有什么共同点？\n" +
            "Thought: 我需要搜索 Nicholas Ray 和 Elia Kazan，找到他们的职业，然后找到他们的共同点。\n" +
            "Action: 搜索[尼古拉斯·雷]\n" +
            "Observation: 尼古拉斯·雷（原名雷蒙德·尼古拉斯·金茨勒，1911 年 8 月 7 日至 1979 年 6 月 16 日）是一位美国电影导演、编剧和演员，因 1955 年的电影《无因的反叛》而闻名。\n" +
            "Thought: 尼古拉斯·雷的职业是导演、编剧和演员。 接下来我需要搜索 Elia Kazan 并找到他的职业。\n" +
            "Action: 搜索[埃利亚·卡赞]\n" +
            "Observation: 埃利亚·卡赞是一位美国电影和戏剧导演、制片人、编剧和演员。\n" +
            "Thought: 埃利亚·卡赞的职业是导演、制片人、编剧和演员。 尼古拉斯·雷和埃利亚·卡赞的职业有一个共同点，那就是导演、编剧和演员。\n" +
            "Action: 完成[导演、编剧、演员]\n" +
            "\n" +
            "Question: 哪一本杂志最先创办，是《亚瑟杂志》还是《First for Women》？\n" +
            "Thought: 我需要搜索《亚瑟杂志》和《First for Women》，找出哪一个最先开始。\n" +
            "Action: 搜索[亚瑟杂志]\n" +
            "Observation: 《亚瑟杂志》（Arthur’s Magazine，1844-1846）是 19 世纪在费城出版的美国文学期刊。\n" +
            "Thought: 《亚瑟杂志》创刊于 1844 年。接下来我需要搜索《First for Women》。\n" +
            "Action: 搜索[女性优先]\n" +
            "Observation: 《First for Women》是美国鲍尔传媒集团出版的一本女性杂志。[1] 该杂志于1989年创刊。\n" +
            "Thought: 《First for Women》创刊于 1989 年。1844 年（《亚瑟杂志》）< 1989 年（《First for Women》），所以《亚瑟杂志》是先创刊的。\n" +
            "Action: 完成[亚瑟的杂志]\n" +
            "\n" +
            "Question: Pavel Urysohn 和 Leonid Levin 是否因同一类型的作品而闻名？\n" +
            "Thought: 我需要搜索 Pavel Urysohn 和 Leonid Levin，找到他们的工作类型，然后看看他们是否相同。\n" +
            "Action: 搜索[Pavel Urysohn]\n" +
            "Observation: 帕维尔·萨梅伊洛维奇·尤里森（Pavel Samuilovich Urysohn，1898年2月3日 - 1924年8月17日）是一位苏联数学家，以其在维度理论方面的贡献而闻名。\n" +
            "Thought: Pavel Urysohn 是一位数学家。 接下来我需要搜索 Leonid Levin 并找到它的工作类型。\n" +
            "Action: 搜索[列昂尼德·莱文]\n" +
            "Observation: 列昂尼德·阿纳托利耶维奇·莱文是一位苏美数学家和计算机科学家。\n" +
            "Thought: Leonid Levin 是一位数学家和计算机科学家。 所以帕维尔·尤里森（Pavel Urysohn）和列昂尼德·莱文（Leonid Levin）有相同类型的工作。\n" +
            "Action: 完成[是]\n" +
            "\nQuestion: {input}\n" +
            "{agent_scratchpad}";

    public static final PromptTemplate PROMPT = new PromptTemplate(DEFAULT_TEMPLATE, Arrays.asList(new String[]{ "input", "agent_scratchpad" }));
    public static final PromptTemplate PROMPT_CH = new PromptTemplate(DEFAULT_TEMPLATE_CH, Arrays.asList(new String[]{ "input", "agent_scratchpad" }));
}
