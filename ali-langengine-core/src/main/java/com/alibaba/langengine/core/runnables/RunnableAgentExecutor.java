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
import com.alibaba.langengine.core.agent.AgentFinish;
import com.alibaba.langengine.core.agent.AgentNextStep;
import com.alibaba.langengine.core.agent.semantickernel.skill.SemanticKernelSkill;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * RunnableAgentExecutor
 *
 * @author xiaoxuan.lp
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class RunnableAgentExecutor extends Runnable<RunnableHashMap, RunnableHashMap>  {

    /**
     * runnable agent
     */
    private RunnableAgent agent;

    /**
     * tool list
     */
    private List<BaseTool> tools;

    /**
     * name to tool map
     */
    private Map<String, BaseTool> nameToToolMap = new HashMap<>();

    /**
     * max iterations
     */
    private Integer maxIterations = 10;

    /**
     * The maximum amount of wall clock time to spend in the execution loop.
     */
    private Double maxExecutionTime;

    /**
     * The method to use for early stopping if the agent never
     * returns `AgentFinish`. Either 'force' or 'generate'.
     */
    private String earlyStoppingMethod = "generate";

    public static final String FORCE_STOPPING_METHOD = "force";
    public static final String GENERATE_STOPPING_METHOD = "generate";

    /**
     * force stopping content
     */
    private String forceStoppingContent;

    public static final String INTERMEDIATE_STEPS_KEY = "intermediate_steps";

    private static final String DEFAULT_FORCE_STOPPING_CONTENT = "Agent stopped due to iteration limit or time limit.";

    /**
     * 工具prompt转换函数
     */
    private Function<List<BaseTool>, RunnableHashMap> toolPromptTransform;

    public RunnableAgentExecutor(RunnableAgent agent, List<BaseTool> tools) {
        this(agent, tools, null);
    }

    public RunnableAgentExecutor(RunnableAgent agent, List<BaseTool> tools, Function<List<BaseTool>, RunnableHashMap> toolPromptTransform) {
        this.agent = agent;
        this.tools = tools;
        this.toolPromptTransform = toolPromptTransform;
        for (BaseTool tool : tools) {
            if(tool instanceof SemanticKernelSkill) {
                String skFunction = String.format("%s.%s", tool.getName(), tool.getFunctionName());
                nameToToolMap.put(skFunction, tool);
            } else {
                nameToToolMap.put(tool.getName(), tool);
            }
        }
    }

    @Override
    public RunnableHashMap invoke(RunnableHashMap input, RunnableConfig config) {
        return invoke(input, config, null);
    }

    @Override
    public RunnableHashMap stream(RunnableHashMap input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        return invoke(input, config, chunkConsumer);
    }

    @Override
    public RunnableHashMap streamLog(RunnableHashMap input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        if(config == null) {
            config = new RunnableConfig();
        }
        config.setStreamLog(true);
        return invoke(input, config, chunkConsumer);
    }

    private RunnableHashMap invoke(RunnableHashMap input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        try {
            List<AgentAction> intermediateSteps = new ArrayList<>();

            int iterations = 0;
            double timeElapsed = 0.0d;
            double startTime = System.currentTimeMillis() / 1000.0;

            while (shouldContinue(iterations, timeElapsed)) {
                AgentNextStep nextStep = takeNextStep(intermediateSteps, input, config, chunkConsumer);
                if (nextStep == null) {
                    AgentFinish output = returnStoppedResponse();
                    return returnAgent(output, chunkConsumer);
                }
                if (nextStep instanceof AgentFinish) {
                    if(FORCE_STOPPING_METHOD.equals(earlyStoppingMethod)) {
                        Map<String, Object> returnValues = new HashMap<>();
                        returnValues.put("output", !StringUtils.isEmpty(forceStoppingContent) ? forceStoppingContent : DEFAULT_FORCE_STOPPING_CONTENT);
                        AgentFinish agentFinish = (AgentFinish) nextStep;
                        agentFinish.setReturnValues(returnValues);
                        return returnAgent(agentFinish, chunkConsumer);
                    }
                    return returnAgent((AgentFinish)nextStep, chunkConsumer);
                }

                AgentAction nextAction = (AgentAction) nextStep;
                // 如果包含动态工具配置
                if(!CollectionUtils.isEmpty(nextAction.getNextTools())) {
                    tools = nextAction.getNextTools();
                    if(toolPromptTransform != null) {
                        RunnableHashMap toolHashMap = toolPromptTransform.apply(tools);
                        if(toolHashMap != null && toolHashMap.size() > 0) {
                            if(toolHashMap.get("functions") != null) {
                                if(config == null) {
                                    config = new RunnableConfig();
                                }
                                config.setMetadata(new HashMap<>());
                                config.getMetadata().put("functions", toolHashMap.get("functions"));
                            } else {
                                input.putAll(toolHashMap);
                            }
                        }
                    }
                }

                intermediateSteps.add(nextAction);

                iterations += 1;
                timeElapsed = System.currentTimeMillis() / 1000.0 - startTime;
            }

            AgentFinish output = returnStoppedResponse();
            return returnAgent(output, chunkConsumer);
        } catch (Throwable e) {
            throw e;
        }
    }

    private AgentFinish returnStoppedResponse() {
//        if (FORCE_STOPPING_METHOD.equals(earlyStoppingMethod)) {
        Map<String, Object> returnValues = new HashMap<>();
        returnValues.put("output", !StringUtils.isEmpty(forceStoppingContent) ? forceStoppingContent : DEFAULT_FORCE_STOPPING_CONTENT);
        AgentFinish agentFinish = new AgentFinish();
        agentFinish.setReturnValues(returnValues);
        return agentFinish;
//        } else {
//            throw new RuntimeException("Got unsupported early_stopping_method `" + earlyStoppingMethod + "`");
//        }
    }

    /**
     * 判断是否继续执行
     *
     * @param iterations
     * @param timeElapsed
     * @return
     */
    private boolean shouldContinue(int iterations, Double timeElapsed) {
        if(maxIterations != null && iterations >= maxIterations) {
            return false;
        }
        if (maxExecutionTime != null && timeElapsed >= maxExecutionTime) {
            return false;
        }
        return true;
    }

    protected RunnableHashMap returnAgent(AgentFinish output, Consumer<Object> chunkConsumer) {
        Map<String, Object> finalOutput = output.getReturnValues();
        RunnableHashMap runnableHashMap = new RunnableHashMap();
        runnableHashMap.putAll(finalOutput);
        if(chunkConsumer != null) {
            chunkConsumer.accept(JSON.toJSONString(runnableHashMap));
        }
        return runnableHashMap;
    }

    public AgentNextStep takeNextStep(List<AgentAction> intermediateSteps, RunnableHashMap input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        input.put(INTERMEDIATE_STEPS_KEY, intermediateSteps);
        AgentNextStep agentNextStep;
        if(chunkConsumer != null) {
            if(config != null && config.isStreamLog()) {
                agentNextStep = agent.streamLog(input, config, chunkConsumer);
            } else {
                agentNextStep = agent.stream(input, config, chunkConsumer);
            }
        } else {
            agentNextStep = agent.invoke(input, config);
        }
        if(agentNextStep == null) {
            return null;
        }
        if(agentNextStep instanceof AgentFinish) {
            return agentNextStep;
        }

        AgentAction agentAction = (AgentAction) agentNextStep;
        log.info("RunnableAgentExecutor takeNextStep eq {}", JSON.toJSONString(agentAction));
        String toolName = containToolName(agentAction.getTool());
        if(StringUtils.isEmpty(toolName)) {
            AgentFinish agentFinish = returnStoppedResponse();
            return agentFinish;
        }
        BaseTool tool = nameToToolMap.get(toolName);

        ToolExecuteResult toolExecuteResult = tool.invoke(agentAction.getToolInput(), config, chunkConsumer);
        if (toolExecuteResult == null) {
            log.error("tool invoke error");
            AgentFinish agentFinish = returnStoppedResponse();
            return agentFinish;
        }
        if (toolExecuteResult.isInterrupted()) {
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

    protected String containToolName(String toolName) {
        if(nameToToolMap.get(toolName) != null) {
            return toolName;
        }

        for (Map.Entry<String, BaseTool> entry : nameToToolMap.entrySet()) {
            String key = entry.getKey();
            if(toolName.indexOf(key) >= 0 || key.indexOf(toolName) >= 0) {
                return key;
            }
        }
        return null;
    }

}
