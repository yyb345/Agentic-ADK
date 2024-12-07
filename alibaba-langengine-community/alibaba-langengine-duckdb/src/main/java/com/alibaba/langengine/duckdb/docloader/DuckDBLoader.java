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
package com.alibaba.langengine.duckdb.docloader;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.docloader.BaseLoader;
import com.alibaba.langengine.core.indexes.Document;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Loads a query result from DuckDB into a list of documents.
 * https://duckdb.org/
 * https://github.com/duckdb
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class DuckDBLoader extends BaseLoader {

    private JdbcTemplate jdbcTemplate;

    /**
     * sql脚本
     */
    private String sql;

    @Override
    public List<Document> load() {
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

        List<Document> documents = list.stream().map(map -> {
            Document document = new Document();
            document.setPageContent(JSON.toJSONString(map));
            return document;
        }).collect(Collectors.toList());
        return documents;
    }
}
