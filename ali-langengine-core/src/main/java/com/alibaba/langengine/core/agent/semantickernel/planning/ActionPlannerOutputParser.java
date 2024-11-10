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
package com.alibaba.langengine.core.agent.semantickernel.planning;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.agent.AgentFinish;
import com.alibaba.langengine.core.agent.AgentNextStep;
import com.alibaba.langengine.core.agent.AgentOutputParser;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.core.util.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Data
public class ActionPlannerOutputParser extends AgentOutputParser<AgentNextStep> {

    private Map<String, BaseTool> toolMap;

    @Override
    public AgentNextStep parse(String text) {
        //{
        //  "plan":{
        //      "rationale": "the list contains a function that can add values",
        //      "function": "math.Add",
        //      "parameters": {
        //          "input": 110,
        //          "Amount": 990
        //      }
        //  }
        //}
        log.warn("ActionPlanner AgentOutputParser parse:" + text);

        String generatedPlanString = JsonUtils.extractJson(text);
        JSONObject generatedPlan = JSON.parseObject(generatedPlanString);

        Map<String, Object> context = new TreeMap<>();
        JSONObject planObject = generatedPlan.getJSONObject("plan");
        String skFunction = planObject.getString("function");
        JSONObject parameters = planObject.getJSONObject("parameters");
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                context.put(entry.getKey(), entry.getValue().toString());
            }
        }
        BaseTool skFunctionTool = getToolMap().get(skFunction);
        if(skFunctionTool == null) {
            return null;
        }
        ToolExecuteResult toolExecuteResult = skFunctionTool.run(JSON.toJSONString(context));

        Map<String, Object> returnValues = new HashMap<>();
        returnValues.put("output", toolExecuteResult.getOutput());
        AgentFinish agentFinish = new AgentFinish();
        agentFinish.setReturnValues(returnValues);
        agentFinish.setLog(text);
        return agentFinish;
    }
}
