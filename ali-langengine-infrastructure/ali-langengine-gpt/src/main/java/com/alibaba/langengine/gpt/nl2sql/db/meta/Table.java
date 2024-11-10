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
package com.alibaba.langengine.gpt.nl2sql.db.meta;

import lombok.Data;

import java.util.List;

/**
 * 数据表
 *
 * @author xiaoxuan.lp
 */
@Data
public class Table {

    /**
     * 表名称
     */
    private String name;

    /**
     * 列集合
     */
    private List<Column> columns;

    /**
     * 主键
     */
    private PrimaryKey primaryKey;

    /**
     * 外键集合
     */
    private List<ForeignKey> foreignKeys;
}
