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
package com.alibaba.langengine.agentframework.model.agent.buildin;

import com.alibaba.langengine.agentframework.model.agent.AgentModel;
import lombok.Data;

import java.util.List;

/**
 * BuiltIn agent model
 *
 * @author xiaoxuan.lp
 */
@Data
public class BuiltInAgentModel extends AgentModel {

    /**
     * id
     */
    private Long id;

    /**
     * 大模型id
     */
    private Long modelId;

    /**
     * 大模型配置
     */
    private String modelConfig;

    /**
     * 对应的大模型
     */
    private BuiltInLlmTemplate llmTemplate;

    /**
     * agent角色描述
     */
    private String prompt;

    /**
     * 关联的文档chunk信息
     */
    private List<BuiltInFileChunk> builtInFileChunks;

    /**
     * agent配置信息,jsonString
     */
    private String agentConfig;
}
