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
package com.alibaba.langengine.core.outputparser;

import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.agent.AgentFinish;
import com.alibaba.langengine.core.agent.AgentNextStep;
import com.alibaba.langengine.core.agent.AgentOutputParser;
import lombok.Data;

import java.util.Collections;

/**
 * Parses tool invocations and final answers in XML format.
 *
 * @author xiaoxuan.lp
 */
@Data
public class XMLAgentOutputParser extends AgentOutputParser<AgentNextStep> {

    @Override
    public AgentNextStep parse(String text) {
        if (text.contains("</tool>")) {
            String[] parts = text.split("</tool>");
            String tool = parts[0].split("<tool>")[1];
            String toolInput = parts[1].split("<tool_input>")[1];
            if (toolInput.contains("</tool_input>")) {
                toolInput = toolInput.split("</tool_input>")[0];
            }
            AgentAction agentAction = new AgentAction();
            agentAction.setTool(tool);
            agentAction.setToolInput(toolInput);
            agentAction.setLog(text);
            return agentAction;
        } else if (text.contains("<final_answer>")) {
            String answer = text.split("<final_answer>")[1].split("</final_answer>")[0];
            AgentFinish agentFinish = new AgentFinish();
            agentFinish.setReturnValues(Collections.singletonMap("output", answer));
            agentFinish.setLog(text);
            return agentFinish;
        } else {
            AgentFinish agentFinish = new AgentFinish();
            agentFinish.setReturnValues(Collections.singletonMap("output", text));
            agentFinish.setLog(text);
            return agentFinish;
        }
    }
}
