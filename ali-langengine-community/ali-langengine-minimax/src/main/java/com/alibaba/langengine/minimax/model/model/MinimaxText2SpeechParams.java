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

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * @author aihe
 *
 * Minimax的文本生成音频参数，对应文档地址：
 * https://api.minimax.chat/document/guides/T2A-model/tts/api?id=6569c8be48bc7b684b3037df
 */
@Data
public class MinimaxText2SpeechParams {
    /**
     * 暂时只支持系统音色(id)：青涩青年音色(male-qn-qingse)精英青年音色(male-qn-jingying)霸道青年音色(male-qn-badao)青年大学生音色
     * (male-qn-daxuesheng)少女音色(female-shaonv)御姐音色(female-yujie)成熟女性音色(female-chengshu)甜美女性音色(female-tianmei)
     * 男性主持人(presenter_male)女性主持人(presenter_female)男性有声书1(audiobook_male_1)男性有声书2(audiobook_male_2)女性有声书1
     * (audiobook_female_1)女性有声书2(audiobook_female_2)
     * 青涩青年音色-beta（male-qn-qingse-jingpin）精英青年音色-beta（male-qn-jingying-jingpin）霸道青年音色-beta（male-qn-badao-jingpin
     * ）青年大学生音色-beta（male-qn-daxuesheng-jingpin）少女音色-beta（female-shaonv-jingpin）御姐音色-beta（female-yujie-jingpin
     * ）成熟女性音色-beta（female-chengshu-jingpin）甜美女性音色-beta（female-tianmei-jingpin）
     */
    @JSONField(name = "voice_id")
    public String voiceId;

    /**
     * 期望生成声音的文本，长度限制<500字符
     */
    public String text;

    /**
     * 默认是 "model": "speech-01",
     */
    public String model;

    /**
     * 范围[0.5, 2],可选，默认值为1.0取值越大，语速越快
     */
    public Double speed;

    /**
     * 可选，默认值为1.0取值越大，音量越高
     * 范围(0, 10]
     */
    public Double vol;

    /**
     * 生成声音的语调,范围[-12, 12]
     * 可选，默认值为0（0为原音色输出，取值需为整数）
     */
    public Integer pitch;

    @JSONField(name = "timber_weights")
    public List<TimberWeight> timberWeights;

    /**
     * 用来进行多个声音合成
     */
    @Data
    public static class TimberWeight {
        @JSONField(name = "voice_id")
        public String voiceId;
        public int weight;
    }
}
