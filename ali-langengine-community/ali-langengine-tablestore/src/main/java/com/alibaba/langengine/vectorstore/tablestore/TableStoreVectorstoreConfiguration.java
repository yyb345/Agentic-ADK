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

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

public class TableStoreVectorstoreConfiguration {
    /**
     * TableStore 实例名
     */
    public static String TABLESTORE_INSTANCE_NAME = WorkPropertiesUtils.get("tablestore_instance_name");
    /**
     * TableStore endpoint
     */
    public static String TABLESTORE_ENDPOINT = WorkPropertiesUtils.get("tablestore_endpoint");
    /**
     * TableStore Access Key ID
     */
    public static String TABLESTORE_ACCESS_SECRET_ID = WorkPropertiesUtils.get("tablestore_access_secret_id");
    /**
     * TableStore Access Key Secret
     */
    public static String TABLESTORE_ACCESS_SECRET_KEY = WorkPropertiesUtils.get("tablestore_access_secret_key");
    /**
     * TableStore 表名
     */
    public static String TABLESTORE_TABLE_NAME = WorkPropertiesUtils.get("tablestore_table_name");
    /**
     * TableStore 索引名
     */
    public static String TABLESTORE_INDEX_NAME = WorkPropertiesUtils.get("tablestore_index_name");
}
