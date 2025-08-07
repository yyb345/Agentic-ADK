package com.alibaba.agentic.core.engine.node.sub;

import com.alibaba.agentic.core.engine.constants.NodeType;
import com.alibaba.agentic.core.engine.delegation.DelegationFlowCanvas;
import com.alibaba.agentic.core.engine.node.FlowCanvas;
import com.alibaba.agentic.core.engine.node.FlowNode;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.MapUtils;
import org.dom4j.Element;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class ReferenceFlowNode extends FlowNode {

    //直接执行的下一个流程引用
    private FlowCanvas canvas;

    private String flowDefinitionId;

    private String flowVersion;

    private Map<String, Object> parameter;

    @Override
    protected String getNodeType() {
        return NodeType.REFERENCE;
    }

    @Override
    protected String getDelegationClassName() {
        return DelegationFlowCanvas.class.getName();
    }

    @Override
    protected void addProperties(Element serviceTask) {
        Element extensionElements = serviceTask.addElement("extensionElements");
        Element properties = extensionElements.addElement("smart:properties");

        Element flowDefinitionIdElement = properties.addElement("smart:property");
        flowDefinitionIdElement.addAttribute("name", "flowDefinitionId");
        flowDefinitionIdElement.addAttribute("value", flowDefinitionId);

        Element flowVersionElement = properties.addElement("smart:property");
        flowVersionElement.addAttribute("name", "flowVersion");
        flowVersionElement.addAttribute("value", flowVersion);

        Element requestProperties = properties.addElement("smart:property");
        requestProperties.addAttribute("name", "parameter");
        requestProperties.addAttribute("value", MapUtils.isEmpty(parameter) ? "{}" : JSONObject.toJSONString(parameter));

    }


}
