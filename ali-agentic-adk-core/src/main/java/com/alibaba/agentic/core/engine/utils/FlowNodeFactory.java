/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.core.engine.utils;

import com.alibaba.agentic.core.engine.dto.FlowDefinition;
import com.alibaba.agentic.core.engine.node.FlowCanvas;
import com.alibaba.agentic.core.engine.node.sub.ReferenceFlowNode;
import com.alibaba.agentic.core.engine.node.sub.ToolFlowNode;
import com.alibaba.agentic.core.engine.node.sub.ToolParam;
import com.alibaba.agentic.core.tools.FunctionTool;

import java.util.List;
import java.util.Map;

public class FlowNodeFactory {

    public static ToolFlowNode createToolNode(String name,
                                              List<ToolParam> paramList,
                                              String functionToolName) {
        ToolFlowNode node = new ToolFlowNode();
        node.setParamList(paramList);
        node.setFunctionToolName(functionToolName);
        node.setName(name);
        return node;
    }

    public static ToolFlowNode createToolNode(String name, List<ToolParam> paramList, FunctionTool functionTool) {
        ToolFlowNode node = new ToolFlowNode();
        node.setParamList(paramList);
        node.setName(name);
        node.setFunctionToolName(functionTool.name());
        return node;
    }


    public static ReferenceFlowNode createReferenceNode(FlowDefinition flowDefinition, Map<String, Object> parameter) {
        ReferenceFlowNode flowNode = new ReferenceFlowNode();
        flowNode.setFlowDefinitionId(flowNode.getFlowDefinitionId());
        flowNode.setFlowVersion(flowNode.getFlowVersion());
        flowNode.setParameter(parameter);

        return flowNode;
    }


    public static ReferenceFlowNode createReferenceNode(FlowCanvas flowCanvas, Map<String, Object> parameter) {
        FlowDefinition flowDefinition = flowCanvas.deploy();
        return createReferenceNode(flowDefinition, parameter);
    }

}
