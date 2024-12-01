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

/**
 * 卡片配置
 *
 * @author xiaoxuan.lp
 */
@Data
public class CardConfig {

    /**
     * 卡片npm名称
     */
    private String npmName;

    /**
     * 是否使用最新版本
     */
    private Boolean useLatestVersion;

    /**
     * 版本号
     */
    private String version;

    /**
     * 卡片属性
     */
    private CardConfigProperty props;
}