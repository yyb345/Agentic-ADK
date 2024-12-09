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
package com.alibaba.langengine.agentframework.model.service.request;

import java.util.Map;

import lombok.Data;

/**
 * 动态脚本调用请求
 */
@Data
public class DynamicScriptInvokeRequest {

    /**
     * 是否是用户的debug链路
     */
    private Boolean isDebug;

    /**
     * 标识具体是那个Agent
     */
    private String agentCode;

    /**
     * 代码节点的类型
     */
    private String codeType;

    /**
     * 代码的内容
     */
    private String codeContents;

    /**
     * 用户的输入参数
     */
    private Map<String, Object> inputCodeParams;

    /**
     * encode代码的类型，默认为Base64
     */
    private String encodeType;

    /**
     * encode编码的具体内容
     */
    private String encodeCodeContent;
}
