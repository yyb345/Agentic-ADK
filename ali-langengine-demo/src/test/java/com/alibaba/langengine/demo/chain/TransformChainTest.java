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
package com.alibaba.langengine.demo.chain;

import com.alibaba.langengine.core.chain.Chain;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.chain.TransformChain;
import com.alibaba.langengine.core.chain.sequential.SimpleSequentialChain;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * 概念介绍：自定义链转换输出链。
 * 适用场景：个性化链实现，例如通过中心化管理Chain来定义链的执行。
 *
 * @author xiaoxuan.lp
 */
public class TransformChainTest {

    @Test
    public void test_run() {
        // success
        String information = "淘宝开放平台（Taobao Open Platform，简称TOP）是阿里与外部生态互联互通的重要开放途径，通过开放的产品技术把阿里经济体一系列基础服务，像水、电、煤一样输送给我们的商家、开发者、社区媒体以及其他合作伙伴，推动行业的定制、创新、进化, 并最终促成新商业文明生态圈。\n" +
                "\n" +
                "我们是一支技术能力雄厚，有着光荣历史传统的技术团队。在历年双十一战场上，团队都表现着优异的成绩。这里承载着每秒百万级的业务处理，90%的订单通过订单推送服务实时地推送到商家的ERP系统完成电商作业，通过奇门开放的ERP-WMS场景已经成为仓储行业标准。在过去十年中，基于TOP孵化了淘宝服务市场，聚石塔，千牛等一系列引流行业的技术产品。随着新零售业务的持续探索与快速发展，我们渴求各路高手加入，参与核心系统架构设计、性能调优，开放模式创新等富有技术挑战的工作。\n" +
                "\n" +
                "开放平台当前支持集团天猫、淘宝、阿里妈妈、飞猪、ICBU、AE、零售通、盒马、Lazada、钉钉、云OS、智慧园区、虾米、阿里通信等集团绝大多数业务的能力和数据开放(当前集团仅1688，菜鸟CP、阿里云自建开放平台，其他业务全部使用开放平台进行阿里能力开放)，当前支持的业务标签(业务线)共175个，开放API数量10000+，注册开发者240万+，日均API调用量约190亿次，双11当天调用量340亿+，峰值70万QPS。\n" +
                "\n" +
                "开放平台支持的开放模式包括五类：\n" +
                "1. 官方开放（开放API最多，最常见的开放）：服务的提供方是内部各业务方，调用方为外部和内部。\n" +
                "2. 官方集成（ISV按照官方标准与阿里进行系统对接，如淘宝游戏充值，盒马会员通，全渠道会员通）：服务提供方是ISV，标准由平台定义，ISV只需要负责实现。\n" +
                "3. 三方互通（三方ISV与三方ISV通过平台进行信息互通，代表作是奇门仓储业务）：调用和实现方都属于ISV，平台制定标准，ISV需要管理路由，主要降低ISV多对多的对接效率问题。\n" +
                "4. 三方开放（云网关，让ISV具备向其他ISV开放接口的能力）： 平台将鉴权，流控等能力开放给外部isv。\n" +
                "5. 消息服务：通过消息的方式，将信息通知给ISV，只允许同步状态变更数据，比如：订单状态变更消息等。\n" +
                "\n" +
                "\n" +
                "开放平台主要为集团各个BU及业务线提供平台技术能力和平台业务流程支持：\n" +
                "\n" +
                "1、开放平台技术能力输出：开放平台提供API发布模块、API网关，消息网关，数据推送，奇门三方互通、主流语言SDK、集团公认用户授权，集团公认的APP账号体系、鉴权模块、流控模块、计费模块、文档模块等，帮助集团各二方通过开放平台快速发布API，实现API调用认证授权，提供API测试工具、实施API调用过程中的安全管控及流控机制、帮助业务方进行API调用量统计以及计量计费等、同时业务方可以在开放平台上快速生成各类开发者文档及API文档。\n" +
                "\n" +
                "2、开放平台流程能力输出：开放平台提供了完整的API工作流审批机制，支持API生产、API发布、API权限包授权、API流控、API线下等对API全生命周期的管理，并将集团安全、法务、内控等多个部门全部纳入到API的全生命周期管控流程中，协助集团各业务方及各个职能团队都能参与到API开放的管理中。\n" +
                "\n" +
                "开放平台的主要职责是为集团各业务方提供基础开放技术能力及API管理流程支持，并不干涉各业务方的业务决策，例如各业务方需要开放什么API，需要开放给哪些合作伙伴，这些是由各业务方根据发展决策来定的，开放平台提供底层技术，流程能力，业务开放指导支持。但是处于集团数据安全考虑，在开放过程中，平台会联合安全部一起对开放的业务进行审核。";

        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setTopP(0d);
        llm.setMaxTokens(2048);

        TransformChain transformChain = new TransformChain();
        transformChain.setInputVariables(Arrays.asList(new String[]{ "text" }));
        transformChain.setOutputVariables(Arrays.asList(new String[]{ "output_text" }));
        transformChain.setTransform(inputs -> {
            String s = inputs.get("text").toString();
            Map<String, Object> outputs = new HashMap<>();
            outputs.put("output_text", s);
            return outputs;
        });

        String template = "总结一下这段文字：\n" +
                "'''\n" +
                "{output_text}\n" +
                "'''\n" +
                "概括：";
        PromptTemplate promptTemplate = new PromptTemplate();
        promptTemplate.setTemplate(template);
        promptTemplate.setInputVariables(Arrays.asList(new String[]{ "output_text" }));

        LLMChain llmChain = new LLMChain();
        llmChain.setLlm(llm);
        llmChain.setPrompt(promptTemplate);

        SimpleSequentialChain overallChain = new SimpleSequentialChain();
        List<Chain> chains = new ArrayList<>();
        chains.add(transformChain);
        chains.add(llmChain);
        overallChain.setChains(chains);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", information);
        Map<String, Object> response = overallChain.run(inputs);
        System.out.println("output:" + response.get("output"));
    }
}
