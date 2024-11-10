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
package com.alibaba.langengine.core.chain;

import com.alibaba.langengine.core.callback.ExecutionContext;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Chain transform chain output.
 * 链变换链输出。
 *
 * @author xiaoxuan.lp
 */
@Data
public class TransformChain extends Chain {

    private List<String> inputVariables;

    private List<String> outputVariables;

    private Function<Map<String, Object>, Map<String, Object>> transform;

    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return transform.apply(inputs);
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
