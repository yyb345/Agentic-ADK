package com.alibaba.langengine.multiagent;

import com.alibaba.langengine.agentframework.process.AgentProcessService;
import com.alibaba.langengine.agentframework.utils.IOUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.agentframework.model.AgentResult;
import com.alibaba.langengine.multiagent.config.BeanConfig;
import com.alibaba.langengine.multiagent.config.MockAgentProcessDefineManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {  UnitTestApplication.class, BeanConfig.class, MockAgentProcessDefineManager.class })
public class MultiAgentExecutorTest {

    @Resource
    private AgentProcessService agentProcessService;

    @Test
    public void test_run_math_calculation_groupchat() {
        String processDefinitionContent = IOUtils.read("bpmn/math_calculation_groupchat.bpmn20.xml");

        String query = "2 * 3 + 10 / 5 等于多少？";
        System.out.println("question: " + query + "\n");
        Map<String, Object> context = new HashMap<>();
        context.put("query", query);

        AgentResult<Map<String,Object>> agentResult = agentProcessService.startProcessInstanceByBpmnXml(processDefinitionContent, context);
        System.out.println("agentResult:" + JSON.toJSONString(agentResult));
    }

    @Test
    public void test_run_math_calculation_network() {
        String processDefinitionContent = IOUtils.read("bpmn/math_calculation_network.bpmn20.xml");

        String query = "2 * 3 + 10 / 5 等于多少？";
        System.out.println("question: " + query + "\n");
        Map<String, Object> context = new HashMap<>();
        context.put("query", query);

        AgentResult<Map<String,Object>> agentResult = agentProcessService.startProcessInstanceByBpmnXml(processDefinitionContent, context);
        System.out.println("agentResult:" + JSON.toJSONString(agentResult));
    }

    @Test
    public void test_run_math_calculation_groupchat2() {
        String processDefinitionContent = IOUtils.read("bpmn/math_calculation_groupchat2.bpmn20.xml");

        String query = "2 * 3 + 10 / 5 等于多少？";
        System.out.println("question: " + query + "\n");
        Map<String, Object> context = new HashMap<>();
        context.put("query", query);

        AgentResult<Map<String,Object>> agentResult = agentProcessService.startProcessInstanceByBpmnXml(processDefinitionContent, context);
        System.out.println("agentResult:" + JSON.toJSONString(agentResult));
    }

    @Test
    public void test_run_twochat() {
        String processDefinitionContent = IOUtils.read("bpmn/twochat.bpmn20.xml");

        String query = "I have a number between 1 and 100. Guess it!";
        System.out.println("question: " + query + "\n");
        Map<String, Object> context = new HashMap<>();
        context.put("query", query);

        AgentResult<Map<String,Object>> agentResult = agentProcessService.startProcessInstanceByBpmnXml(processDefinitionContent, context);
        System.out.println("agentResult:" + JSON.toJSONString(agentResult));
    }

    @Test
    public void test_run_groupchat() {
        String processDefinitionContent = IOUtils.read("bpmn/groupchat.bpmn20.xml");

        Map<String, Object> context = new HashMap<>();

        AgentResult<Map<String,Object>> agentResult = agentProcessService.startProcessInstanceByBpmnXml(processDefinitionContent, context);
        System.out.println("agentResult:" + JSON.toJSONString(agentResult));
    }

    @Test
    public void test_run() {
        String processDefinitionContent = IOUtils.read("bpmn/demo.bpmn20.xml");

        Map<String, Object> context = new HashMap<>();

        AgentResult<Map<String,Object>> agentResult = agentProcessService.startProcessInstanceByBpmnXml(processDefinitionContent, context);
        System.out.println("agentResult:" + JSON.toJSONString(agentResult));
    }
}
