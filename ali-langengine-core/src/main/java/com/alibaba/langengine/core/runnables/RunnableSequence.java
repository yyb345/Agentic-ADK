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

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * A sequence of runnables, where the output of each is the input of the next.
 * RunnableSequence is the most important composition operator in LangChain as it is used in virtually every chain.
 *
 * @author xiaoxuan.lp
 */
@Data
public class RunnableSequence extends Runnable<Object, RunnableOutput> {

    private List<RunnableInterface> steps;

    public RunnableSequence(String name, RunnableInterface... steps) {
        setName(name);
        setSteps(Arrays.asList(steps));
    }

    public RunnableSequence(RunnableInterface... steps) {
        setSteps(Arrays.asList(steps));
    }

    @Override
    public RunnableOutput invoke(Object input, RunnableConfig config) {
        return invoke(input, config, null);
    }

    private RunnableOutput invoke(Object input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        Object output = null;
        for (RunnableInterface step : getSteps()) {
            if(chunkConsumer != null) {
                if(config != null && config.isStreamLog()) {
                    output = step.streamLog(input, config, chunkConsumer);
                } else {
                    output = step.stream(input, config, chunkConsumer);
                }
            } else {
                output = step.invoke(input, config);
            }
            if (output instanceof RunnableInput) {
                input = output;
            }
        }
        if(output != null) {
            if(output instanceof String) {
                RunnableStringVar stringVar = new RunnableStringVar();
                stringVar.setValue((String)output);
                return stringVar;
            } else if(output instanceof RunnableOutput) {
                return (RunnableOutput) output;
            }
        }
        return null;
    }

    @Override
    public RunnableOutput stream(Object input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        return invoke(input, config, chunkConsumer);
    }

    @Override
    public RunnableOutput streamLog(Object input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        if(config == null) {
            config = new RunnableConfig();
        }
        config.setStreamLog(true);
        return invoke(input, config, chunkConsumer);
    }
}