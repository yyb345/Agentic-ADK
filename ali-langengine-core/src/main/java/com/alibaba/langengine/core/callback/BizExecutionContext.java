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
package com.alibaba.langengine.core.callback;

import java.util.Map;

import lombok.Data;

/**
 * @author aihe.ah
 * @time 2023/11/22
 * 功能说明：
 * 给业务用的上下文，多加一个userid
 */
@Data
public class BizExecutionContext extends ExecutionContext {

    /**
     * 上下文中的用户id
     */
    private String userId;

    /**
     * 业务的关键Id，方便排查问题、比如模板Id，助手Id
     */
    private String bizId;

    /**
     * 标记业务属于什么类型
     */
    private String bizType;

    /**
     * 业务上下文，业务可能想要处理的过程中处理一下自己的上下文数据
     */
    private Map<String, Object> bizContext;

    /**
     * 业务的埋点数据，放在这里面的内容都会打印出来
     */
    private Map<String, Object> utBizParams;

    /**
     * 当前的业务位于那一次的会话
     * 主要由于LLM作为ChatBox的场景比较多，大部分的生成是发生在某一次会话中；
     * 通过会话Id，把生成的结果和会话关联起来
     */
    private String sessionId;
}
