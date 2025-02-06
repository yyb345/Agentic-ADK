package com.alibaba.langengine.lindorm.vectorstore;

public class LindormConstants {
    public static String LINDORM_VECTOR_METHOD_IVFPQ = "ivfpq";
    public static String LINDORM_VECTOR_METHOD_HNSW = "hnsw";
    public static String LINDORM_VECTOR_METHOD_SPARSE_HNSW = "sparse_hnsw";

    public static String LINDORM_DEFAULT_ENGINE_TYPE = "lvector";
    public static String LINDORM_DEFAULT_VECTOR_FIELD_NAME = "vector";
    public static String LINDORM_DEFAULT_VECTOR_TYPE = "knn_vector";

    public static String LINDORM_DEFAULT_METHOD_NAME = LINDORM_VECTOR_METHOD_HNSW;
    public static String LINDORM_DEFAULT_DATA_TYPE = "float";
    public static String LINDORM_DEFAULT_SPACE_TYPE = "l2";

    public static Integer LINDORM_DEFAULT_M = 24;
    public static Integer LINDORM_EF_CONSTRUCTION = 200;
    public static Integer LINDORM_DEFAULT_NUM_OF_SHARDS = 2;

    public static Integer LINDORM_DEFAULT_NLIST = 100;
    public static Integer LINDORM_DEFAULT_CENTROIDS_HNSW_M = 16;
    public static Integer LINDORM_DEFAULT_CENTROIDS_HNSW_EF_CONSTRUCT = 100;
    public static Integer LINDORM_DEFAULT_CENTROIDS_HNSW_EF_SEARCH = 100;


    // search params
    public static String LINDORM_HYBRID_SEARCH_PRE_FILTER = "pre_filter";
    public static String LINDORM_HYBRID_SEARCH_POST_FILTER = "post_filter";
    public static String LINDORM_HYBRID_SEARCH_EFFICIENT_FILTER = "efficient_filter";

    public static Integer LINDORM_DEFAULT_EF_SEARCH = 100;
    public static Integer LINDORM_DEFAULT_REORDER_FACTOR = 2;

    public static Integer LINDORM_IVFPQ_BUILD_INDEX_MIN_DOC_COUNT = 300;
}
