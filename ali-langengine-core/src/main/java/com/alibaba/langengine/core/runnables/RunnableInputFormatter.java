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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RunnableInputFormatter extends Runnable<RunnableHashMap, RunnableHashMap> {

    private Map<String, Function<Object, String>> inputFormatters;

    public RunnableInputFormatter(Map<String, Function<Object, String>> inputFormatters) {
        this(null, inputFormatters);
    }

    public RunnableInputFormatter(String name, Map<String, Function<Object, String>> inputFormatters) {
        setName(name);
        this.inputFormatters = inputFormatters;
    }

    public RunnableInputFormatter() {
        this(null, new HashMap<>());
    }

    public RunnableInputFormatter add(String key, Function<Object, String> formatter) {
        inputFormatters.put(key, formatter);
        return this;
    }

    @Override
    public RunnableHashMap invoke(RunnableHashMap input, RunnableConfig config) {
        return invoke(input, config, null);
    }

    @Override
    public RunnableHashMap stream(RunnableHashMap input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        return invoke(input, config, chunkConsumer);
    }

    private RunnableHashMap invoke(RunnableHashMap input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        try {
            RunnableHashMap output = new RunnableHashMap();
            output.putAll(input);
            for (Map.Entry<String, Function<Object, String>> entry : inputFormatters.entrySet()) {
                if (input.get(entry.getKey()) == null) {
                    continue;
                }
                output.put(entry.getKey(), entry.getValue().apply(input.get(entry.getKey())));
            }
            return output;
        } catch (Throwable e) {
            log.error("RunnableInputFormatter invoke error", e);
            throw new RuntimeException(e.toString());
        }
    }

    public Map<String, Function<Object, String>> getInputFormatters() {
        return inputFormatters;
    }
}
