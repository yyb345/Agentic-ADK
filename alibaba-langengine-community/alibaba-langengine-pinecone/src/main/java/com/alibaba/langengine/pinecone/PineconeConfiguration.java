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
package com.alibaba.langengine.pinecone;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * configuration
 *
 * @author xiaoxuan.lp
 */
public class PineconeConfiguration {

    /**
     * pinecone api key
     */
    public static String PINECONE_API_KEY = WorkPropertiesUtils.get("pinecone_api_key");

    /**
     * pinecone environment
     */
    public static String PINECONE_ENVIRONMENT = WorkPropertiesUtils.get("pinecone_environment");

    /**
     * pinecone project name
     */
    public static String PINECONE_PROJECT_NAME = WorkPropertiesUtils.get("pinecone_project_name");
}
