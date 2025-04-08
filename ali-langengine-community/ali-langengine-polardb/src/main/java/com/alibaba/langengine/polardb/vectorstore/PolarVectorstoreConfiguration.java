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

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * vectorstore configuration
 *
 * @author xiaoxuan.lp
 */
public class PolarVectorstoreConfiguration {

    /**
     * polardb datasource endpoint
     */
    public static String POLARDB_DATASOURCE_ENDPOINT = WorkPropertiesUtils.get("polardb_datasource_endpoint");

    /**
     * polardb datasource databasename
     */
    public static String POLARDB_DATASOURCE_DATABASENAME = WorkPropertiesUtils.get("polardb_datasource_databasename");

    /**
     * polardb datasource u
     */
    public static String POLARDB_DATASOURCE_U = WorkPropertiesUtils.get("polardb_datasource_u");

    /**
     * polardb datasource p
     */
    public static String POLARDB_DATASOURCE_P = WorkPropertiesUtils.get("polardb_datasource_p");
}
