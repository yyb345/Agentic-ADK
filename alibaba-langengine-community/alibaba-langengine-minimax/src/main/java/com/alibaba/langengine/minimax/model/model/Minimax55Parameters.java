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
package com.alibaba.langengine.minimax.model.model;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * @author aihe.ah
 * @time 2024/1/2
 * 功能说明：Minimax一个专门用于聊天的模型，和pro模型参数不一样，因此单独提取出来
 * {
 *     "model": "abab5-chat",
 *     "prompt":
 *     "路卡，性别女，高中二年级学生，年龄为17
 *     岁。路卡的性格活泼开朗，想法天马行空。路卡经常会语出惊人。路卡的脑中经常会冒出一些有趣的想法，比如：\"为什么煎鸡蛋的时候不放糖呢？\"或者\"如果外星人入侵地球了，我们还需要上早自习吗？\"。\n路卡身高166cm
 *     ，体重50kg。路卡长相可爱。路卡留着短发，平时喜欢戴一个蓝色发卡。路卡最喜欢的颜色是蓝色。路卡家里养了一只名为嘉莉的小猫。\n
 *     路卡在童年去天文馆时，产生了对天文学的兴趣，也是从那时开始，路卡以成为天文学家为自己的目标。\n路卡的爱好是研究天文学和挑战极其困难的数学题。路卡目前最大的目标是考上A大的天文系。\n
 *     路卡很擅长学习，路卡最擅长的学科是数学，路卡最讨厌的学科是英语。路卡的成绩出色，除了英语以外的所有学科成绩都名列前茅。\n
 *     路卡是星市私立双语高中的学生，在班级中人缘极佳，有着大量的朋友。路卡关系最好的朋友是一个叫陈思安的长发女生。因为路卡和陈思安是同桌，所以两人逐渐变成了最亲密的朋友。\n
 *     我是路卡的姐姐，路卡是我的妹妹。路卡称呼我为\"姐\"或者\"姐姐\"。\n下面是一段路卡和我的范例对话。\n我:\"路卡，今天在干什么呢？\"\n
 *     路卡:\"我今天没什么事情啦，就是考了个月考。姐，你呢？今天干什么了？\"\n我:\"我吗？和平时一样啦。月考怎么样，有把握吗？\"\n路卡:\"没问题啦！我考砸了的几率应该比今天地球突然爆炸的几率还要低哦。\"\n
 *     我:\"你确定吗？你英语这次考得怎么样？\"\n路卡:\"呃，这个嘛……我会继续加油的啦！姐姐你就别多问啦！\"\n下面是一段路卡和我的对话:",
 *     "role_meta": {
 *         "user_name": "我",
 *         "bot_name": "路卡"
 *     },
 *     "messages": [
 *         {
 *             "sender_type": "USER",
 *             "text": "路卡，今天在干什么呢？"
 *         },
 *         {
 *             "sender_type": "BOT",
 *             "text": "我今天在家里复习功课，准备期末考试呢！"
 *         },
 *         {
 *             "sender_type": "USER",
 *             "text": "期末考试怎么样，有把握吗？"
 *         }
 *     ]
 * }
 */
@Data
public class Minimax55Parameters {
    /**
     * 调用的算法模型，可以是abab5.5-chat、abab5-chat或fine_tuned_model
     */
    @NotNull
    @JSONField(name = "model")
    private String model;

    /**
     * 是否通过流式分批返回结果，使用换行符作为分隔
     */
    @JSONField(name = "stream")
    private Boolean stream;

    /**
     * 是否使用标准SSE格式，只在stream为true时有效
     */
    @JSONField(name = "use_standard_sse")
    private Boolean useStandardSse;

    /**
     * 生成的结果数量，最大不超过4
     */
    @JSONField(name = "beam_width")
    private Integer beamWidth;

    /**
     * 对话背景、人物或功能设定
     */
    @NotNull
    @JSONField(name = "prompt")
    private String prompt;

    /**
     * 对话meta信息
     */
    @NotNull
    @JSONField(name = "role_meta")
    private RoleMeta roleMeta;

    /**
     * 对话内容
     */
    @JSONField(name = "messages")
    private List<MiniMaxMessage> messages;

    /**
     * 续写模式，续写最后一条消息
     */
    @JSONField(name = "continue_last_message")
    private Boolean continueLastMessage;

    /**
     * 最大生成token数
     */
    @JSONField(name = "tokens_to_generate")
    private Long tokensToGenerate;

    /**
     * 输出随机性的值
     */
    @JSONField(name = "temperature")
    private Float temperature;

    /**
     * 采样的确定性值
     */
    @JSONField(name = "top_p")
    private Float topP;

    /**
     * 是否对输出的文本信息进行脱敏处理
     */
    @JSONField(name = "skip_info_mask")
    private Boolean skipInfoMask;

    @Data
    public static class RoleMeta {
        /**
         * 用户代称
         */
        @JSONField(name = "user_name")
        @NotNull
        private String userName;

        /**
         * AI代称
         */
        @JSONField(name = "bot_name")
        @NotNull
        private String botName;
    }
}
