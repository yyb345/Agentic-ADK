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

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * ListStepContainer
 *
 * @author xiaoxuan.lp
 */
@Data
public class ListStepContainer extends BaseStepContainer {

    private List<StepPair> steps = new ArrayList<>();

    @Override
    public void addStep(Step step, StepResponse stepResponse) {
        StepPair stepPair = new StepPair();
        stepPair.setStep(step);
        stepPair.setStepResponse(stepResponse);
        steps.add(stepPair);
    }

    @Override
    public String getFinalResponse() {
        if(steps.size() == 0) {
            return "";
        }
        return steps.get(steps.size() - 1).getStepResponse().getResponse();
    }
}
