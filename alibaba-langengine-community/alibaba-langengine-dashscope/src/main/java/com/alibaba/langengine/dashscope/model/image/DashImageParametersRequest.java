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
package com.alibaba.langengine.dashscope.model.image;

import lombok.*;

/**
 * @author chenshuaixin
 * @date 2024/05/17
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DashImageParametersRequest {
    /**
     * 输出图像的风格，目前支持以下风格取值：
     * "<auto>" 默认，
     * "<3d cartoon>" 3D卡通,
     * "<anime>" 动画,
     * "<oil painting>" 油画,
     * "<watercolor>"水彩,
     * "<sketch>" 素描,
     * "<chinese painting>" 中国画,
     * "<flat illustration>" 扁平插画
     */
    private String style;
    /**
     * 非必填
     * 生成图像的分辨率，目前仅支持'1024*1024', '720*1280', '1280*720'三种分辨率，默认为1024*1024像素。
     * example: "size": "1024*1024"
     */
    private String size;
    /**
     * 非必填
     * 本次请求生成的图片数量，目前支持1-4张，默认为1
     * "n": 4
     */
    private Integer n;
    /**
     * 非必填
     * 图片生成时候的种子值，取值范围为(0,4294967290) 。如果不提供，则算法自动用一个随机生成的数字作为种子，如果给定了，则根据 batch 数量分别生成 seed, seed+1, seed+2, seed+3 为参数的图片。
     */
    private Integer seed;
    /**
     * 非必填
     * 期望输出结果与垫图（参考图）的相似度，取值范围[0.0, 1.0], 数字越大，生成的结果与参考图越相似
     */
    private Float ref_strength;
    /**
     * 非必填
     * 呼应input里的参考图片
     * 垫图（参考图）生图使用的生成方式，可选值为'repaint' （默认） 和 'refonly'; 其中 repaint代表参考内容，refonly代表参考风格
     * example："repaint"
     */
    private String ref_mode;
}
