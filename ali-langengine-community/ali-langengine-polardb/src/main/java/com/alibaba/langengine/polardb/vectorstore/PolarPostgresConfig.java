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
package com.alibaba.langengine.polardb.vectorstore;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.polardb.vectorstore.mapper.PolarPostgresKnowledgeMapper;
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
 * polardb bean配置
 *
 * @author xiaoxuan.lp
 */
@Configuration
@MapperScan(basePackages = "com.alibaba.langengine.polardb.vectorstore.mapper", sqlSessionFactoryRef = PolarPostgresConfig.SQL_SESSION_FACTORY_NAME)
public class PolarPostgresConfig {

    private static final String ENDPOINT = PolarVectorstoreConfiguration.POLARDB_DATASOURCE_ENDPOINT;

    private static final String DBNAME = PolarVectorstoreConfiguration.POLARDB_DATASOURCE_DATABASENAME;

    private static final String DATABASE_U = PolarVectorstoreConfiguration.POLARDB_DATASOURCE_U;

    private static final String DATABASE_P = PolarVectorstoreConfiguration.POLARDB_DATASOURCE_P;

    private static final String DATA_SOURCE_NAME = "polardbDataSource";

    public static final String SQL_SESSION_FACTORY_NAME = "polardbSqlSessionFactory";

    private static final String TRANSACTION_MANAGER = "polardbTransactionManager";

    @Bean(name = DATA_SOURCE_NAME)
    public DataSource getDataSource() throws SQLException {
        String url = "jdbc:postgresql://" + ENDPOINT + "/" + DBNAME + "?preferQueryMode=simple&tcpKeepAlive=true";

        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(url);

        druidDataSource.setUsername(DATABASE_U);
        druidDataSource.setPassword(DATABASE_P);

        // 初始化连接数
        druidDataSource.setInitialSize(5);
        // 最大连接数
        druidDataSource.setMaxActive(20);

        // 连接等待超时时间
        druidDataSource.setMaxWait(12000);

        // 配置间隔多久进行一次检测，检测需要关闭的空闲连接
        druidDataSource.setTimeBetweenEvictionRunsMillis(3000);
        druidDataSource.setValidationQuery("SELECT 1");
        druidDataSource.setFilters("stat");
        druidDataSource.setTestWhileIdle(true);
        // 配置从连接池获取连接时，是否检查连接有效性，true每次都检查；false不检查
        druidDataSource.setTestOnBorrow(false);
        // 配置向连接池归还连接时，是否检查连接有效性，true每次都检查；false不检查
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
    public PolarPostgresDB polarPostgresDB(PolarPostgresKnowledgeMapper polarPostgresKnowledgeMapper,
                                           @Qualifier(value = "embedding") Embeddings embedding) {
        PolarPostgresDB polarPostgresDB = new PolarPostgresDB();
        polarPostgresDB.setEmbedding(embedding);
        polarPostgresDB.setPolarPostgresKnowledgeMapper(polarPostgresKnowledgeMapper);
        return polarPostgresDB;
    }
}