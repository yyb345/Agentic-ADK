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
package com.alibaba.langengine.agentframework.model.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Agent请求API响应体
 *
 * @author xiaoxuan.lp
 */
@Data
public class AgentAPIInvokeResponse {

    /**
     * 对话式专用（new）
     */
    private List<ChatMessage> message = new ArrayList<>();

    /**
     * 生成式专用（new）
     */
    private Map<String, Object> structData;


    /** ------ 以下均为兼容字段 ------ **/

    /**
     * agent推理结果，非结构化
     * 其中，如果固定是message变量，直接返回message变量结果
     */
    private String answer;

    /**
     * 引用结果，文档引用使用
     */
    private List<Map<String, Object>> reference;

    /**
     * debug返回信息
     */
    private String debugInfo;
}
