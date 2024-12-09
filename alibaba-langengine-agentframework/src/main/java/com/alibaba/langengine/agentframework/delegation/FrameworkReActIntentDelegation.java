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
package com.alibaba.langengine.agentframework.delegation;

import com.alibaba.langengine.agentframework.delegation.constants.SceneIntentRecognitionConstant;
import com.alibaba.langengine.agentframework.delegation.provider.DelegationHelper;
import com.alibaba.langengine.agentframework.delegation.tools.IntentTool;
import com.alibaba.langengine.agentframework.utils.FrameworkUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.agentframework.model.AgentEngineConfiguration;
import com.alibaba.langengine.agentframework.model.agent.domain.InstanceFlow;
import com.alibaba.langengine.agentframework.model.domain.GlobalVariable;
import com.alibaba.langengine.agentframework.model.enums.AgentMagicErrorCode;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.outputparser.QwenStructuredChatOutputParser;
import com.alibaba.langengine.core.prompt.PromptConverter;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.core.runnables.*;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.alibaba.langengine.agentframework.delegation.constants.SystemConstant.*;
import static com.alibaba.langengine.agentframework.delegation.provider.DelegationHelper.getSystemValue;

/**
 * ReAct方式-意图识别节点
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Component
public class FrameworkReActIntentDelegation extends FrameworkDelegationBase<Object> implements SceneIntentRecognitionConstant {

    private static final String INTENT_RECOGNITION_TEMPLATE = "Answer the following questions as best you can. You have access to the following tools:\n" +
            "\n" +
            "{tools}\n" +
            "\n" +
            "Use the following format:\n" +
            "\n" +
            "Question: the input question you must answer\n" +
            "Thought: you should always think about what to do\n" +
            "Action: the action to take, should be one of [{tool_names}]\n" +
            "Action Input: the input to the action\n" +
            "Observation: the result of the action\n" +
            "\n" +
            "Begin!\n" +
            "\n" +
            "{examples}" +
            "Question: {input}\n";

    @Resource
    private AgentEngineConfiguration agentEngineConfiguration;

    @Override
    public Object executeInternal(ExecutionContext executionContext, JSONObject properties, JSONObject request) {
        String requestId = DelegationHelper.getSystemValue(request, REQUEST_ID_KEY);
        String query = getSystemValue(request, QUERY_KEY);

        List<InstanceFlow> sceneInstanceFlows;
        if (getSystemValue(request, SCENE_INSTANCE_FLOWS_KEY) != null) {
            sceneInstanceFlows = JSON.parseArray(getSystemValue(request, SCENE_INSTANCE_FLOWS_KEY), InstanceFlow.class);
        } else {
            sceneInstanceFlows = new ArrayList<>();
        }

        // 全局变量
        List<GlobalVariable> globalVariables;
        if (getSystemValue(request, GLOBAL_VARIABLES_KEY) != null) {
            globalVariables = JSON.parseArray(getSystemValue(request, GLOBAL_VARIABLES_KEY), GlobalVariable.class);
        } else {
            globalVariables = new ArrayList<>();
        }
        Map<String, Object> variablesMap = null;
        if (!CollectionUtils.isEmpty(globalVariables)) {
            variablesMap = globalVariables.stream().collect(Collectors.toMap(GlobalVariable::getVariableName,
                    output -> output.getDesc(),
                    (existing, replacement) -> existing));
        }

        // model
        BaseLanguageModel model = agentEngineConfiguration.getBaseLanguageModel();

        RunnableInterface modelBinding = model.bind(new RunnableHashMap() {{
            put("stop", Arrays.asList(new String[]{"Observation:"}));
        }});

        String destinationsStr = getToolDesc(sceneInstanceFlows, variablesMap);

        List<BaseTool> tools = sceneInstanceFlows.stream().map(flow -> {
            BaseTool tool = new IntentTool();
            tool.setName(flow.getProcessDefinitionId());
            tool.setDescription(flow.getDescription());
            return tool;
        }).collect(Collectors.toList());

        Map<String, Object> args = new HashMap<>();
        args.put("tools", destinationsStr);
        args.put("tool_names", String.join(", ", sceneInstanceFlows.stream().map(InstanceFlow::getProcessDefinitionId).toArray(String[]::new)));
        args.put("examples", getExamples(sceneInstanceFlows, variablesMap));
        String template = PromptConverter.replacePrompt(INTENT_RECOGNITION_TEMPLATE, args);
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate(template);

        RunnableInterface assign = Runnable.assign(new RunnableHashMap() {{
            put("input", new RunnableLambda(e -> e.get("input").toString()));
        }});

        RunnableAgent agent = new RunnableAgent(Runnable.sequence(
                assign,
                prompt,
                modelBinding
        ), new QwenStructuredChatOutputParser());

        RunnableAgentExecutor agentExecutor = new RunnableAgentExecutor(agent, tools);

        Object runnableOutput = agentExecutor.invoke(new RunnableHashMap() {{
            put("input", query);
        }});
        log.info("intent output:" + JSON.toJSONString(runnableOutput));
        String result = ((Map<String, Object>) runnableOutput).get("output").toString();
        if (StringUtils.isEmpty(result)) {
            log.error("intent output empty");
            throw new AgentMagicException(AgentMagicErrorCode.INTENT_SYSTEM_ERROR, "intent output empty", requestId);
        }
        Map<String, Object> resultMap = JSON.parseObject(result, Map.class);
        if (resultMap.get("name") == null) {
            log.error("intent name empty");
            throw new AgentMagicException(AgentMagicErrorCode.INTENT_SYSTEM_ERROR, "intent name empty", requestId);
        }
        String destination = resultMap.get("name").toString();
        request.put("destination", destination);
        if (resultMap.get("variables") != null) {
            Map<String, Object> variablesResultMap = (Map<String, Object>) resultMap.get("variables");
            for (Map.Entry<String, Object> variablesResultEntry : variablesResultMap.entrySet()) {
                request.put(FrameworkUtils.SYS_INTENT_PREFIX + variablesResultEntry.getKey(), variablesResultEntry.getValue());
            }
        }

        List<InstanceFlow> subFlows = sceneInstanceFlows.stream()
                .filter(promptInfo -> promptInfo.getProcessDefinitionId().equals(destination))
                .collect(Collectors.toList());
        if (subFlows.size() == 0) {
            throw new AgentMagicException(AgentMagicErrorCode.INTENT_SYSTEM_ERROR, "subFlow not exists.destination is " + destination, requestId);
        }
        InstanceFlow subFlow = subFlows.get(0);

        Map<String, Object> data = new HashMap<>();
        data.put("intentionId", subFlow.getProcessDefinitionId());
        data.put("intentionVersion", subFlow.getVersion());
        data.put("intentionContent", subFlow.getProcessDefinitionContent());
        return data;
    }

    private String getExamples(List<InstanceFlow> sceneInstanceFlows, Map<String, Object> variablesMap) {
        String exampleTemplate = "Question: %s\n" +
                "Thought: %s\n" +
                "Action: %s\n" +
                "Action Input: %s\n" +
                "Observation: %s工具被执行\n\n";
        StringBuilder builder = new StringBuilder();
        sceneInstanceFlows.stream().forEach(sceneInstanceFlow -> {
            if(!CollectionUtils.isEmpty(sceneInstanceFlow.getSceneQuerys())) {
                for (String sceneQuery : sceneInstanceFlow.getSceneQuerys()) {
                    builder.append(String.format(exampleTemplate,
                            sceneQuery,
                            sceneInstanceFlow.getDescription(),
                            sceneInstanceFlow.getProcessDefinitionId(),
                            "{}", // TODO 先设置为空json对象，后续可以通过fewshot样本去写入
                            sceneInstanceFlow.getProcessDefinitionId()));
                }
            }
        });
        return builder.toString();
    }

    private String getToolDesc(List<InstanceFlow> sceneInstanceFlows, Map<String, Object> variablesMap) {
        String toolTemplateStr = "%s: %s";
        if(!CollectionUtils.isEmpty(variablesMap)) {
            toolTemplateStr = toolTemplateStr + " Parameters: " + JSON.toJSONString(variablesMap) + " Format the arguments as a JSON object.";
        }
        String finalToolTemplateStr = toolTemplateStr;
        List<String> destinations = sceneInstanceFlows.stream()
                .map(p -> String.format(finalToolTemplateStr,  p.getProcessDefinitionId(), p.getDescription()))
                .collect(Collectors.toList());
        String destinationsStr = destinations.stream().collect(Collectors.joining("\n"));
        return destinationsStr;
    }
}
