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

import lombok.Data;

import java.util.Map;
import java.util.function.Consumer;

@Data
public class ToolCallingInvokeRequest {

    private String query;

    private String toolId;

    private String toolScene;

    private String toolVersion;

    private String toolParams;

    // 离线跑批任务标记，默认false
    @Deprecated
    private Boolean offlineBatch = false;

//    private Boolean offline;
//
//    private Boolean batch;


    // 替换读超时时间字段
    private String replaceReadTimeout;

    private String replaceDomain;

    Consumer<Object> chunkConsumer;

    // 是否apikey call
    private boolean apiKeyCall = false;

    // 应用是否走节点异步化metaq
    private boolean async = false;

    //节点的异步操作
    private boolean nodeAsync = false;

    //节点异步调用超时时间，单位为秒
    private Integer timeoutSecond;

    private String apiKey;

    private String env;

    private String projectName;

    private String requestId;

    private String agentCode;

    private String cardConfig;

    private Map<String, Object> invokeContext;

    private boolean needNewTraceId = false;

    private String processInstanceId;

}