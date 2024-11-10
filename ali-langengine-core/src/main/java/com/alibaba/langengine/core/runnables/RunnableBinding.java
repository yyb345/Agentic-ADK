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

import java.util.Map;
import java.util.function.Consumer;

import static com.alibaba.langengine.core.config.LangEngineConfiguration.NULL_AWARE_BEAN_UTILS_BEAN;

/**
 * Wrap a runnable with additional functionality.
 * 反向绑定
 *
 * @param <Input>
 * @param <Output>
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class RunnableBinding<Input, Output> extends Runnable<Input, Output> {

    private Runnable<Input, Output> bound;

    private Map<String, Object> extraAttributes;

    private RunnableConfig currentConfig = new RunnableConfig();

    @Override
    public Output invoke(Input input, RunnableConfig config) {
        return invoke(input, config, null);
    }

    @Override
    public Output stream(Input input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        return invoke(input, config, chunkConsumer);
    }

    private Output invoke(Input input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        if (extraAttributes != null && extraAttributes.size() > 0) {
            if (config == null) {
                config = new RunnableConfig();
            }
            config.setMetadata(extraAttributes);
        }
        if(config != null) {
            try {
                NULL_AWARE_BEAN_UTILS_BEAN.copyProperties(currentConfig, config);
            } catch (Throwable e) {
                log.error((chunkConsumer != null ? "stream" : "invoke") + " error", e);
                throw new RuntimeException(e);
            }
        }

        if(chunkConsumer != null) {
            if(config != null && config.isStreamLog()) {
                return bound.streamLog(input, config, chunkConsumer);
            }
            return bound.stream(input, config, chunkConsumer);
        } else {
            return bound.invoke(input, config);
        }
    }
}