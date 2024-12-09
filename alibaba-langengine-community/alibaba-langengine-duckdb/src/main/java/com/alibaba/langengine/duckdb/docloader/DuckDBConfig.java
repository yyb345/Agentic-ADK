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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * duckdb bean配置
 *
 * @author xiaoxuan.lp
 */
@Configuration
public class DuckDBConfig {

    private static final String DATA_SOURCE_NAME = "duckdbDataSource";

    @Bean
    public JdbcTemplate jdbcTemplate(@Qualifier(DATA_SOURCE_NAME) DataSource dataSource) {
        JdbcTemplate duckdb =new JdbcTemplate();
        duckdb.setDataSource(dataSource);
        return duckdb;
    }

    @Bean(name = DATA_SOURCE_NAME)
    public DataSource getDataSource() {
        String url = "jdbc:duckdb:";

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.duckdb.DuckDBDriver");
        config.setMaximumPoolSize(10);
        config.setMaxLifetime(3);
        config.setJdbcUrl(url);
        HikariDataSource dataSource = new HikariDataSource(config);
        return dataSource;
    }

    @Bean
    public DuckDBLoader duckDBLoader(JdbcTemplate jdbcTemplate) {
        DuckDBLoader duckDBLoader = new DuckDBLoader();
        duckDBLoader.setJdbcTemplate(jdbcTemplate);
        return duckDBLoader;
    }
}
