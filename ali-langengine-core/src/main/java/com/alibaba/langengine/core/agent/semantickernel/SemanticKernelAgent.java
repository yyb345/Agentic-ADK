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
package com.alibaba.langengine.core.agent.semantickernel;

import com.alibaba.langengine.core.agent.Agent;
import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.agent.semantickernel.planning.ActionPlanner;
import com.alibaba.langengine.core.agent.semantickernel.planning.BasePlanner;
import com.alibaba.langengine.core.agent.semantickernel.planning.SequentialPlanner;
import com.alibaba.langengine.core.agent.semantickernel.planning.StepwisePlanner;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.StructuredTool;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SemanticKernel Planner Agent实现
 *
 * @author xiaoxuan.lp
 */
@Data
public class SemanticKernelAgent extends Agent {

    @Override
    public String observationPrefix() {
        return stop;
    }

    private String stop = "Observation: ";

    @Override
    public String llmPrefix() {
        return "";
    }

    @Override
    public String constructScratchpad(List<AgentAction> intermediateSteps) {
        String agentScratchpad = _constructScratchpad(intermediateSteps);
        return agentScratchpad;
    }

    private String _constructScratchpad(List<AgentAction> intermediateSteps) {
        String thoughts = "";
        for (AgentAction action : intermediateSteps) {
            thoughts += action.getLog();
            if(thoughts.endsWith("\n")) {
                thoughts = thoughts.substring(0, thoughts.length() - 1);
            }
            thoughts += "\n" + observationPrefix() + "\n" + action.getObservation() + "\n" + llmPrefix();
        }
        return thoughts;
    }

    public static Agent fromLlmAndTools(BaseLanguageModel llm, List<BaseTool> tools, BasePlanner planner) {
        List<String> structSchemas = new ArrayList<>();
        Map<String, BaseTool> toolMap = new HashMap<>();
        for (BaseTool tool : tools) {
            if(tool instanceof StructuredTool) {
                String skFunction = String.format("%s.%s", tool.getName(), tool.getFunctionName());
                toolMap.put(skFunction, tool);

                StructuredTool structuredTool = (StructuredTool)tool;
                String structSchema;
                if(planner instanceof SequentialPlanner) {
                    structSchema = structuredTool.formatSemantickernelBasicPrompt("inputs");
                } else if(planner instanceof ActionPlanner) {
                    structSchema = structuredTool.formatSemantickernelActionPrompt();
                } else if(planner instanceof StepwisePlanner) {
                    structSchema = structuredTool.formatSemantickernelStepwisePrompt();
                } else {
                    structSchema = structuredTool.formatSemantickernelBasicPrompt();
                }
                structSchemas.add(structSchema);
            }
        }
        String formattedStructSchemas = String.join("\n\n", structSchemas);
        Map<String, Object> toolInputs = new HashMap<>();
        toolInputs.put("available_functions", formattedStructSchemas);

        //创建计划
        planner.createPlan(toolInputs);
        planner.setToolMap(toolMap);

        LLMChain llmChain = new LLMChain();
        llmChain.setLlm(llm);
        llmChain.setPrompt(planner.getPromptTemplate());

        SemanticKernelAgent agent = new SemanticKernelAgent();
        agent.setLlmChain(llmChain);
        agent.setAllowedTools(tools.stream().map(tool -> tool.getName()).collect(Collectors.toList()));
        agent.setOutputParser(planner.getPlannerOutputParser());
        if(planner instanceof  ActionPlanner) {
            agent.setStop("#END-OF-PLAN");
        } else if(planner instanceof SequentialPlanner) {
            agent.setStop("<!-- END -->");
        }
        else if(planner instanceof StepwisePlanner) {
            agent.setStop("\n[OBSERVATION]");
        }
        return agent;
    }
}
