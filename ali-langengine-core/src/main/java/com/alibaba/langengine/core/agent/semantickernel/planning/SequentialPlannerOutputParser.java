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
package com.alibaba.langengine.core.agent.semantickernel.planning;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.agent.AgentFinish;
import com.alibaba.langengine.core.agent.AgentNextStep;
import com.alibaba.langengine.core.agent.AgentOutputParser;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.core.util.XmlUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.tree.DefaultAttribute;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class SequentialPlannerOutputParser extends AgentOutputParser<AgentNextStep> {

    private Map<String, BaseTool> toolMap;

    @Override
    public AgentNextStep parse(String text) {
        log.warn("SequentialPlanner AgentOutputParser parse:" + text);

//                text = "<plan>\n" +
//                        "    <!-- Generate ideas for a poem about Edson Arantes do Nascimento -->\n" +
//                        "    <function.WriterSkill.Brainstorm input=\"Write a poem about Edson Arantes do Nascimento\" />\n" +
//                        "\n" +
//                        "    <!-- Summarize the generated ideas -->\n" +
//                        "    <function.SummarizeSkill.Summarize input=\"$WriterSkill.Brainstorm.output\" setContextVariable=\"SUMMARIZED_IDEAS\" />\n" +
//                        "\n" +
//                        "    <!-- Generate the poem using the summarized ideas -->\n" +
//                        "    <function.ShakespeareSkill.shakespeare input=\"$SUMMARIZED_IDEAS\" setContextVariable=\"POEM\" />\n" +
//                        "\n" +
//                        "    <!-- Translate the poem into Chinese -->\n" +
//                        "    <function.WriterSkill.Translate input=\"$POEM\" language=\"Chinese\" appendToResult=\"RESULT__TRANSLATED_POEM\" />\n" +
//                        "</plan>";

        // FIXME 需要优化下代码逻辑
        try {
            Map<String, Object> appendToResultValues = new HashMap<>();

            Document document = XmlUtils.parseText(text.trim());
            Element rootElement = document.getRootElement();
            if(rootElement != null) {
                Map<String, Object> context = new HashMap<>();

                Map<String, Object> contextVariables = new HashMap<>();

                List<Element> routeElements = rootElement.elements();
                for (Element routeElement : routeElements) {
                    String functionName = routeElement.getName();

                    Map<String, Object> input = new HashMap<>();

                    List<Attribute> attributes = routeElement.attributes();
                    String contextVariableField = null;
                    String appendToResultValueField = null;
                    for (Object attribute : attributes) {
                        if(attribute instanceof DefaultAttribute) {
                            DefaultAttribute defaultAttribute = (DefaultAttribute)attribute;
                            if("setContextVariable".equals(defaultAttribute.getName())) {
                                contextVariables.put(defaultAttribute.getValue(), null);
                                contextVariableField = defaultAttribute.getValue();
                                continue;
                            }
                            if("appendToResult".equals(defaultAttribute.getName())) {
                                appendToResultValues.put(defaultAttribute.getValue(), "");
                                appendToResultValueField = defaultAttribute.getValue();
                                continue;
                            }
                            String attributeValue = defaultAttribute.getStringValue();
                            input.put(defaultAttribute.getName(), attributeValue);
                        }
                    }
                    String skFunction = functionName.replace("function.", "");

                    BaseTool skFunctionTool = toolMap.get(skFunction);
                    if(skFunctionTool == null) {
                        return null;
                    }

                    for (Map.Entry<String, Object> entry : input.entrySet()) {
                        String tplName = "";
                        String content = entry.getValue().toString();

                        if(content.indexOf("$") >= 0
                                || content.indexOf("_output") >= 0
                                || content.indexOf("_OUTPUT") >= 0) {
                            content = content.replace(".", "_")
                                    .replace("_OUTPUT", "_output");
                        }
                        StringWriter writer = new StringWriter();
                        Map<String, Object> allVaribles = new HashMap<>();
                        allVaribles.putAll(contextVariables);
                        allVaribles.putAll(appendToResultValues);
//                        VelocityContext velocityContext = new VelocityContext(allVaribles);
//                        Velocity.evaluate(velocityContext, writer, tplName, content);
                        String output = writer.getBuffer().toString();

                        context.put(entry.getKey(), output);
                    }
                    ToolExecuteResult toolExecuteResult = skFunctionTool.run(JSON.toJSONString(context));
                    String output = toolExecuteResult.getOutput();
                    if(!StringUtils.isEmpty(contextVariableField)) {
                        contextVariables.put(contextVariableField, output);
                    }
                    if(!StringUtils.isEmpty(appendToResultValueField)) {
                        appendToResultValues.put(appendToResultValueField, output);
                    }
                    if(StringUtils.isEmpty(contextVariableField) && StringUtils.isEmpty(appendToResultValueField)) {
                        contextVariables.put(skFunction.replace(".", "_"), output);
                        contextVariables.put(skFunction.replace(".", "_") + "_output", output);
                    }
                }
            }

            Map<String, Object> returnValues = new HashMap<>();
            returnValues.put("output", JSON.toJSONString(appendToResultValues));
            AgentFinish agentFinish = new AgentFinish();
            agentFinish.setReturnValues(returnValues);
            agentFinish.setLog(text);
            return agentFinish;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
