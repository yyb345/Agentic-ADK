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
package com.alibaba.langengine.core.agent.planexecute.executors;

import com.alibaba.langengine.core.agent.planexecute.StepResponse;
import com.alibaba.langengine.core.chain.Chain;
import lombok.Data;

import java.util.Map;

/**
 * ChainExecutor
 *
 * @author xiaoxuan.lp
 */
@Data
public class ChainExecutor extends BaseExecutor {

    private Chain chain;

    @Override
    public StepResponse step(Map<String, Object> inputs) {
        Map<String, Object> outputs = chain.run(inputs);
        StepResponse stepResponse = new StepResponse();
        stepResponse.setResponse(outputs.get("output").toString());
        return stepResponse;
    }
}
