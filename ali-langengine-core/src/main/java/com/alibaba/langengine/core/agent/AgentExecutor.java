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
package com.alibaba.langengine.core.agent;

import com.alibaba.langengine.core.agent.structured2.StructuredChatAgentV2;
import com.alibaba.langengine.core.callback.BaseCallbackManager;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chain.Chain;
import com.alibaba.langengine.core.memory.BaseChatMemory;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.alibaba.langengine.core.util.Constants.CALLBACK_ERROR_KEY;

/**
 * 使用工具的代理执行器
 *
 * @author xiaoxuan.lp
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AgentExecutor extends Chain {

    private BaseSingleActionAgent agent;
    private List<BaseTool> tools;
    private boolean returnIntermediateSteps;
    private Integer maxIterations = 10;
    /**
     * The maximum amount of wall clock time to spend in the execution
     * loop.
     */
    private Double maxExecutionTime;
    /**
     * The method to use for early stopping if the agent never
     * returns `AgentFinish`. Either 'force' or 'generate'.
     * <p>
     * `"force"` returns a string saying that it stopped because it met a
     *     time or iteration limit.
     * <p>
     * `"generate"` calls the agent's LLM Chain one final time to generate
     *     a final answer based on the previous steps.
     */
    private String earlyStoppingMethod = "generate";

    public static final String FORCE_STOPPING_METHOD = "force";
    public static final String GENERATE_STOPPING_METHOD = "generate";

    /**
     * force stopping content
     */
    private String forceStoppingContent;

    private static final String DEFAULT_FORCE_STOPPING_CONTENT = "Agent stopped due to iteration limit or time limit.";

    private boolean handleParsingErrors = false;
    private boolean isCH = false;

    @Override
    public void setCallbackManager(BaseCallbackManager callbackManager) {
        super.setCallbackManager(callbackManager);
        if(this.agent != null) {
            this.agent.setCallbackManager(callbackManager);
        }
        if(this.tools != null && callbackManager != null) {
            this.tools.stream().forEach(tool -> tool.setCallbackManager(callbackManager.getChild()));
        }
    }

    public void setAgent(BaseSingleActionAgent agent) {
        this.agent = agent;
        if (getCallbackManager() != null) {
            this.agent.setCallbackManager(getCallbackManager());
        }
    }

    public void setTools(List<BaseTool> tools) {
        this.tools = tools;
        if (tools != null && getCallbackManager() != null) {
            this.tools.stream().forEach(tool -> tool.setCallbackManager(getCallbackManager().getChild()));
        }
    }

    @Override
    public List<String> getInputKeys() {
        return agent.getInputKeys();
    }

    @JsonIgnore
    @Override
    public List<String> getOutputKeys() {
        if(returnIntermediateSteps) {
            List<String> values = new ArrayList<>();
            values.addAll(agent.returnValues());
            values.add("intermediate_steps");
            return values;
        } else {
            return agent.returnValues();
        }
    }

    /**
     * Run text through and get agent response.
     */
    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        try {
            if(executionContext != null) {
                executionContext.setExecutionType(null);
                executionContext.setChildExecutionType(null);
            }
            onChainStart(this, inputs, executionContext);

            List<AgentAction> intermediateSteps = new ArrayList<>();
            // Let's start tracking the number of iterations and time elapsed
            int iterations = 0;
            double timeElapsed = 0.0d;
            double startTime = System.currentTimeMillis() / 1000.0;

            // Construct a mapping of tool name to tool for easy lookup
            Map<String, BaseTool> nameToToolMap = new TreeMap<>();
            for (BaseTool tool : tools) {
                nameToToolMap.put(tool.getName(), tool);
            }

            // We now enter the agent loop (until it returns something).
            while (shouldContinue(iterations, timeElapsed)) {
                Object nextStepOutput = takeNextStep(nameToToolMap, inputs, intermediateSteps, consumer, executionContext, extraAttributes);
                if (nextStepOutput == null) {
                    if(executionContext != null) {
                        executionContext.setExecutionType(null);
                        executionContext.setChildExecutionType(null);
                    }
                    onChainEnd(this, inputs, null, executionContext);

                    AgentFinish output = returnStoppedResponse();
                    return returnAgent(output, intermediateSteps, executionContext);
                }
                if (nextStepOutput instanceof AgentFinish) {
                    Map<String, Object> outputs = returnAgent((AgentFinish) nextStepOutput, intermediateSteps, executionContext);
                    if(executionContext != null) {
                        executionContext.setExecutionType(null);
                        executionContext.setChildExecutionType(null);
                    }
                    onChainEnd(this, inputs, ((AgentFinish) nextStepOutput).getReturnValues(), executionContext);

                    //如果包含记忆，清除记忆
//                    if(getMemory() != null) {
//                        getMemory().clear();
//                    }
                    return outputs;
                }

                AgentAction nextAction = (AgentAction) nextStepOutput;
                if(!CollectionUtils.isEmpty(nextAction.getNextTools())) {
                    tools = nextAction.getNextTools();
                    agent = StructuredChatAgentV2.fromLlmAndTools(getLlm(), tools, getCallbackManager(),
                            null, null, null, null, null, null, getMemory(), isCH(),
                            (Agent) agent);
                }

                intermediateSteps.add(nextAction);
                iterations += 1;
                timeElapsed = System.currentTimeMillis() / 1000.0 - startTime;
            }

            AgentFinish output = returnStoppedResponse();
            Map<String, Object> outputs = returnAgent(output, intermediateSteps, executionContext);
            if(executionContext != null) {
                executionContext.setExecutionType(null);
                executionContext.setChildExecutionType(null);
            }
            onChainEnd(this, inputs, null, executionContext);

            return outputs;
        } catch (Throwable e) {
            if(executionContext != null) {
                executionContext.setExecutionType(null);
                executionContext.setChildExecutionType(null);
            }
            onChainError(this, inputs, e, executionContext);

            if(getCallbackManager() != null) {
                Map<String, Object> outputs = new HashMap<>();
                outputs.put(CALLBACK_ERROR_KEY, executionContext);
                return outputs;
            }
            throw e;
        }
    }

    @Override
    public CompletableFuture<Map<String, Object>> callAsync(Map<String, Object> inputs, ExecutionContext executionContext, Map<String, Object> extraAttributes) {
        return CompletableFuture.supplyAsync(() -> call(inputs, executionContext, null, extraAttributes));
    }

    public BaseTool lookupTool(String name) {
        List<BaseTool> filters = tools.stream().
                filter(tool -> tool.getName().equals(name))
                .collect(Collectors.toList());
        if(filters.size() > 0) {
            return filters.get(0);
        }
        return null;
    }

    public boolean shouldContinue(int iterations, Double timeElapsed) {
        if(maxIterations != null && iterations >= maxIterations) {
            return false;
        }
        if (maxExecutionTime != null && timeElapsed >= maxExecutionTime) {
            return false;
        }
        return true;
    }

    private Map<String, Object> returnAgent(AgentFinish output, List<AgentAction> intermediateSteps, ExecutionContext executionContext) {
        if(executionContext == null) {
            executionContext = new ExecutionContext();
        }
        if(getCallbackManager() != null) {
            executionContext.setChain(this);
            executionContext.setAgentFinish(output);
            getCallbackManager().onAgentFinish(executionContext);
        }
        Map<String, Object> finalOutput = output.getReturnValues();
        if(returnIntermediateSteps) {
            finalOutput.put("intermediate_steps", intermediateSteps);
        }
        return finalOutput;
    }

    /**
     * 在思想-行动-观察循环中迈出一步。这可以控制代理如何做出选择并根据选择采取行动
     *
     * @param nameToToolMap
     * @param inputs
     * @param intermediateSteps
     * @return
     */
    public Object takeNextStep(Map<String, BaseTool> nameToToolMap,
                               Map<String, Object> inputs,
                               List<AgentAction> intermediateSteps,
                               Consumer<String> consumer,
                               ExecutionContext executionContext,
                               Map<String, Object> extraAttributes) {
        // Call the LLM to see what to do.
        Object output = agent.plan(intermediateSteps, inputs, consumer, executionContext, extraAttributes);
        if(output == null) {
            return null;
        }
        // If the tool chosen is the finishing tool, then we end and return.
        if(output instanceof AgentFinish) {
            //如果包含记忆，清除记忆
//            if(getMemory() != null) {
//                getMemory().clear();
//            }

            if(FORCE_STOPPING_METHOD.equals(earlyStoppingMethod)) {
                Map<String, Object> returnValues = new HashMap<>();
                returnValues.put("output", !StringUtils.isEmpty(forceStoppingContent) ? forceStoppingContent : DEFAULT_FORCE_STOPPING_CONTENT);
                AgentFinish agentFinish = (AgentFinish) output;
                agentFinish.setReturnValues(returnValues);
                return agentFinish;
            }
            return output;
        }
        List<AgentAction> actions = new ArrayList<>();
        if(output instanceof AgentAction) {
            actions.add((AgentAction) output);
        }
        for (AgentAction agentAction : actions) {
            onAgentAction(this, agentAction, executionContext);

            //可以模糊匹配
            String toolName = containActionName(nameToToolMap, agentAction.getTool());
            if(!StringUtils.isEmpty(toolName)) {
                BaseTool tool = nameToToolMap.get(toolName);
//                boolean returnDirect = tool.isReturnDirect();
                if(executionContext != null) {
                    executionContext.setChildExecutionType("tool-" + intermediateSteps.size());
                }
                ToolExecuteResult toolExecuteResult = tool.run(agentAction.getToolInput(), executionContext);
                if(toolExecuteResult == null) {
                    return null;
                }

                //对于工具返回结果也增加记忆
                if(getMemory() != null) {
                    BaseChatMemory chatMemory = (BaseChatMemory) getMemory();
                    chatMemory.getChatMemory().addToolMessage(toolExecuteResult.getOutput());
                }

                if(toolExecuteResult.isInterrupted()) {
                    Map<String, Object> returnValues = new HashMap<>();
                    returnValues.put("output", toolExecuteResult.getOutput());
                    AgentFinish agentFinish = new AgentFinish();
                    agentFinish.setReturnValues(returnValues);
                    agentFinish.setLog(toolExecuteResult.getOutput());
                    return agentFinish;
                }

                if(!CollectionUtils.isEmpty(toolExecuteResult.getNextTools())) {
                    agentAction.setNextTools(toolExecuteResult.getNextTools());
                }
                agentAction.setObservation(toolExecuteResult.getOutput());
                return agentAction;
            }
        }
        return null;
    }

    /**
     * 模糊匹配
     *
     * @param nameToToolMap
     * @param toolName
     * @return
     */
    private String containActionName(Map<String, BaseTool> nameToToolMap, String toolName) {
        for (Map.Entry<String, BaseTool> nameToToolEntry : nameToToolMap.entrySet()) {
            String key = nameToToolEntry.getKey();
            if(toolName.indexOf(key) >= 0) {
                return key;
            }
        }
        return null;
    }

    private void onAgentAction(Chain chain, AgentAction agentAction, ExecutionContext executionContext) {
        if (executionContext == null) {
            executionContext = new ExecutionContext();
        }
        if(getCallbackManager() != null) {
            executionContext.setChain(chain);
            executionContext.setAgentAction(agentAction);
            getCallbackManager().onAgentAction(executionContext);
        }
    }

    /**
     * Return response when agent has been stopped due to max iterations.
     */
    private AgentFinish returnStoppedResponse() {
//        if (FORCE_STOPPING_METHOD.equals(earlyStoppingMethod)) {
            Map<String, Object> returnValues = new HashMap<>();
            returnValues.put("output", !StringUtils.isEmpty(forceStoppingContent) ? forceStoppingContent : DEFAULT_FORCE_STOPPING_CONTENT);
            AgentFinish agentFinish = new AgentFinish();
            agentFinish.setReturnValues(returnValues);
            agentFinish.setLog("");
            return agentFinish;
//        } else {
//            throw new RuntimeException("Got unsupported early_stopping_method `" + earlyStoppingMethod + "`");
//        }
    }
}
