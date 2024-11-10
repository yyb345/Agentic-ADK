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

import com.alibaba.langengine.core.util.BaseRequest;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @author chenshuaixin
 * @date 2024/05/17
 */
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DashImageRequest extends BaseRequest {

    /**
     * 指定需要的调用的模型
     */
    private String model;
    /**
     * 通义万象的input
     */
    private DashImageInputRequest input;
    /**
     * 通义万相的 parameters
     */
    private DashImageParametersRequest parameters;
}
