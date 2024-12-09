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
package com.alibaba.langengine.sqlite.memory.cache;

import com.alibaba.langengine.sqlite.memory.cache.mapper.SqliteChatInfoMapper;
import com.alibaba.langengine.sqlite.memory.cache.mapper.SqliteMessageInfoMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;

/**
 * sqlite bean配置
 *
 * @author xiaoxuan.lp
 */
@Configuration
@MapperScan(basePackages = "com.alibaba.langengine.sqlite.memory.cache.mapper", sqlSessionFactoryRef = SqliteConfig.SQL_SESSION_FACTORY_NAME)
public class SqliteConfig {

    private static final String DATA_SOURCE_NAME = "sqliteDataSource";

    public static final String SQL_SESSION_FACTORY_NAME = "sqliteSqlSessionFactory";

    private static final String TRANSACTION_MANAGER = "sqliteTransactionManager";

    @Bean(name = DATA_SOURCE_NAME)
    public DataSource getDataSource() {
        String filePath = SqliteConfig.class.getClassLoader().getResource("sqlite/memory.db").getPath();

        String url = "jdbc:sqlite:" + filePath;

        SQLiteDataSource sqLiteDataSource = new SQLiteDataSource();
        sqLiteDataSource.setUrl(url);

        return sqLiteDataSource;
    }

    @Bean(name = TRANSACTION_MANAGER)
    public DataSourceTransactionManager hologresDataSourceTransactionManager(@Qualifier(DATA_SOURCE_NAME) DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = SQL_SESSION_FACTORY_NAME)
    public SqlSessionFactory hologresSqlSessionFactory(@Qualifier(DATA_SOURCE_NAME) DataSource dataSource)
            throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public SqliteCache sqliteCache(SqliteChatInfoMapper sqliteChatInfoMapper,
                                   SqliteMessageInfoMapper sqliteMessageInfoMapper) {
        SqliteCache sqliteCache = new SqliteCache();
        sqliteCache.setSqliteChatInfoMapper(sqliteChatInfoMapper);
        sqliteCache.setSqliteMessageInfoMapper(sqliteMessageInfoMapper);
        return sqliteCache;
    }
}
