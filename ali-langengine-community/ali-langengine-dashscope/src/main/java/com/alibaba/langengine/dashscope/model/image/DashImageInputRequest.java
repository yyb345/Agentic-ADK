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
public class DashImageInputRequest {
    /**
     * 必填
     * 文生图的prompt提示语
     */
    private String prompt;
    /**
     * 非必填
     * 画面中不想出现的内容描述词信息。支持中英文，长度不超过500个字符，超过部分会自动截断。
     * example:
     * 低分辨率、错误、最差质量、低质量、jpeg 伪影、丑陋、重复、病态、残缺、超出框架、多余的手指、变异的手、画得不好的手、画得不好的脸、突变、变形、模糊、脱水、不良的解剖结构、 比例不良、多余肢体、克隆脸、毁容、总体比例、畸形肢体、缺臂、缺腿、多余手臂、多余腿、融合手指、手指过多、长脖子、用户名、水印、签名
     */
    private String negative_prompt;
    /**
     * 非必填
     * 输入参考图像的url; 图片格式可为 jpg, png, tiff, webp等常见位图格式。默认为空。
     * example:cdn上图片都可以
     * 会参照此图的内容or风格生成，有其他参数控制
     */
    private String ref_img;
}
