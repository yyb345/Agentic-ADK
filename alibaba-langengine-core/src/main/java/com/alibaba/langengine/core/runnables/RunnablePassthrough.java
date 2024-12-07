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

/**
 * A runnable to passthrough inputs unchanged or with additional keys.
 *
 *     This runnable behaves almost like the identity function, except that it
 *     can be configured to add additional keys to the output, if the input is a
 *     dict.
 *
 *     The examples below demonstrate this runnable works using a few simple
 *     chains. The chains rely on simple lambdas to make the examples easy to execute
 *     and experiment with.
 *
 * @author xiaoxuan.lp
 */
public class RunnablePassthrough extends Runnable<Object, RunnableOutput> {

    @Override
    public RunnableOutput invoke(Object o, RunnableConfig config) {
        return null;
    }

    @Override
    public RunnableOutput stream(Object o, RunnableConfig config, Consumer<Object> chunkConsumer) {
        return null;
    }
}
