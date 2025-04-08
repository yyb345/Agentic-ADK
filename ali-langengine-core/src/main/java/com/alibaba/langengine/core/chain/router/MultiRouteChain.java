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

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chain.Chain;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Use a single chain to route an input to one of multiple candidate chains.
 * 使用单个链将输入路由到多个候选链之一。
 *
 * @author xiaoxuan.lp
 */
@Data
public class MultiRouteChain extends Chain {

    /**
     * Chain that routes inputs to destination chains.
     * 将输入路由到目标链的链。
     */
    private RouterChain routerChain;

    /**
     * Chains that return final answer to inputs.
     * 返回输入最终答案的链。
     */
    private Map<String, Chain> destinationChains;

    /**
     * Default chain to use when none of the destination chains are suitable.
     * 当没有目标链适合时使用的默认链。
     */
    private Chain defaultChain;

    /**
     *  If True, use default_chain when an invalid destination name is provided. Defaults to False.
     *  如果为 True，则在提供无效目标名称时使用 default_chain。默认为 False。
     */
    private boolean silentErrors;


    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        Route route = routerChain.route(inputs);
        if(route.getDestination() == null) {
            return defaultChain.call(route.getNextInputs(), executionContext, consumer, extraAttributes);
        } else if(destinationChains.containsKey(route.getDestination())) {
            return destinationChains.get(route.getDestination()).call(route.getNextInputs(), executionContext, consumer, extraAttributes);
        } else if(silentErrors) {
            return defaultChain.call(route.getNextInputs(), executionContext, consumer, extraAttributes);
        }
        return null;
    }

    @Override
    public List<String> getInputKeys() {
        return routerChain.getInputKeys();
    }

    @Override
    public List<String> getOutputKeys() {
        return new ArrayList<>();
    }
}
