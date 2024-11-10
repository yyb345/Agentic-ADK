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
package com.alibaba.langengine.core.agent.planexecute;

import com.alibaba.langengine.core.agent.planexecute.executors.BaseExecutor;
import com.alibaba.langengine.core.agent.planexecute.planners.BasePlanner;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chain.Chain;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * PlanAndExecute
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class PlanAndExecute extends Chain {

    private BasePlanner planner;

    private BaseExecutor executor;

    private BaseStepContainer stepContainer = new ListStepContainer();

    private String inputKey = "input";

    private String outputKey = "output";

    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        Plan plan = planner.plan(inputs, extraAttributes);
        log.warn("plan:" + plan);
        for (Step step : plan.getSteps()) {
            Map<String, Object> newInputs = new HashMap<>();
            newInputs.putAll(inputs);
            newInputs.put("previous_steps", stepContainer.getFinalResponse());
            newInputs.put("current_step", step.getValue());
            newInputs.put("objective", inputs.get(inputKey));
            StepResponse response = executor.step(newInputs);
            stepContainer.addStep(step, response);
        }
        Map<String, Object> output = new HashMap<>();
        output.put(outputKey, stepContainer.getFinalResponse());
        return output;
    }

    @Override
    public List<String> getInputKeys() {
        return Arrays.asList(new String[]{ inputKey });
    }

    @Override
    public List<String> getOutputKeys() {
        return Arrays.asList(new String[]{ outputKey });
    }
}
