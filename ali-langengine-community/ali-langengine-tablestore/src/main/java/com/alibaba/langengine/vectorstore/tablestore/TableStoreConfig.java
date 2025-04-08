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
package com.alibaba.langengine.vectorstore.tablestore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TableStoreConfig {
    public static String TABLESTORE_INSTANCE_NAME = TableStoreVectorstoreConfiguration.TABLESTORE_INSTANCE_NAME;
    public static String TABLESTORE_ENDPOINT = TableStoreVectorstoreConfiguration.TABLESTORE_ENDPOINT;
    public static String TABLESTORE_ACCESS_SECRET_ID = TableStoreVectorstoreConfiguration.TABLESTORE_ACCESS_SECRET_ID;
    public static String TABLESTORE_ACCESS_SECRET_KEY = TableStoreVectorstoreConfiguration.TABLESTORE_ACCESS_SECRET_KEY;

    public static String TABLESTORE_TABLE_NAME = TableStoreVectorstoreConfiguration.TABLESTORE_TABLE_NAME;
    public static String TABLESTORE_INDEX_NAME = TableStoreVectorstoreConfiguration.TABLESTORE_INDEX_NAME;

    public static final String TABLESTORE_DEFAULT_TABLE_NAME = "langengine_ots_v1";
    public static final String TABLESTORE_DEFAULT_INDEX_NAME = "langengine_ots_index_v1";

    static {
        if (StringUtils.isBlank(TABLESTORE_TABLE_NAME)) {
            TABLESTORE_TABLE_NAME = TABLESTORE_DEFAULT_TABLE_NAME;
        }
        if (StringUtils.isBlank(TABLESTORE_INDEX_NAME)) {
            TABLESTORE_INDEX_NAME = TABLESTORE_DEFAULT_INDEX_NAME;
        }
    }

    @Bean
    public TableStoreDB tableStoreDB(@Qualifier(value = "embedding") Embeddings embedding) {
        TableStoreDB tableStoreDB = new TableStoreDB(embedding);
        return tableStoreDB;
    }
}
