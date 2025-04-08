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
package com.alibaba.langengine.core.agent.selfask;

import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.agent.AgentFinish;
import com.alibaba.langengine.core.agent.AgentOutputParser;
import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Output parser for the self-ask agent.
 *
 * @author xiaoxuan.lp
 */
@Data
public class SelfAskOutputParser extends AgentOutputParser {
    List<String> followups = Arrays.asList(new String[]{ "Follow up:", "Followup:", "追问:", "追问：" });

    List<String> finishStrings = Arrays.asList(new String[] { "So the final answer is:", "最终答案:" });

    @Override
    public Object parse(String text) {
        text = text.replaceAll("追问：", "追问: ");
        text = text.replaceAll("中间回答：", "中间回答: ");
        String[] lines = text.split("\n");
        String lastLine = lines[lines.length - 1];
        if (!followups.stream().anyMatch(followup -> lastLine.indexOf(followup) >= 0)) {
            if (!finishStrings.stream().anyMatch(finishString -> lastLine.indexOf(finishString) >= 0)) {
                throw new RuntimeException("Could not parse output: " + text);
            }
            List<String> matchFinishStrings = finishStrings.stream().filter(finishString -> lastLine.contains(finishString)).collect(Collectors.toList());
            AgentFinish agentFinish = new AgentFinish();
            agentFinish.setReturnValues(Collections.singletonMap("output", lastLine.substring(matchFinishStrings.get(0).length())));
            agentFinish.setLog(text);
            return agentFinish;
        }
        String afterColon = lastLine.split("[:：]")[1].trim();
        AgentAction agentAction = new AgentAction();
        agentAction.setTool("Intermediate Answer");
        agentAction.setToolInput(afterColon);
        agentAction.setLog(text);
        return agentAction;
    }

    @Override
    public String getParserType() {
        return "self_ask";
    }
}
