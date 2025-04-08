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

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * RunnableLampda
 *
 * @author xiaoxuan.lp
 */
public class RunnableLambda extends Runnable<RunnableHashMap, Object> {

    /**
     * 转换器函数
     */
    private Function<RunnableHashMap, Object> transform;

    public RunnableLambda(Function<RunnableHashMap, Object> transform) {
        this.transform = transform;
    }

    @Override
    public Object invoke(RunnableHashMap input, RunnableConfig config) {
        return transform.apply(input);
    }

    @Override
    public Object stream(RunnableHashMap input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        return transform.apply(input);
    }
}
