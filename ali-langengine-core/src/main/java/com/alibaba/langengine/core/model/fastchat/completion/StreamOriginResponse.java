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
package com.alibaba.langengine.core.model.fastchat.completion;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Agent响应
 *
 * @author xiaoxuan.lp
 */
@Data
public class StreamOriginResponse {

    private Map<String, Object> structData;

    /**
     * Agent推理结果
     */
    private String answer;

    /**
     * 引用结果
     */
    private List<Map<String, Object>> reference;

    /**
     * debug返回信息
     */
    private String debugInfo;

    /**
     * 推理中间过程数据
     */
    private List<Map<String, Object>> steps;
}
