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
package com.alibaba.langengine.core.agent.planexecute.planners;

import com.alibaba.langengine.core.agent.planexecute.Plan;
import com.alibaba.langengine.core.agent.planexecute.PlanOutputParser;
import com.alibaba.langengine.core.chain.LLMChain;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LLMPlanner extends BasePlanner {

    private LLMChain llmChain;

    private PlanOutputParser outputParser;

    private List<String> stop;

    @Override
    public Plan plan(Map<String, Object> inputs, Map<String, Object> extraAttributes) {
        if(stop != null && stop.size() > 0) {
            inputs.put("stop", stop);
        }
        Map<String, Object> outputs = llmChain.run(inputs, extraAttributes);
        return outputParser.parse(outputs.get("text").toString());
    }
}
