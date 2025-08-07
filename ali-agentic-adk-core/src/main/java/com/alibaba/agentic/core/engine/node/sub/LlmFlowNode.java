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
import com.alibaba.agentic.core.engine.delegation.DelegationLlm;
import com.alibaba.agentic.core.engine.delegation.domain.LlmRequest;
import com.alibaba.agentic.core.engine.node.FlowNode;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dom4j.Element;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class LlmFlowNode extends FlowNode {

    private LlmRequest llmRequest;

    public LlmFlowNode(LlmRequest llmRequest) {
        this.llmRequest = llmRequest;
    }

    @Override
    protected String getNodeType() {
        return NodeType.LLM;
    }

    @Override
    protected String getDelegationClassName() {
        return DelegationLlm.class.getName();
    }

    @Override
    protected void generate(Element processElement) {
        super.generate(processElement);
    }

    @Override
    protected void addProperties(Element serviceTask) {
        Element extensionElements = serviceTask.addElement("extensionElements");
        Element properties = extensionElements.addElement("smart:properties");
        Map<String, Object> reqMap = JSONObject.parseObject(JSONObject.toJSONString(llmRequest), Map.class);
        reqMap.forEach((k, v) -> {
            if (v != null) {
                Element prop = properties.addElement("smart:property");
                prop.addAttribute("name", k);
                prop.addAttribute("value", v instanceof String ? (String) v : JSONObject.toJSONString(v));
            }
        });
    }
}
