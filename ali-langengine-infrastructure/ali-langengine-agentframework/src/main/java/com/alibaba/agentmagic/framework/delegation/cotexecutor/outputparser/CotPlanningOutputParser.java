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
package com.alibaba.agentmagic.framework.delegation.cotexecutor.outputparser;

import com.alibaba.langengine.core.agent.planexecute.Plan;
import com.alibaba.langengine.core.agent.planexecute.Step;
import com.alibaba.langengine.core.outputparser.BaseOutputParser;
import com.alibaba.langengine.core.runnables.RunnableHashMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Data
public class CotPlanningOutputParser extends BaseOutputParser<RunnableHashMap> {

    @Override
    public RunnableHashMap parse(String text) {
        if(text == null) {
            return null;
        }
        List<Step> steps = new ArrayList<>();
        Pattern pattern = Pattern.compile("\n\\s*\\d+\\. ");
        String[] substrings = pattern.split(text);
        for (String substring : substrings) {
            if (!substring.trim().isEmpty()) {
                Step step = new Step();
                step.setValue(substring.trim());
                steps.add(step);
            }
        }
        if(steps.size() == 0) {
            log.info("steps size is " + steps.size());
            return null;
        }
        if(!steps.get(0).getValue().startsWith("Plan")) {
            return null;
        }

        steps = steps.stream().skip(1).collect(Collectors.toList());
        if(steps.size() == 0) {
            log.info("steps skip 1 after size is " + steps.size());
            return null;
        }

        RunnableHashMap runnableHashMap = new RunnableHashMap();
        Plan plan = new Plan();
        plan.setSteps(steps);
        runnableHashMap.put("plan", plan);
        return runnableHashMap;
    }
}

