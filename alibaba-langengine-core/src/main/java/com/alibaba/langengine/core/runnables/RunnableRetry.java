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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * Retry a Runnable if it fails.
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class RunnableRetry extends RunnableBinding {

    private int maxAttemptNumber = 3;
    private long intervalTime = 10;

    @Override
    public Object invoke(Object input, RunnableConfig config) {
        return invoke(input, config, 0, null);
    }

    @Override
    public Object stream(Object o, RunnableConfig config, Consumer chunkConsumer) {
        return invoke(o, config, 0, chunkConsumer);
    }

    private Object invoke(Object input, RunnableConfig config, int count, Consumer chunkConsumer) {
        try {
            if(chunkConsumer != null) {
                if(config != null && config.isStreamLog()) {
                    return getBound().streamLog(input, config, chunkConsumer);
                }
                return getBound().stream(input, config, chunkConsumer);
            } else {
                return getBound().invoke(input, config);
            }
        } catch (Throwable e) {
            log.error(String.format("[%d] RunnableRetry invoke error", count), e);
            if(count >= maxAttemptNumber) {
                throw new RuntimeException("last time RunnableRetry invoke error", e);
            }
            // 停顿时间
            try {
                Thread.sleep(intervalTime);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            return invoke(input ,config, ++count, chunkConsumer);
        }
    }
}