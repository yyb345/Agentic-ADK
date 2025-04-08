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
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A Runnable that can fallback to other Runnables if it fails.
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class RunnableWithFallbacks<Input, Output> extends Runnable<Input, Output> {

    /**
     * The runnable to run first.
     */
    RunnableInterface runnable;

    /**
     * A sequence of fallbacks to try.
     */
    List<RunnableInterface> fallbacks;

    @Override
    public Output invoke(Input input, RunnableConfig config) {
        return invoke(input, config, null);
    }

    private Output invoke(Input input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        List<RunnableInterface> runnables = new ArrayList<>();
        runnables.add(runnable);
        if(!CollectionUtils.isEmpty(fallbacks)) {
            runnables.addAll(fallbacks);
        }

        Throwable firstError = null;
        for (RunnableInterface run : runnables) {
            try {
                if(chunkConsumer != null) {
                    if(config != null && config.isStreamLog()) {
                        return (Output)run.streamLog(input, config, chunkConsumer);
                    }
                    return (Output)run.stream(input, config, chunkConsumer);
                } else {
                    return (Output) run.invoke(input, config);
                }
            } catch (Throwable e) {
                log.error("RunnableWithFallbacks error", e);
                if(firstError == null) {
                    firstError = e;
                }
            }
        }
        if (firstError == null) {
            throw new IllegalStateException("No error stored at end of fallbacks.");
        }
        throw new RuntimeException(firstError);
    }

    @Override
    public Output stream(Input input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        return invoke(input, config, chunkConsumer);
    }
}