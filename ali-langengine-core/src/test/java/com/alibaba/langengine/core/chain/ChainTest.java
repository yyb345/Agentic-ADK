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

import java.util.HashMap;
import org.junit.jupiter.api.Test;

import com.alibaba.langengine.core.callback.ExecutionContext;


/**
 * @author aihe.ah
 * @time 2023/11/14
 * 功能说明：
 */
public class ChainTest {

    @Test
    public void testOnChainStart_withExecutionContextContainingChildChain() {
        // success
        ConversationChain chain = new ConversationChain();
        HashMap<String, Object> inputs = new HashMap<>();
        inputs.put("a", "b");
        inputs.put("c", "d");

        ExecutionContext executionContext = new ExecutionContext();
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("d", "e");
        stringObjectHashMap.put("f", "g");
        stringObjectHashMap.put("c", "c");
        executionContext.setInputs(stringObjectHashMap);
        chain.onChainStart(chain, inputs, executionContext);
        System.out.println(executionContext.getInputs());
    }
}