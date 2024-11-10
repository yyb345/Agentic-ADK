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
package com.alibaba.langengine.core.chain.router;

import com.alibaba.langengine.core.chain.Chain;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Chain that outputs the name of a destination chain and the inputs to it.
 * 输出目标链的名称及其输入的链。
 *
 * @author xiaoxuan.lp
 */
@Data
public abstract class RouterChain extends Chain {

    private static final String[] OUPUT_KEYS = new String[] { "destination", "next_inputs" };

    public List<String> getOutputKeys() {
        return Arrays.asList(OUPUT_KEYS);
    }

    public Route route(Map<String, Object> inputs) {
        Map<String, Object> result = super.call(inputs);
        Route route = new Route();
        route.setDestination(result.containsKey("destination") ? (String)result.get("destination") : null);
        route.setNextInputs(inputs);
        // modify by gonglang.wyw
        if(result.containsKey("next_inputs")) {
            route.getNextInputs().putAll((Map<String, Object>)result.get("next_inputs"));
        }
        return route;
    }
}
