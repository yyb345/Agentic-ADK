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
package com.alibaba.agentic.core.runner;

import com.alibaba.agentic.core.Application;
import com.alibaba.agentic.core.engine.delegation.domain.LlmRequest;
import com.alibaba.agentic.core.engine.delegation.domain.LlmResponse;
import com.alibaba.agentic.core.engine.node.FlowCanvas;
import com.alibaba.agentic.core.engine.node.sub.LlmFlowNode;
import com.alibaba.agentic.core.engine.node.sub.ToolFlowNode;
import com.alibaba.agentic.core.engine.utils.DelegationUtils;
import com.alibaba.agentic.core.executor.InvokeMode;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.tools.DashScopeTools;
import com.alibaba.fastjson.JSON;
import io.reactivex.rxjava3.core.Flowable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/10 19:18
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class })
public class RunnerTest {


    @Test
    public void testLlmOutputAllParamsToTool_Structured() {
        FlowCanvas flowCanvas = new FlowCanvas();

        // 1. LLM节点，用户输入全部写在prompt里，要求返回结构化JSON
        LlmRequest llmRequest = new LlmRequest();
        llmRequest.setModel("dashscope");
        llmRequest.setModelName("qwen-plus");
        llmRequest.setMessages(List.of(
                new LlmRequest.Message("user", "我的appId是5845862de55340179393a57d78067365,我想要查询杭州未来7天的天气\n" +
                        "\n" +
                        "你需要帮我整理成工具调用形式,最终仅输出下面的形式:\n" +
                        "\n" +
                        "{\"appId\": xxxx, \"prompt\": xxxx}  ")
        ));
        LlmFlowNode llmNode = new LlmFlowNode(llmRequest); // 默认输出 {"text": xxx}
        llmNode.setId("llmNode");

        // 2. Tool节点，不写任何参数，只用LLM输出
        ToolFlowNode toolNode = new ToolFlowNode(
                List.of(),
                new DashScopeTools() {
                    @Override
                    public String name() { return "weather_tool"; }
                    @Override
                    public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                        LlmResponse llmResponse = DelegationUtils.getResultOfNode(systemContext, "llmNode", LlmResponse.class);
                        assert llmResponse != null;
                        Map<String, Object> paramMap = JSON.parseObject(llmResponse.getChoices().get(0).getText(), Map.class);
                        return super.run(paramMap, systemContext);
                    }
                }
        );

        // 串联两个节点
        flowCanvas.setRoot(llmNode.next(toolNode));

        Request request = new Request().setInvokeMode(InvokeMode.SYNC);
        Flowable<Result> flowable = new Runner().run(flowCanvas, request);
        flowable.blockingIterable().forEach(result -> System.out.println("Final result: " + result));
    }

}
