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
package com.alibaba.langengine.core.agent.reactdoc;

import com.alibaba.langengine.core.agent.Agent;
import com.alibaba.langengine.core.prompt.BasePromptTemplate;
import com.alibaba.langengine.core.tool.BaseTool;
import lombok.Data;

import java.util.List;

import static com.alibaba.langengine.core.agent.reactdoc.PromptConstants.PROMPT;
import static com.alibaba.langengine.core.agent.reactdoc.PromptConstants.PROMPT_CH;

/**
 * Agent for the ReAct chain.
 *
 * @author xiaoxuan.lp
 */
@Data
public class ReActDocstoreAgent extends Agent {

    public static BasePromptTemplate createPrompt(List<BaseTool> tools, boolean isCH) {
        return isCH ? PROMPT_CH : PROMPT;
    }

    @Override
    public String observationPrefix() {
        return "Observation: ";
    }

    @Override
    public String stop() {
        return "\nObservation:";
    }

    @Override
    public String llmPrefix() {
        return "Thought:";
    }

    @Override
    public List<String> getInputKeys() {
        return null;
    }
}
