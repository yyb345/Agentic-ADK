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

import com.alibaba.langengine.agentframework.model.agent.domain.AgentRelation;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 框架级系统上下文
 *
 * 注意事项，类中存在循环引用，请不要序列化对象，会造成异常
 * @author xiaoxuan.lp
 */
@Data
public class FrameworkSystemContext {

    /**
     * 开场白开关
     */
    private Boolean isInit;

    /**
     * 是否异步化
     */
    private Boolean async;

    /**
     * 会话内容
     */
    private String query;

    /**
     * agentCode
     */
    private String agentCode;

    /**
     * 会话id
     */
    private String sessionId;

    /**
     * 请求id
     */
    private String requestId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 环境标
     */
    private String env;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * AgentRelation
     */
    private AgentRelation agentRelation;

    private Boolean apikeyCall;

    private String apikey;

    private List<ChatAttachment> chatAttachments;

    private Consumer<Object> chunkConsumer;

    private List<ChatMessage> history;

    private Map<String, Object> invokeContext;

    //****** ------ 附带属性 ------ ******//

    /**
     * 附带的工具，不作为系统变量
     */
    private List<BaseTool> allTools;

    private Boolean forceStream;

    private Object shortcutContent;

    private ExecutionContext executionContext;

    private Boolean offline;

    private Boolean batch;
}
