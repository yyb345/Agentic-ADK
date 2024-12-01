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
package com.alibaba.agentmagic.framework.domain;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum TemplateTypeEnums {
    llm("llm", "生成大模型节点", "llm", "com.alibaba.agentmagic.core.delegation.LlmCallingDelegation"),
    tool("component", "组件节点", "component", "com.alibaba.agentmagic.core.delegation.ToolCallingDelegation"),
    agent("agent", "应用节点", "agent", "com.alibaba.agentmagic.core.delegation.AgentCallingDelegation"),
    knowledge("knowledge", "知识库节点", "knowledge", "com.alibaba.agentmagic.core.delegation.KnowledgeRetrievalDelegation"),
    script("code", "代码节点", "code", "com.alibaba.agentmagic.core.delegation.DynamicScriptDelegation"),
    cot("cot", "生成大模型节点", "llm", "com.alibaba.agentmagic.core.delegation.CotCallingDelegation"),
    exclusiveGateway("exclusiveGateway", "选择器节点", "exclusiveGateway", "ExclusiveGateway"),
    llm_suggest("llm_suggest", "推荐大模型节点", "llm_suggest", "LlmSuggest"),
    start("start", "开始节点", "start", "StartEvent"),
    end("end", "结束节点", "end", "EndEvent"),
    intention("intention", "意图识别", "intention", "com.alibaba.agentmagic.core.delegation.IntentionDelegation"),

    ;

    private final String code;
    private final String name;
    private final String nameEn;
    private final String activityType;

    TemplateTypeEnums(String code, String name, String nameEn, String activityType) {
        this.code = code;
        this.name = name;
        this.nameEn = nameEn;
        this.activityType = activityType;
    }

    public static TemplateTypeEnums instance(String code) {
        for (TemplateTypeEnums value : TemplateTypeEnums.values()) {
            if(StringUtils.equalsIgnoreCase(value.getCode(), code)) {
                return value;
            }
        }
        return null;
    }

    public static TemplateTypeEnums instanceByActivityType(String activityType) {
        for (TemplateTypeEnums value : TemplateTypeEnums.values()) {
            if(StringUtils.equalsIgnoreCase(value.getActivityType(), activityType)) {
                return value;
            }
        }
        return null;
    }

}
