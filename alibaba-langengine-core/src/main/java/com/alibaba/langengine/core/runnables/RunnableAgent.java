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
package com.alibaba.langengine.core.runnables;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.agent.AgentNextStep;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.outputparser.BaseOutputParser;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Runnable Agent
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class RunnableAgent extends Runnable<RunnableInput, AgentNextStep> {

    private RunnableSequence runnableSequence;
    private BaseOutputParser<AgentNextStep> outputParser;

    public RunnableAgent(RunnableSequence runnableSequence, BaseOutputParser<AgentNextStep> outputParser) {
        this.runnableSequence = runnableSequence;
        this.outputParser = outputParser;
    }

    @Override
    public AgentNextStep invoke(RunnableInput runnableInput, RunnableConfig config) {
        return invoke(runnableInput, config, null);
    }

    @Override
    public AgentNextStep stream(RunnableInput runnableInput, RunnableConfig config, Consumer<Object> chunkConsumer) {
        return invoke(runnableInput, config, chunkConsumer);
    }

    private AgentNextStep invoke(RunnableInput runnableInput, RunnableConfig config, Consumer<Object> chunkConsumer) {
        RunnableOutput runnableOutput;
        if(chunkConsumer != null) {
            if(config != null && config.isStreamLog()) {
                runnableOutput = runnableSequence.streamLog(runnableInput, config, chunkConsumer);
            } else {
                runnableOutput = runnableSequence.stream(runnableInput, config, chunkConsumer);
            }
        } else {
            runnableOutput = runnableSequence.invoke(runnableInput, config);
        }
        if(runnableOutput instanceof BaseMessage) {
            BaseMessage baseMessage = (BaseMessage) runnableOutput;
            if (baseMessage.getAdditionalKwargs() != null) {
                // TODO 可能需要增加tool_call
                Map<String, Object> functionCall = (Map<String, Object>) baseMessage.getAdditionalKwargs().get("function_call");
                if (functionCall != null && functionCall.get("name") != null) {
                    log.info("RunnableAgent message:{}", JSON.toJSONString(baseMessage));
                    AgentAction agentAction = new AgentAction();
                    agentAction.setTool((String) functionCall.get("name"));
                    agentAction.setToolInput((String) functionCall.get("arguments"));
                    agentAction.setLog(baseMessage.getContent());
                    return agentAction;
                }
            }
            return outputParser.invoke((RunnableInput) runnableOutput, config);
        } else if(runnableOutput instanceof RunnableStringVar) {
            return outputParser.invoke((RunnableStringVar) runnableOutput, config);
        }
        throw new RuntimeException("RunnableAgent invoke error.runnableOutput is class of" + runnableOutput.getClass());
    }
}