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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Simple chain where the outputs of one step feed directly into next.
 * 简单的链，其中一个步骤的输出直接输入到下一步。
 *
 * @author xiaoxuan.lp
 */
@Data
public class SimpleSequentialChain extends Chain {

    private List<Chain> chains;

    private boolean stripOutputs;

    private String inputKey = "input";

    private String outputKey = "output";

    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        String _input = inputs.get(inputKey).toString();
        for (Chain chain : chains) {
            Map<String, Object> newInputs = new HashMap<>();
            newInputs.put(chain.getInputKeys().get(0), _input);
            Map<String, Object> result = chain.run(newInputs, extraAttributes);
            List<String> outputKeys = chain.getOutputKeys();
            _input = result.get(outputKeys.get(0)).toString();
            if(stripOutputs) {
                _input = _input.trim();
            }
        }
        Map<String, Object> outputs = new HashMap<>();
        outputs.put(outputKey, _input);
        return outputs;
    }

    @Override
    public List<String> getInputKeys() {
        return Arrays.asList(new String[]{ inputKey });
    }

    @Override
    public List<String> getOutputKeys() {
        return Arrays.asList(new String[] { outputKey });
    }
}
