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
package com.alibaba.langengine.agentframework.model.agent.domain;

import com.alibaba.langengine.agentframework.model.domain.KnowledgeRetrievalInput;
import com.alibaba.langengine.agentframework.model.domain.LlmTemplateConfig;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

/**
 * agent关联
 *
 * @author xiaoxuan.lp
 */
@Data
public class AgentRelation {

    /**
     * 知识关联
     */
    private List<KnowledgeRetrievalInput> knowledgeList;

    /**
     * 组件关联
     */
    private List<ComponentCallingInput> componentList;

    /**
     * 角色指令
     */
    private String rolePrompt;

    /**
     * 欢迎词
     */
    private String welcomeMessage;

    /**
     * 推荐问题
     */
    private List<String> recommendQuestions;

    /**
     * 是否启动用户问题建议，默认关闭
     */
    private boolean llmSuggestEnabled = false;

    /**
     * 用户问题建议的自定义prompt
     */
    private String llmSuggestPrompt;
    /**
     * 是否选择创建自定义prompt
     */
    private boolean llmSelfDefineSuggestPrompt;

    /**
     * 执行类型
     */
    private int executeType = 0;

    /**
     * 对应的推理大模型，有值的话以这个配置为准
     */
    private LlmTemplateConfig llmTemplateConfig;

    /**
     * 智能体COT节点对应组件的输出卡片配置
     */
    private JSONObject cotToolsCardConfig;
}