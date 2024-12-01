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
import com.alibaba.langengine.agentframework.process.AgentProcessService;
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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {  UnitTestApplication.class, BeanConfig.class })
public class AgentEngineServiceTest {

    @Resource
    private AgentProcessService agentProcessService;

    @Test
    public void test_run() {
        String processDefinitionContent = IOUtils.read("bpmn/demo.bpmn20.xml");

        Map<String, Object> context = new HashMap<>();

        AgentResult<Map<String,Object>> agentResult = agentProcessService.startProcessInstanceByBpmnXml(processDefinitionContent, context);
        System.out.println("agentResult:" + JSON.toJSONString(agentResult));
    }
}
