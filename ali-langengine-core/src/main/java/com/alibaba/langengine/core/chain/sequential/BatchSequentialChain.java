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
package com.alibaba.langengine.core.chain.sequential;

/**
 * @author yushuo
 * @version BatchSequentialChain.java, v 0.1 2023年12月26日 10:41 yushuo
 */
/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 执行批处理请求
 *
 * @author yushuo
 * @version BatchSequentialChain.java, v 0.1 2023年12月22日 13:04 yushuo
 */
@Data
public class BatchSequentialChain extends SequentialChain {

    private List<SequentialChain> sequentialChains;

    private List<Map<String, Object>> inputsList;

    private List<String> outputKeys;

    /**
     * 异步批量执行
     *
     * @param timeout
     * @param unit
     * @return
     */
    public List<Map<String, Object>> runAsync(long timeout, TimeUnit unit) {
        List<CompletableFuture<Map<String, Object>>> futures = Lists.newArrayList();
        for (int i = 0; i < sequentialChains.size(); i++) {
            SequentialChain sequentialChain = sequentialChains.get(i);
            Map<String, Object> inputs = inputsList.get(i);
            CompletableFuture<Map<String, Object>> future = sequentialChain.runAsync(inputs);
            futures.add(future);
        }
        List<Map<String, Object>> retList = Lists.newArrayList();
        for (CompletableFuture<Map<String, Object>> future : futures) {
            try {
                retList.add(future.get(timeout, unit));
            } catch (Exception e) {
                // to nothing
            }
        }
        return retList;
    }

    /**
     * 如果批量请求的outputKey相同，则可以使用该方法
     * @param key
     * @param timeout
     * @param unit
     * @return
     * @param <T>
     */
    public <T> List<T> runAsync(String key, long timeout, TimeUnit unit) {
        List<Map<String, Object>> mapsList = runAsync(timeout, unit);
        List<T> retList = Lists.newArrayList();
        for (Map<String, Object> stringObjectMap : mapsList) {
            Object o = stringObjectMap.get(key);
            if (Objects.isNull(o)) {
                retList.add(null);
            } else {
                retList.add((T) o);
            }
        }
        return retList;
    }

}