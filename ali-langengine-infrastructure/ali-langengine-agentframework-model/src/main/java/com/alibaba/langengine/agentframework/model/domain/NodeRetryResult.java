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
package com.alibaba.langengine.agentframework.model.domain;

import lombok.Data;

@Data
public class NodeRetryResult {

    /**
     * 是否需要重试
     */
    Boolean needRetry;

    /**
     * 当前重试次数
     */
    Integer retryCount;

    /**
     * 最大重试次数
     */
    Integer maxRetryCount;

    /**
     * 当前重试间隔,毫秒
     */
    Long retryInterval;


    public static NodeRetryResult returnNeedRetry(Integer retryCount,Integer maxRetryCount) {
        NodeRetryResult retryResult = new NodeRetryResult();
        retryResult.setNeedRetry(true);
        retryResult.setRetryCount(retryCount);
        retryResult.setMaxRetryCount(maxRetryCount);
        retryResult.setRetryInterval(0L);
        return retryResult;
    }

    public static NodeRetryResult returnNoNeedRetry(Integer retryCount,Integer maxRetryCount) {
        NodeRetryResult retryResult = new NodeRetryResult();
        retryResult.setNeedRetry(false);
        retryResult.setRetryCount(retryCount);
        retryResult.setMaxRetryCount(maxRetryCount);
        retryResult.setRetryInterval(0L);
        return retryResult;
    }

    public static NodeRetryResult returnNoNeedRetry() {
        NodeRetryResult retryResult = new NodeRetryResult();
        retryResult.setNeedRetry(false);
        retryResult.setRetryCount(0);
        retryResult.setMaxRetryCount(0);
        retryResult.setRetryInterval(0L);
        return retryResult;
    }

    public static NodeRetryResult returnNeedRetry(Integer retryCount,Integer maxRetryCount,Long retryInterval) {
        NodeRetryResult retryResult = new NodeRetryResult();
        retryResult.setNeedRetry(true);
        retryResult.setRetryCount(retryCount);
        retryResult.setMaxRetryCount(maxRetryCount);
        retryResult.setRetryInterval(retryInterval);
        return retryResult;
    }
}
