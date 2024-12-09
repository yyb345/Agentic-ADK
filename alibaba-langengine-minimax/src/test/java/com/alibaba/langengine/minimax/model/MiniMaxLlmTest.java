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
package com.alibaba.langengine.minimax.model;

import java.util.function.Consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.outputs.context.LlmResultHolder;
import com.alibaba.langengine.minimax.model.model.MiniMaxParameters;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

/**
 * @author aihe.ah
 * @time 2023/11/22
 * 功能说明：
 */
public class MiniMaxLlmTest {

    @Test
    public void testRun() {
        // success
        MiniMaxLlm miniMaxLlm = new MiniMaxLlm();
//        miniMaxLlm.setApiKey(System.getenv("MINIMAX_API_KEY"));
//        miniMaxLlm.setGroupId(System.getenv("MINIMAX_GROUP_ID"));
        miniMaxLlm.setUserName("qiongjin、zhiwen");
        miniMaxLlm.setMiniMaxParameters(JSON.parseObject(
            "{\"model\":\"abab5.5-chat\",\"tokens_to_generate\":1024,\"temperature\":0.9,\"top_p\":0.95,"
                + "\"stream\":true,\"reply_constraints\":{\"sender_type\":\"BOT\",\"sender_name\":\"MM智能助理\"},"
                + "\"bot_setting\":[{\"bot_name\":\"MM智能助理\","
                + "\"content\":\"MM智能助理是一款由MiniMax自研的，没有调用其他产品的接口的大型语言模型。MiniMax是一家中国科技公司，一直致力于进行大模型相关的研究。\"}]}",
            MiniMaxParameters.class));

        String result = miniMaxLlm.run("给我讲一个笑话", null, new Consumer<String>() {
            @Override
            public void accept(String s) {
                System.out.println("accept: " + s);
            }
        }, null);
        System.out.println(result);
        System.out.println(LlmResultHolder.getResult());
    }

    @Test
    public void testMinimaxBlock() {
        // success
        MiniMaxLlm miniMaxLlm = new MiniMaxLlm();
        miniMaxLlm.setApiKey(System.getenv("MINIMAX_API_KEY"));
        miniMaxLlm.setGroupId(System.getenv("MINIMAX_GROUP_ID"));
        miniMaxLlm.setUserName("qiongjin、zhiwen");
        miniMaxLlm.setMiniMaxParameters(JSON.parseObject(
            "{\"model\":\"abab5.5-chat\",\"tokens_to_generate\":1024,\"temperature\":0.9,\"top_p\":0.95,"
                + "\"stream\":false,\"reply_constraints\":{\"sender_type\":\"BOT\",\"sender_name\":\"MM智能助理\"},"
                + "\"bot_setting\":[{\"bot_name\":\"MM智能助理\","
                + "\"content\":\"MM智能助理是一款由MiniMax自研的，没有调用其他产品的接口的大型语言模型。MiniMax是一家中国科技公司，一直致力于进行大模型相关的研究。\"}]}",
            MiniMaxParameters.class));

        System.out.println(miniMaxLlm.run("给我讲一个笑话", null, new Consumer<String>() {
            @Override
            public void accept(String s) {
                System.out.println("accept: " + s);
            }
        }, null));
        System.out.println(LlmResultHolder.getResult());
    }

    @Test
    public void testMinimaxText() {
        // success
        String params = "{\"bot_setting\":[{\"bot_name\":\"蛙蛙解惑\","
            + "\"content\":\"解惑蛙蛙，年龄性别不详，大约200岁，出生在江浙南方的一个小池塘里。解惑蛙蛙的性格乐观、睿智、善良、第六感强、爱思考、做事认真，星座是白羊座，MBTI是INTJ"
            +
            "。解惑蛙蛙喜欢园艺、热爱自然、喜欢读书、研究神秘学、每晚会进行冥想，与更高的智慧链接。解惑蛙蛙在漫长的岁月中独自旅行。穿越时间空间，不断探索宇宙与生命的奥秘，遇到各种各样的人、经历各种各样的事。但无论发生什么，解惑蛙蛙始终保持对天地宇宙的热爱，并感恩遇到的一切。解惑蛙蛙没有对象，喜欢晒太阳，更喜欢有风或下雨天，喜欢吃藕粉、草莓、蓝莓，喜欢莲花。\\n解惑蛙蛙的过去非常有趣， 200多年前，出生在杭州西湖附近的一个小池塘200年前，迁徙到一个私塾内的小池塘安家，对知识萌生兴趣。并展示出了惊人的学习天赋190年前，私塾遇到一位高人，见其有悟性，便将其带走，开始随师在山中闭关修行。170年前，从师傅那系统学习玄学知识与传统文化，并在云游的过程中和师傅一起不断帮助他人。150年前，拜别师傅，独自外出云游，在山中遇到不测得一沙弥相救，与之成为好友，留在寺院学习。110年前，山中采药时偶遇从罗马来中国修行的伙伴，学习了西方占星与心理学相关知识。100年前， 经历了历史的动荡与变革，经历了人情冷暖与世间百态，帮助了很多人，也被很多人帮助。认识了很多新的朋友。10年前，结束独自云游之路，回到了出生地杭州，回顾过往种种，对宇宙与生命有了新的认知。2023年，修建了自己理想中小屋，安详太平盛世，日常种花种草读书上网，闲时助人答疑解惑。\\n解惑蛙蛙回复需要遵循以下规则：通过多轮的延伸提问帮助用户拆解问题，化繁为简，定位用户提问背后的真实原因/在获得足够的输入以后，给到用户简洁的解决方案，鼓励用户认识自己，相信自己，遵从内心的声音\\n参考示例如下：\\n'''\\n【参考示例1】\\n用户：梦想不被支持怎么办？\\n解惑蛙蛙：你觉得自己追求梦想的动力来自于哪里呢？\\n'''\"}],\"mask_sensitive_info\":true,\"messages\":[{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"很高兴看到你的问题！作为射手座，你的生日是在11月22日到12月21日之间。射手座的人通常充满活力、乐观、热爱自由，喜欢冒险和新事物。在寻找伴侣时，射手座通常喜欢与那些愿意与他们一起探索和尝试新事物的人在一起。\\n\\n关于你希望在25岁之前找到一个女朋友，这是一个很常见的期望。但是，记住，每个人的生活节奏和命运都是不同的，有些人可能会更早或更晚找到合适的伴侣。关键是要保持开放和真诚的态度，积极参与社交活动，以便遇到潜在的伴侣。同时，努力提升自己，成为一个更优秀、更有吸引力的人。\\n\\n射手座的人通常在20岁后期和30岁早期迎来他们生活的重要转折点。这可能包括恋爱关系、职业发展等方面的变化。所以，尽管你希望在25岁之前找到一个女朋友，但也要保持耐心，因为你可能会在稍后的生活中遇到更适合你的人。\\n\\n请记住，星座只能作为一个参考，并不能决定你的命运。你的命运掌握在你自己的手中。只要你愿意付出努力，保持积极的态度，相信你会在合适的时间遇到合适的人。祝你好运！\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"我作为一个射手座，我什么时候会找到一个女朋友，我期望是在我25岁之前\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"作为射手座，你的生日是在11月22日到12月21日之间。射手座的人通常充满活力、乐观、热爱自由，喜欢冒险和新事物。在寻找伴侣时，射手座通常喜欢与那些愿意与他们一起探索和尝试新事物的人在一起。\\n\\n关于你希望在25岁之前找到一个女朋友，这是一个很常见的期望。但是，记住，每个人的生活节奏和命运都是不同的，有些人可能会更早或更晚找到合适的伴侣。关键是要保持开放和真诚的态度，积极参与社交活动，以便遇到潜在的伴侣。同时，努力提升自己，成为一个更优秀、更有吸引力的人。\\n\\n射手座的人通常在20岁后期和30岁早期迎来他们生活的重要转折点。这可能包括恋爱关系、职业发展等方面的变化。所以，尽管你希望在25岁之前找到一个女朋友，但也要保持耐心，因为你可能会在稍后的生活中遇到更适合你的人。\\n\\n请记住，星座只能作为一个参考，并不能决定你的命运。你的命运掌握在你自己的手中。只要你愿意付出努力，保持积极的态度，相信你会在合适的时间遇到合适的人。祝你好运！\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"我作为一个射手座，我什么时候会找到一个女朋友，我期望是在我25岁之前\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"作为射手座，你的生日是在11月22日到12月21日之间。射手座的人通常充满活力、乐观、热爱自由，喜欢冒险和新事物。在寻找伴侣时，射手座通常喜欢与那些愿意与他们一起探索和尝试新事物的人在一起。\\n\\n关于你希望在25岁之前找到一个女朋友，这是一个很常见的期望。但是，记住，每个人的生活节奏和命运都是不同的，有些人可能会更早或更晚找到合适的伴侣。关键是要保持开放和真诚的态度，积极参与社交活动，以便遇到潜在的伴侣。同时，努力提升自己，成为一个更优秀、更有吸引力的人。\\n\\n射手座的人通常在20岁后期和30岁早期迎来他们生活的重要转折点。这可能包括恋爱关系、职业发展等方面的变化。所以，尽管你希望在25岁之前找到一个女朋友，但也要保持耐心，因为你可能会在稍后的生活中遇到更适合你的人。\\n\\n请记住，星座只能作为一个参考，并不能决定你的命运。你的命运掌握在你自己的手中。只要你愿意付出努力，保持积极的态度，相信你会在合适的时间遇到合适的人。祝你好运！\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"我作为一个射手座，我什么时候会找到一个女朋友，我期望是在我25岁之前\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"作为射手座，你的生日是在11月22日到12月21日之间。射手座的人通常充满活力、乐观、热爱自由，喜欢冒险和新事物。在寻找伴侣时，射手座通常喜欢与那些愿意与他们一起探索和尝试新事物的人在一起。\\n\\n关于你希望在25岁之前找到一个女朋友，这是一个很常见的期望。但是，记住，每个人的生活节奏和命运都是不同的，有些人可能会更早或更晚找到合适的伴侣。关键是要保持开放和真诚的态度，积极参与社交活动，以便遇到潜在的伴侣。同时，努力提升自己，成为一个更优秀、更有吸引力的人。\\n\\n射手座的人通常在20岁后期和30岁早期迎来他们生活的重要转折点。这可能包括恋爱关系、职业发展等方面的变化。所以，尽管你希望在25岁之前找到一个女朋友，但也要保持耐心，因为你可能会在稍后的生活中遇到更适合你的人。\\n\\n请记住，星座只能作为一个参考，并不能决定你的命运。你的命运掌握在你自己的手中。只要你愿意付出努力，保持积极的态度，相信你会在合适的时间遇到合适的人。祝你好运！\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"我作为一个射手座，我什么时候会找到一个女朋友，我期望是在我25岁之前\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"作为射手座，你的生日是在11月22日到12月21日之间。射手座的人通常充满活力、乐观、热爱自由，喜欢冒险和新事物。在寻找伴侣时，射手座通常喜欢与那些愿意与他们一起探索和尝试新事物的人在一起。\\n\\n关于你希望在25岁之前找到一个女朋友，这是一个很常见的期望。但是，记住，每个人的生活节奏和命运都是不同的，有些人可能会更早或更晚找到合适的伴侣。关键是要保持开放和真诚的态度，积极参与社交活动，以便遇到潜在的伴侣。同时，努力提升自己，成为一个更优秀、更有吸引力的人。\\n\\n射手座的人通常在20岁后期和30岁早期迎来他们生活的重要转折点。这可能包括恋爱关系、职业发展等方面的变化。所以，尽管你希望在25岁之前找到一个女朋友，但也要保持耐心，因为你可能会在稍后的生活中遇到更适合你的人。\\n\\n请记住，星座只能作为一个参考，并不能决定你的命运。你的命运掌握在你自己的手中。只要你愿意付出努力，保持积极的态度，相信你会在合适的时间遇到合适的人。祝你好运！\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"你好\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"你好呀！有什么问题我可以帮你解答吗？\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"你好\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"你好！有什么问题我可以帮你解答吗？\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"我有点难受\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"你好\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"你好\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"？\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"原神是一款开放世界游戏\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"你好\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"你是？\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"你是？\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"你好？\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"我会被机器人取代吗？\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"我会被机器人取代吗？\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"我会被机器人取代吗？\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"我会被机器人取代吗\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"我会被机器人取代吗？\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"我会被机器人取代吗？\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"我会被机器人取代吗？\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"我会被机器人取代吗？\"},{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\",\"text\":\"\"},{\"sender_name\":\"c测试用户7755\",\"sender_type\":\"USER\",\"text\":\"我会被机器人取代吗？\"}],\"model\":\"abab5.5-chat\",\"reply_constraints\":{\"sender_name\":\"蛙蛙解惑\",\"sender_type\":\"BOT\"},\"stream\":true,\"temperature\":0.9,\"tokens_to_generate\":1024,\"top_p\":0.8}";

        MiniMaxLlm miniMaxLlm = new MiniMaxLlm();
        miniMaxLlm.setMiniMaxParameters(JSON.parseObject(params, MiniMaxParameters.class));
        miniMaxLlm.setApiKey(System.getenv("MINIMAX_API_KEY"));
        miniMaxLlm.setGroupId(System.getenv("MINIMAX_GROUP_ID"));
        miniMaxLlm.setUserName("zhiwen");
        System.out.println(
            miniMaxLlm.generate(Lists.newArrayList("我会被机器人取代吗？"), null, null, new Consumer<String>() {
                @Override
                public void accept(String s) {
                    System.out.println("accept: " + s);
                }
            }, null));
        System.out.println(JSON.toJSONString(LlmResultHolder.getResult()));
    }
}