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

import java.io.Serializable;
import java.util.List;

/**
 * Llm template config
 *
 * @author xiaoxuan.lp
 */
@Data
public class LlmTemplateConfig implements Serializable {

    /**
     * id
     */
    private String modelId;

    /**
     * 模型名称，例如whale的modelName:qingzhai_qwen_1_5_7B
     */
    private String modelName;

    /**
     * 模型版本号，组件专用
     */
    private String modelVersion;

    /**
     * 子模型名称，例如gpt4-proxy的o1-mini和o1-preview
     */
    private String subModelName;

    /**
     * 模型模版，例如WhaleHttpLLM
     */
    private String modelTemplate;

    /**
     * 模型类型，例如qwen
     */
    private String modelType;

    /**
     * 模型apikey
     */
    private String modelApiKey;

    /**
     * 最大token长度
     */
    private Integer maxLength = 2048;

    /**
     * 最大输出token长度
     */
    private Integer maxNewTokens = 100;

    /**
     * topK
     */
    private Integer topK;

    /**
     * topP
     */
    private Double topP;

    /**
     * 温度
     */
    private Double temperature;

    /**
     * 自动打标
     */
    private Boolean autoLlmFlag = false;

    /**
     * 是否增量流式
     */
    private Boolean sseInc;

    /**
     * 是否返回所有response
     */
    private Boolean allResponse = false;

    /**
     * 包含统计信息
     */
    private Boolean containUsage = false;

    /**
     * 是否多模态生成
     */
    private Boolean multiGenerate = false;

    /**
     * 是否包含包含functionCall AI Message
     */
    private Boolean containFunctionCallAIMessage;

    /**
     * marcoVL是否切换千问返回格式
     */
    private Boolean marcoVLSwitchQwenFormat;

    /**
     * 停止词
     */
    private List<String> stop;
}
