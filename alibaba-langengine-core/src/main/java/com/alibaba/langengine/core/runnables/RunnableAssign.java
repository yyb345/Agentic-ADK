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
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.alibaba.langengine.core.runnables.RunnableAgentExecutor.INTERMEDIATE_STEPS_KEY;

/**
 * Runnable上下文赋值
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class RunnableAssign extends Runnable<RunnableHashMap, RunnableHashMap> {

    private Map<String, Object> assign;

    public RunnableAssign(String name, Map<String, Object> assign) {
        setName(name);
        this.assign = assign;
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
            for (Map.Entry<String, Object> entry : assign.entrySet()) {
                if(entry.getValue() instanceof String) {
                    if (input.get(entry.getValue()) == null) {
                        continue;
                    }
                    output.put(entry.getKey(), input.get(entry.getValue()));
                } else if(entry.getValue() instanceof RunnableLambda) {
                    RunnableLambda runnableLambda = (RunnableLambda) entry.getValue();
                    if(chunkConsumer != null) {
                        output.put(entry.getKey(), runnableLambda.stream(input, config, chunkConsumer));
                    } else {
                        output.put(entry.getKey(), runnableLambda.invoke(input, config));
                    }
                } else if(entry.getValue() instanceof RunnableAgentLambda) {
                    RunnableAgentLambda runnableLambda = (RunnableAgentLambda) entry.getValue();
                    if(chunkConsumer != null) {
                        output.put(entry.getKey(), runnableLambda.stream((List<AgentAction>) input.get(INTERMEDIATE_STEPS_KEY), config, chunkConsumer));
                    } else {
                        output.put(entry.getKey(), runnableLambda.invoke((List<AgentAction>) input.get(INTERMEDIATE_STEPS_KEY), config));
                    }
                }
            }
            return output;
        } catch (Throwable e) {
            log.error("RunnableAssign invoke error", e);
            throw new RuntimeException(e.toString());
        }
    }
}
