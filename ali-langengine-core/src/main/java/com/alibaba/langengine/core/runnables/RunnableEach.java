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

import java.util.List;
import java.util.function.Consumer;

/**
 * A runnable that delegates calls to another runnable with each element of the input sequence.
 *
 * @author xiaoxuan.lp
 */
@Data
public class RunnableEach extends Runnable<List<RunnableHashMap>, List<RunnableOutput>> {

    private RunnableInterface bound;

    @Override
    public List<RunnableOutput> invoke(List<RunnableHashMap> runnableInputs, RunnableConfig config) {
        return invoke(runnableInputs, config, null);
    }

    @Override
    public List<RunnableOutput> stream(List<RunnableHashMap> runnableInputs, RunnableConfig config, Consumer<Object> chunkConsumer) {
        return invoke(runnableInputs, config, chunkConsumer);
    }

    private List<RunnableOutput> invoke(List<RunnableHashMap> runnableInputs, RunnableConfig config, Consumer<Object> chunkConsumer) {
        return bound.batch(runnableInputs, config);
    }
}