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
package com.alibaba.agentic.core.engine.node.sub;

import com.alibaba.agentic.core.engine.constants.NodeType;
import com.alibaba.agentic.core.engine.delegation.DelegationTool;
import com.alibaba.agentic.core.engine.node.FlowNode;
import com.alibaba.agentic.core.tools.BaseTool;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.collections.CollectionUtils;
import org.dom4j.Element;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ToolFlowNode extends FlowNode {

    private String functionToolName;

    private List<ToolParam> paramList;

    private BaseTool baseTool;

    public ToolFlowNode() {
    }

    public ToolFlowNode(String functionToolName, List<ToolParam> paramList) {
        this.functionToolName = functionToolName;
        this.paramList = paramList;
    }

    public ToolFlowNode(List<ToolParam> paramList, BaseTool baseTool) {
        this.baseTool = baseTool;
        this.functionToolName = baseTool.name();
        this.paramList = paramList;
    }


    @Override
    protected String getNodeType() {
        return NodeType.TOOL;
    }

    @Override
    protected String getDelegationClassName() {
        return DelegationTool.class.getName();
    }

    @Override
    protected void generate(Element processElement) {
        super.generate(processElement);
        if (baseTool != null) {
            DelegationTool.register(baseTool);
        }
    }

    @Override
    protected void addProperties(Element serviceTask) {
        Element extensionElements = serviceTask.addElement("extensionElements");
        Element properties = extensionElements.addElement("smart:properties");

        Element functionToolNameProperties = properties.addElement("smart:property");
        functionToolNameProperties.addAttribute("name", "functionToolName");
        functionToolNameProperties.addAttribute("value", functionToolName);

        Element paramListProperties = properties.addElement("smart:property");
        paramListProperties.addAttribute("name", "paramList");
        paramListProperties.addAttribute("value", CollectionUtils.isEmpty(paramList) ? "[]" : JSONObject.toJSONString(paramList));

    }

}
