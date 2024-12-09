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

import java.util.List;
import java.util.Map;

/**
 * 卡片配置属性
 *
 * @author xiaoxuan.lp
 */
@Data
public class CardConfigProperty {

    /**
     * 类型（vertical，normal）
     */
    private String type;

    /**
     * vertical列表取最多行数
     */
    private Integer maxCount;

    /**
     * 列表数据路径
     */
    private Object listDataPath;

    /**
     * 项数据路径
     */
    private Map<String, Object> itemDataPath;

    /**
     * 数据列表
     */
    private List<Map<String, Object>> list;
}