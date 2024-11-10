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

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

public class OpenSearchVectorstoreConfiguration {

    /**
     * opensearch 弹内版本，实例id
     */
    public static String OPENSEARCH_DATASOURCE_INSTANCE_ID = WorkPropertiesUtils.get("opensearch_datasource_instance_id");
    /**
     * opensearch 弹内版本，endpoint
     */
    public static String OPENSEARCH_DATASOURCE_ENDPOINT = WorkPropertiesUtils.get("opensearch_datasource_endpoint");

    public static String OPENSEARCH_DATASOURCE_TABLE_NAME = WorkPropertiesUtils.get("opensearch_datasource_table_name");
    /**
     * opensearch 弹内版本，swift zk
     *
     */
    public static String OPENSEARCH_DATASOURCE_SWIFT_SERVER_ROOT = WorkPropertiesUtils.get("opensearch_datasource_swift_server_root");
    /**
     * opensearch 弹内版本，swift topic
     *
     */
    public static String OPENSEARCH_DATASOURCE_SWIFT_TOPIC = WorkPropertiesUtils.get("opensearch_datasource_swift_topic");
}
