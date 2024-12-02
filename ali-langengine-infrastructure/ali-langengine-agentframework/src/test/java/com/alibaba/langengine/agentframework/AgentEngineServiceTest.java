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
package com.alibaba.langengine.agentframework;

import com.alibaba.langengine.agentframework.config.BeanConfig;
import com.alibaba.langengine.agentframework.config.MockAgentProcessDefineManager;
import com.alibaba.langengine.agentframework.engine.AgentOriginRequest;
import com.alibaba.langengine.agentframework.manager.AgentProcessDefineManager;
import com.alibaba.langengine.agentframework.model.FrameworkEngineConfiguration;
import com.alibaba.langengine.agentframework.model.agent.domain.AgentRelation;
import com.alibaba.langengine.agentframework.model.agent.flow.FlowAgentModel;
import com.alibaba.langengine.agentframework.process.AgentProcessService;
import com.alibaba.langengine.agentframework.utils.FrameworkSystemContextUtils;
import com.alibaba.langengine.agentframework.utils.IOUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.agentframework.model.AgentResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.alibaba.langengine.agentframework.delegation.constants.SystemConstant.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {  UnitTestApplication.class, BeanConfig.class, MockAgentProcessDefineManager.class})
public class AgentEngineServiceTest {

    @Resource
    private AgentProcessService agentProcessService;
    @Resource
    private FrameworkEngineConfiguration agentEngineConfiguration;

    @Test
    public void test_run() {
        String processDefinitionContent = IOUtils.read("bpmn/demo.bpmn20.xml");

        Map<String, Object> context = new HashMap<>();

        AgentResult<Map<String,Object>> agentResult = agentProcessService.startProcessInstanceByBpmnXml(processDefinitionContent, context);
        System.out.println("agentResult:" + JSON.toJSONString(agentResult));
    }

    @Test
    public void test_run_llm() {
        String processDefinitionContent = IOUtils.read("bpmn/langengine_core_llm.bpmn20.xml");

        AgentOriginRequest agentOriginRequest = new AgentOriginRequest();
        agentOriginRequest.setQuery("你是谁？");

        Map<String, Object> context = new HashMap<>();
        FrameworkSystemContextUtils.putSystemContext(context, agentOriginRequest);
        context.put("context", "请幽默回复我");

        AgentResult<Map<String,Object>> agentResult = agentProcessService.startProcessInstanceByBpmnXml(processDefinitionContent, context);
        System.out.println("agentResult:" + JSON.toJSONString(agentResult));
    }

    @Test
    public void test_run_llm_stream() {
        String processDefinitionContent = IOUtils.read("bpmn/langengine_core_llm.bpmn20.xml");

        AgentOriginRequest agentOriginRequest = new AgentOriginRequest();
        Consumer<Object> chunkConsumer = e -> {
            System.out.println(JSON.toJSONString(e));
        };
        agentOriginRequest.setChunkConsumer(chunkConsumer);
        agentOriginRequest.setRequestId("123456");
        agentOriginRequest.setQuery("你是谁？");
        Map<String, Object> context = new HashMap<>();
        FrameworkSystemContextUtils.putSystemContext(context, agentOriginRequest);

        AgentResult<Map<String,Object>> agentResult = agentProcessService.startProcessInstanceByBpmnXml(processDefinitionContent, context);
        System.out.println("agentResult:" + JSON.toJSONString(agentResult));
    }

    @Test
    public void test_run_cot() {
        String processDefinitionContent = IOUtils.read("bpmn/cot.bpmn20.xml");

        String relationString = "{\"componentList\":[{\"componentId\":\"getWeather\",\"componentVersion\":\"1.0.0\",\"inputParams\":[{\"exampleValue\":\"\",\"key\":\"10.244185488604467\",\"paramDesc\":\"city\",\"paramName\":\"location\",\"paramType\":\"string\",\"required\":false}]}],\"executeType\":0,\"llmSuggestEnabled\":false,\"rolePrompt\":\"# 角色\\n你是一个天气预报专家，你擅长查询天气\"}";
        AgentRelation agentRelation = JSON.parseObject(relationString, AgentRelation.class);
        FlowAgentModel agentModel = new FlowAgentModel();
        agentModel.setRelation(agentRelation);

        AgentOriginRequest agentOriginRequest = new AgentOriginRequest();
        Consumer<Object> chunkConsumer = e -> {
            System.out.println(JSON.toJSONString(e));
        };
        agentOriginRequest.setChunkConsumer(chunkConsumer);
        agentOriginRequest.setRequestId("123456");
        agentOriginRequest.setQuery("今天杭州的天气如何？");
        agentOriginRequest.setAgentModel(agentModel);
        Map<String, Object> context = new HashMap<>();
        FrameworkSystemContextUtils.putSystemContext(context, agentOriginRequest);

        AgentResult<Map<String,Object>> agentResult = agentProcessService.startProcessInstanceByBpmnXml(processDefinitionContent, context);
        System.out.println("agentResult:" + JSON.toJSONString(agentResult));
    }

    @Test
    public void test_startProcessInstance() {
        String processDefinitionContent = IOUtils.read("bpmn/ioc_process.bpmn20.xml");

        Map<String, Object> context = new HashMap<>();
        Map<String, Object> system = new HashMap<String, Object>() {{
            put(QUERY_KEY, "<EnoughInfo>");
            put(PROMPT_INFO_KEY, agentEngineConfiguration.getPromptService().getPromptInfo());
        }};
        context.put(SYSTEM_KEY, system);
        AgentResult<Map<String,Object>> agentResult = agentProcessService.startProcessInstanceByBpmnXml(processDefinitionContent, context);
        System.out.println("agentResult:" + agentResult);
    }
}
