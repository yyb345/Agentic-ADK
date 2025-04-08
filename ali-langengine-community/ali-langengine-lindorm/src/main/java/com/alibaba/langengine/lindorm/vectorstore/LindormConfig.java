package com.alibaba.langengine.lindorm.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LindormConfig {
    public static String LINDORM_ENDPOINT = LindormVectorStoreConfiguration.LINDORM_DATASOURCE_ENDPOINT;
    public static String LINDORM_USERNAME = LindormVectorStoreConfiguration.LINDORM_DATASOURCE_USERNAME;
    public static String LINDORM_PASSWORD = LindormVectorStoreConfiguration.LINDORM_DATASOURCE_PASSWORD;
    public static String LINDORM_INDEX_NAME = LindormVectorStoreConfiguration.LINDORM_DATASOURCE_INDEX_NAME;
    public static String LINDORM_DEFAULT_INDEX_NAME = "knowledge";
    public static int  LINDORM_SEARCH_PORT = 30070;

    static {
        if (StringUtils.isBlank(LINDORM_INDEX_NAME)) {
            LINDORM_INDEX_NAME = LINDORM_DEFAULT_INDEX_NAME;
        }
    }

    @Bean
    public LindormDB lindormDB(@Qualifier(value = "embedding") Embeddings embedding) {
        LindormDB lindormDB = new LindormDB(LindormConfig.LINDORM_INDEX_NAME);
        lindormDB.setEmbedding(embedding);
        return lindormDB;
    }
}
