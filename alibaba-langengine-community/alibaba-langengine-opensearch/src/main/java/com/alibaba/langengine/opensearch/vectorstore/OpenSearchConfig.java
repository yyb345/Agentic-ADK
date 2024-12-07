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
package com.alibaba.langengine.opensearch.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author cuzz.lb
 * @date 2023/11/10 16:10
 */
@Configuration
public class OpenSearchConfig {
    public static String OPENSEARCH_INSTANCE_ID = OpenSearchVectorstoreConfiguration.OPENSEARCH_DATASOURCE_INSTANCE_ID;
    public static String OPENSEARCH_ENDPOINT = OpenSearchVectorstoreConfiguration.OPENSEARCH_DATASOURCE_ENDPOINT;
    public static String OPENSARCH_SWIFT_SERVER_ROOT = OpenSearchVectorstoreConfiguration.OPENSEARCH_DATASOURCE_SWIFT_SERVER_ROOT;
    public static String OPENSARCH_SWIFT_TOPIC = OpenSearchVectorstoreConfiguration.OPENSEARCH_DATASOURCE_SWIFT_TOPIC;

    public static String OPENSEARCH_DATASOURCE_TABLE_NAME = OpenSearchVectorstoreConfiguration.OPENSEARCH_DATASOURCE_TABLE_NAME;

    public static String OPENSEARCH_DEFAULT_TABLE_NAME = "knowledge";

    static {
        if (StringUtils.isBlank(OPENSEARCH_DATASOURCE_TABLE_NAME)) {
            OPENSEARCH_DATASOURCE_TABLE_NAME = OPENSEARCH_DEFAULT_TABLE_NAME;
        }
    }


    @Bean
    public OpenSearchDB openSearchDB(@Qualifier(value = "embedding") Embeddings embedding) {
        OpenSearchDB openSearchDB = new OpenSearchDB();
        openSearchDB.setEmbedding(embedding);
        return openSearchDB;
    }


}
