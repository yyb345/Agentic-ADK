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
package com.alibaba.langengine.core.runnables;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Runnable that runs a mapping of Runnables in parallel, and returns a mapping of their outputs.
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class RunnableParallel extends Runnable<Object, RunnableHashMap> {

    private List<RunnableInterface> steps;


    public RunnableParallel(String name, RunnableInterface... steps) {
        setName(name);
        setSteps(Arrays.asList(steps));
    }

    public RunnableParallel(RunnableInterface... steps) {
        setSteps(Arrays.asList(steps));
    }


    @Override
    public RunnableHashMap invoke(Object input, RunnableConfig config) {
        return invoke(input, config, null);
    }

    private RunnableHashMap invoke(Object input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        try {
            RunnableHashMap runnableHashMap = new RunnableHashMap();

            List<CompletableFuture<RunnableOutput>> futures = Lists.newArrayList();
            for (RunnableInterface step : getSteps()) {
                CompletableFuture<RunnableOutput> future;
                if(chunkConsumer != null) {
                    if(config != null && config.isStreamLog()) {
                        future = step.streamLogAsync(input, config, chunkConsumer);
                    } else {
                        future = step.streamAsync(input, config, chunkConsumer);
                    }
                } else {
                    future = step.invokeAsync(input, config);
                }
                futures.add(future);
            }

            // 使用 allOf 等待所有的 CompletableFuture 完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            // 阻塞并等待所有 future 完成
            if(config != null) {
                allFutures.get(config.getParallelSecondTimeout(), TimeUnit.SECONDS);
            } else {
                allFutures.get(300, TimeUnit.SECONDS);
            }

            int counter = 0;
            // 之后获取每个 CompletableFuture 的结果
            for (CompletableFuture<RunnableOutput> future : futures) {
                RunnableOutput response  = future.get();
                if(response instanceof RunnableHashMap) {
                    runnableHashMap.putAll((RunnableHashMap)response);
                } else {
                    runnableHashMap.put(steps.get(counter++).getName(), response);
                }
            }
            return runnableHashMap;
        } catch (Throwable e) {
            log.error("RunnableParallel invoke error", e);
            throw new RuntimeException(e.toString());
        }
    }

    @Override
    public RunnableHashMap stream(Object input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        return invoke(input, config, chunkConsumer);
    }

    @Override
    public RunnableHashMap streamLog(Object input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        if(config == null) {
            config = new RunnableConfig();
        }
        config.setStreamLog(true);
        return invoke(input, config, chunkConsumer);
    }
}
