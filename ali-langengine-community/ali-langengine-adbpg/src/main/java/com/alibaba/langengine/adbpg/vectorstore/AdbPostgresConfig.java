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
package com.alibaba.langengine.adbpg.vectorstore;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.langengine.adbpg.vectorstore.mapper.AdbPostgresKnowledgeMapper;
import com.alibaba.langengine.core.embeddings.Embeddings;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * adbpostgres bean配置
 *
 * @author xiaoxuan.lp
 */
@Configuration
@MapperScan(basePackages = "com.alibaba.langengine.adbpg.vectorstore.mapper", sqlSessionFactoryRef = AdbPostgresConfig.SQL_SESSION_FACTORY_NAME)
public class AdbPostgresConfig {

    private static final String ENDPOINT = AdbVectorstoreConfiguration.ADBPG_DATASOURCE_ENDPOINT;

    private static final String DBNAME = AdbVectorstoreConfiguration.ADBPG_DATASOURCE_DATABASENAME;

    private static final String DATABASE_U = AdbVectorstoreConfiguration.ADBPG_DATASOURCE_U;

    private static final String DATABASE_P = AdbVectorstoreConfiguration.ADBPG_DATASOURCE_P;

    private static final String DATA_SOURCE_NAME = "adbpgDataSource";

    public static final String SQL_SESSION_FACTORY_NAME = "adbpgSqlSessionFactory";

    private static final String TRANSACTION_MANAGER = "adbpgTransactionManager";

    @Bean(name = DATA_SOURCE_NAME)
    public DataSource getDataSource() throws SQLException {
        String url = "jdbc:postgresql://" + ENDPOINT + "/" + DBNAME + "?preferQueryMode=simple&tcpKeepAlive=true";

        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(url);

        druidDataSource.setUsername(DATABASE_U);
        druidDataSource.setPassword(DATABASE_P);

        druidDataSource.setInitialSize(5);
        druidDataSource.setMaxActive(20);
        druidDataSource.setMaxWait(12000);

        druidDataSource.setTimeBetweenEvictionRunsMillis(3000);
        druidDataSource.setValidationQuery("SELECT 1");
        druidDataSource.setFilters("stat");
        druidDataSource.setTestWhileIdle(true);
        druidDataSource.setTestOnBorrow(false);
        druidDataSource.setTestOnReturn(false);

        return druidDataSource;
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
    public AdbPostgresDB adbPostgresDB(AdbPostgresKnowledgeMapper adbPostgresKnowledgeMapper,
                                       @Qualifier(value = "embedding") Embeddings embedding) {
        AdbPostgresDB adbPostgresDB = new AdbPostgresDB();
        adbPostgresDB.setEmbedding(embedding);
        adbPostgresDB.setAdbPostgresKnowledgeMapper(adbPostgresKnowledgeMapper);
        return adbPostgresDB;
    }
}