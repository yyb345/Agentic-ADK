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
package com.alibaba.langengine.core.chain.sequential;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chain.Chain;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Chain where the outputs of one chain feed directly into next.
 * 一条链的输出直接馈送到下一条链的链。
 *
 * @author xiaoxuan.lp
 */
@Data
public class SequentialChain extends Chain {

    private List<Chain> chains;

    private List<String> inputVariables;

    private List<String> outputVariables;

    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        for (Chain chain : chains) {
            Map<String, Object> outputs = chain.run(inputs, true, null,  null, extraAttributes);
            inputs.putAll(outputs);
        }
        Map<String, Object> result = new HashMap<>();
        for (String outputVariable : outputVariables) {
            if(inputs.containsKey(outputVariable)) {
                result.put(outputVariable, inputs.get(outputVariable));
            }
        }
        return result;
    }

    @Override
    public List<String> getInputKeys() {
        return inputVariables;
    }

    @Override
    public List<String> getOutputKeys() {
        return outputVariables;
    }
}
