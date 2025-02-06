package com.alibaba.langengine.lindorm.vectorstore;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

public class LindormVectorStoreConfiguration {

    /**
     * lindorm 实例endpoint
     */
    public static String LINDORM_DATASOURCE_ENDPOINT = WorkPropertiesUtils.get("lindorm_datasource_endpoint");

    /**
     * lindorm 实例username
     */
    public static String LINDORM_DATASOURCE_USERNAME = WorkPropertiesUtils.get("lindorm_datasource_username");

    /**
     * lindorm 实例password
     */
    public static String LINDORM_DATASOURCE_PASSWORD = WorkPropertiesUtils.get("lindorm_datasource_password");

    /**
     * * lindorm 索引名
     */
    public static String LINDORM_DATASOURCE_INDEX_NAME = WorkPropertiesUtils.get("lindorm_datasource_index_name");
}
