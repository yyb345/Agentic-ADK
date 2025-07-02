package com.alibaba.langengine.docloader.yuque.service;

import lombok.Data;

/**
 * 语雀响应结果
 *
 * @author xiaoxuan.lp
 */
@Data
public class YuqueResult<T> {

    private T data;


    private YuqueMeta meta;

    @Data
    public static class YuqueMeta {

        /**
         * 总共多少条文档
         */
        private Integer total;
    }
}
