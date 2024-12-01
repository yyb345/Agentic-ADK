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
package com.alibaba.langengine.multiagent;

import com.alibaba.langengine.autogen.agentchat.contrib.RetrieveAssistantAgent;
import com.alibaba.langengine.autogen.agentchat.contrib.RetrieveConfig;
import com.alibaba.langengine.autogen.agentchat.contrib.RetrieveUserProxyAgent;
import com.alibaba.langengine.core.model.FakeAI;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RetrieveTest {

    @Test
    public void testTerminate() {
        ChatOpenAI llm = new ChatOpenAI();
        RetrieveAssistantAgent assistant = new RetrieveAssistantAgent("assistant", llm);

        RetrieveConfig retrieveConfig = RetrieveConfig.builder().
                task("CODE").
                contextMaxTokens(1000).
                build();

        RetrieveUserProxyAgent userProxy = new RetrieveUserProxyAgent("user_proxy",
                llm,
                new HashMap<String, Object>() {{
                    put("work_dir", "coding");
                }}
                );
        userProxy.setRetrieveConfig(retrieveConfig);

        userProxy.setMaxConsecutiveAutoReply(5);
        userProxy.initiateChat(assistant, true, false, "什么是孔雀翎", new HashMap<String, Object>() {{
            put("nResults", 5);
            put("searchString", "测试");
        }});
    }
    @Test
    public void testMockRetrieveDocs() {
        FakeAI llm = new FakeAI();
        RetrieveAssistantAgent assistant = new RetrieveAssistantAgent("assistant", llm);

        RetrieveConfig retrieveConfig =  RetrieveConfig.builder().
                task("QA").
                contextMaxTokens(195).
                build();
        RetrieveUserProxyAgent userProxy = new RetrieveUserProxyAgent("user_proxy",
                llm,
                new HashMap<String, Object>() {{
                    put("work_dir", "coding");
                }}){
            @Override
            public void retrieveDocs(String problem, int nResults, String searchString) {
                Map<String, Object> results = new HashMap<>();
                List<String> ids = new ArrayList<String>(){{
                    add("1");
                    add("2");
                    add("3");
                }};
                results.put("ids",ids);
                List<String> documents = new ArrayList<String>(){{
                    add("向量检索服务DashVector基于通义实验室自研的高效向量引擎Proxima内核，提供具备水平拓展能力的云原生、全托管的向量检索服务。DashVector将其强大的向量管理、向量查询等多样化能力，通过简洁易用的SDK/API接口透出，方便被上层AI应用迅速集成，从而为包括大模型生态、多模态AI搜索、分子结构分析在内的多种应用场景，提供所需的高效向量检索能力。");
                    add("容器计算服务（简称 ACS）是以 K8s 为使用界面供给容器算力资源的云计算服务，提供符合容器规范的算力资源，支持资源预定和按量付费的灵活付费模式，以及 Serverless 形态的算力交付模式，用户无需关注底层节点及集群的运维管理。ACS 算力资源可支持用户的容器应用负载，还能支持阿里云云产品的负载。");
                    add("孔雀翎是由阿里健康科技有限公司研发的ERP系统，是阿里巴巴集团旗下一款专业的企业智能软件产品。");
                }};
                results.put("documents",documents);
                this.results = results;
            }
        };
        userProxy.setRetrieveConfig(retrieveConfig);

        userProxy.setMaxConsecutiveAutoReply(5);
        userProxy.initiateChat(assistant, true, false, "什么是孔雀翎", new HashMap<String, Object>() {{
            put("nResults", 5);
            put("searchString", "测试");
        }});
    }
}
