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

import com.alibaba.langengine.core.agent.AgentAction;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * RunnableLambda
 *
 * @author xiaoxuan.lp
 */
public class RunnableAgentLambda extends Runnable<List<AgentAction>, Object> {

    private Function<List<AgentAction>, Object> transform;

    public RunnableAgentLambda(Function<List<AgentAction>, Object> transform) {
        this.transform = transform;
    }

    @Override
    public Object invoke(List<AgentAction> input, RunnableConfig config) {
        return transform.apply(input);
    }

    @Override
    public Object stream(List<AgentAction> input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        return transform.apply(input);
    }
}