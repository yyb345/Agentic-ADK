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
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
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

        // function_call或者tool_calls的大模型调用返回结果
        if(runnableOutput instanceof BaseMessage) {
            BaseMessage baseMessage = (BaseMessage) runnableOutput;
            log.info("invoke baseMessage is {}", JSON.toJSONString(baseMessage));
            if (baseMessage.getAdditionalKwargs() != null) {
                if(baseMessage.getAdditionalKwargs().get("function_call") != null) {
                    Map<String, Object> functionCall = (Map<String, Object>) baseMessage.getAdditionalKwargs().get("function_call");
                    if (functionCall != null && functionCall.get("name") != null) {
                        log.info("RunnableAgent function_call message is {}", JSON.toJSONString(baseMessage));
                        AgentAction agentAction = new AgentAction();
                        agentAction.setTool((String) functionCall.get("name"));
                        agentAction.setToolInput((String) functionCall.get("arguments"));
                        agentAction.setLog(baseMessage.getContent());
                        return agentAction;
                    }
                } else if(baseMessage.getAdditionalKwargs().get("tool_calls") != null) {
                    List<Map<String, Object>> toolCalls = (List<Map<String, Object>>) baseMessage.getAdditionalKwargs().get("tool_calls");
                    if (!CollectionUtils.isEmpty(toolCalls)) {
                        AgentAction agentAction = new AgentAction();
                        agentAction.setLog(baseMessage.getContent());
                        agentAction.setActions(new ArrayList<>());
                        log.info("RunnableAgent tool_calls message is {}", JSON.toJSONString(baseMessage));

                        for(Map<String, Object> toolCall : toolCalls) {
                            if(toolCall != null && toolCall.get("function") != null) {
                                Map<String, Object> functionCall = (Map<String, Object>)toolCall.get("function");
                                AgentAction childAction = new AgentAction();
                                childAction.setTool((String) functionCall.get("name"));
                                childAction.setToolInput((String) functionCall.get("arguments"));
                                if(toolCall.get("id") != null) {
                                    childAction.setPrevId(toolCall.get("id").toString());
                                }
                                agentAction.getActions().add(childAction);
                            }
                        }

                        return agentAction;
                    }
                }
            }
            return outputParser.invoke((RunnableInput) runnableOutput, config);
        } else if(runnableOutput instanceof RunnableStringVar) {
            return outputParser.invoke((RunnableStringVar) runnableOutput, config);
        }
        throw new RuntimeException("RunnableAgent invoke error.runnableOutput is class of" + runnableOutput.getClass());
    }
}