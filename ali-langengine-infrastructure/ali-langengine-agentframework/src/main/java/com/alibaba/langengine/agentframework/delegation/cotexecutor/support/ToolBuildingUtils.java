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
package com.alibaba.langengine.agentframework.delegation.cotexecutor.support;

import com.alibaba.langengine.agentframework.delegation.FrameworkCotCallingDelegation;
import com.alibaba.langengine.agentframework.config.ComponentStreamCallback;
import com.alibaba.langengine.agentframework.delegation.cotexecutor.tools.ComponentTool;
import com.alibaba.langengine.agentframework.model.agent.domain.ComponentCallingInput;
import com.alibaba.langengine.agentframework.model.domain.FrameworkSystemContext;
import com.alibaba.langengine.agentframework.model.service.ToolCallingService;
import com.alibaba.langengine.core.callback.CallbackManager;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionDefinition;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionItem;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionParameter;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionProperty;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.StructuredParameter;
import com.alibaba.langengine.core.tool.StructuredSchema;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具构建辅助工具
 *
 * @author xiaoxuan.lp
 */
public class ToolBuildingUtils {

    public static void buildTool(FrameworkSystemContext systemContext, List<BaseTool> tools, List<FunctionDefinition> functions, Long startTime, FrameworkCotCallingDelegation delegation) {
        buildTool(systemContext, tools, functions, startTime, delegation, false);
    }

    public static void buildTool(FrameworkSystemContext systemContext, List<BaseTool> tools, List<FunctionDefinition> functions,
                                 Long startTime, FrameworkCotCallingDelegation delegation, boolean containStructSchema) {
        List<ComponentCallingInput> componentList = systemContext.getAgentRelation().getComponentList();
        if(CollectionUtils.isEmpty(componentList)) {
            return;
        }
        buildFunctions(componentList, functions, tools, true,
                startTime, delegation.getToolCallingService(), systemContext, containStructSchema);
        systemContext.setAllTools(tools);
    }

    public static void buildFunctions(List<ComponentCallingInput> componentList, List<FunctionDefinition> functions) {
        buildFunctions(componentList, functions, null, false, null, null, null, false);
    }

    public static void buildFunctions(List<ComponentCallingInput> componentList, List<FunctionDefinition> functions, List<BaseTool> tools,
                                      boolean filledTools, Long startTime, ToolCallingService toolCallingService, FrameworkSystemContext systemContext, Boolean containStructSchema) {
        for (ComponentCallingInput component : componentList) {
            List<String> required = new ArrayList<>();
            FunctionDefinition functionDefinition = new FunctionDefinition();
            functionDefinition.setName(component.getComponentId());
            functionDefinition.setDescription(component.getComponentDesc());

            Map<String, FunctionProperty> propertyMap = new HashMap<>();

            if (!CollectionUtils.isEmpty(component.getInputParams())) {
                component.getInputParams().stream().forEach(inputParam -> {
                    if (inputParam.getRequired() != null && inputParam.getRequired()) {
                        required.add(inputParam.getParamName());
                    }

                    FunctionProperty functionProperty = new FunctionProperty();
                    functionProperty.setType(inputParam.getParamType());

                    if ("stringList".equals(inputParam.getParamType())) {
                        functionProperty.setType("array");
                        FunctionItem items = new FunctionItem();
                        items.setType("string");
                        functionProperty.setItems(items);
                    } else if ("numberList".equals(inputParam.getParamType())) {
                        functionProperty.setType("array");
                        FunctionItem items = new FunctionItem();
                        items.setType("integer");
                        functionProperty.setItems(items);
                    } else if ("booleanList".equals(inputParam.getParamType())) {
                        functionProperty.setType("array");
                        FunctionItem items = new FunctionItem();
                        items.setType("boolean");
                        functionProperty.setItems(items);
                    } else if ("objectList".equals(inputParam.getParamType())) {
                        functionProperty.setType("array");
                        FunctionItem items = new FunctionItem();
                        items.setType("object");
                        functionProperty.setItems(items);
                    }

                    functionProperty.setDescription(inputParam.getParamDesc());
                    propertyMap.put(inputParam.getParamName(), functionProperty);
                });
            }

            FunctionParameter functionParameter = new FunctionParameter();
            functionParameter.setRequired(required);
            functionParameter.setProperties(propertyMap);
            functionDefinition.setParameters(functionParameter);
            functions.add(functionDefinition);

            if (filledTools) {
                ComponentTool tool = new ComponentTool();
                tool.setStartTime(startTime);
                tool.setName(component.getComponentId());
                tool.setDescription(component.getComponentDesc());
                tool.setFunctionName(component.getComponentVersion());
                tool.setToolCallingService(toolCallingService);
                tool.setMessageConsumer(systemContext.getChunkConsumer());
                tool.setRequestId(systemContext.getRequestId());
                tool.setUserId(systemContext.getUserId());
                tool.setApikeyCall(systemContext.getApikeyCall());
                tool.setApiKey(systemContext.getApikey());
                tool.setEnv(systemContext.getEnv());
                tool.setCallbackManager(new CallbackManager());
                tool.getCallbackManager().addHandler(new ComponentStreamCallback());
                tool.setSystemContext(systemContext);

                if (containStructSchema) {
                    if (functionDefinition.getParameters() != null
                            && functionDefinition.getParameters().getProperties() != null) {
                        StructuredSchema structuredSchema = new StructuredSchema();
                        List<String> requiredList = functionDefinition.getParameters().getRequired();
                        Map<String, FunctionProperty> functionPropertyMap = functionDefinition.getParameters().getProperties();
                        for (Map.Entry<String, FunctionProperty> entry : functionPropertyMap.entrySet()) {
                            StructuredParameter parameter = new StructuredParameter();
                            parameter.setName(entry.getKey());
                            parameter.setDescription(entry.getValue().getDescription());
                            if (requiredList != null && requiredList.size() > 0) {
                                parameter.setRequired(requiredList.contains(entry.getKey()));
                                structuredSchema.getParameters().add(parameter);
                            }
                        }
                        tool.setStructuredSchema(structuredSchema);
                    }
                }

                tools.add(tool);
            }
        }
    }
}
