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
import com.alibaba.fastjson.JSONArray;
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

@Slf4j
@Data
public class BasicPlannerOutputParser extends AgentOutputParser<AgentNextStep> {

    private Map<String, BaseTool> toolMap;

    @Override
    public AgentNextStep parse(String text) {
        //{
        //    "input": "Valentine's Day Date Ideas",
        //    "subtasks": [
        //        {"function": "WriterSkill.Brainstorm"},
        //        {"function": "WriterSkill.Translate", "args": {"language": "Chinese"}}
        //    ]
        //}
        log.warn("BasicPlanner AgentOutputParser parse:" + text);

        String generatedPlanString = JsonUtils.extractJson(text);
        JSONObject generatedPlan = JSON.parseObject(generatedPlanString);

        Map<String, Object> context = new HashMap<>();
        context.put("input", generatedPlan.getString("input"));
        JSONArray subtasks = generatedPlan.getJSONArray("subtasks");
        for (Object subtaskObj : subtasks) {
            JSONObject subtask = (JSONObject) subtaskObj;
            String skFunction = subtask.getString("function");
            ToolExecuteResult toolExecuteResult = null;
            JSONObject args = subtask.getJSONObject("args");
            if (args != null) {
                for (Map.Entry<String, Object> entry : args.entrySet()) {
                    context.put(entry.getKey(), entry.getValue().toString());
                }
            }
            BaseTool skFunctionTool = toolMap.get(skFunction);
            if(skFunctionTool == null) {
                continue;
            }
            toolExecuteResult = skFunctionTool.run(JSON.toJSONString(context));
            context.put("input", toolExecuteResult.getOutput());
        }

        Map<String, Object> returnValues = new HashMap<>();
        returnValues.put("output", context.get("input").toString());
        AgentFinish agentFinish = new AgentFinish();
        agentFinish.setReturnValues(returnValues);
        agentFinish.setLog(text);
        return agentFinish;
    }
}
