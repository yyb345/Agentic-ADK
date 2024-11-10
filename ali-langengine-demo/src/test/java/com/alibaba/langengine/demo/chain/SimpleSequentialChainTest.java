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

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.chain.Chain;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.chain.sequential.SimpleSequentialChain;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * 概念介绍：简单的链，其中一个步骤的输出直接输入到下一步。通过一个Chain列表，去循环遍历Chain，上一个的输出变成下一个的输入作为特点，进行顺序的LLM执行。
 * 适用场景：顺序逻辑一步步推导的调用场景，根据问题和目标，完成问题不断拆解和解答。
 *
 * @author xiaoxuan.lp
 */
public class SimpleSequentialChainTest {

    @Test
    public void test_openai_run() {
        // success
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(1024);

//        String template1 = "You are a playwright. Given the title of play, it is your job to write a synopsis for that title.\n" +
//                "\n" +
//                "Title: {title}\n" +
//                "Playwright: This is a synopsis for the above play:";
        String template1 = "你是一位剧作家。 给定游戏的标题，你的工作就是为该标题写一个概要。\n" +
                "\n" +
                "作品名称：{title}\n" +
                "编剧：这是上述戏剧的剧情简介：";
        PromptTemplate promptTemplate1 = new PromptTemplate();
        promptTemplate1.setTemplate(template1);
        promptTemplate1.setInputVariables(Arrays.asList(new String[]{ "title" }));

        LLMChain synopsisChain = new LLMChain();
        synopsisChain.setLlm(llm);
        synopsisChain.setPrompt(promptTemplate1);

//        String template2 = "You are a play critic from the New York Times. Given the synopsis of play, it is your job to write a review for that play.\n" +
//                "\n" +
//                "Play Synopsis:\n" +
//                "{synopsis}\n" +
//                "Review from a New York Times play critic of the above play:";
        String template2 = "您是《纽约时报》的戏剧评论家。 鉴于戏剧的概要，您的工作就是为该戏剧撰写评论。\n" +
                "\n" +
                "播放剧情简介：\n" +
                "{synopsis}\n" +
                "《纽约时报》剧评人对上述剧作的评价：";
        PromptTemplate promptTemplate2 = new PromptTemplate();
        promptTemplate2.setTemplate(template2);
        promptTemplate2.setInputVariables(Arrays.asList(new String[]{ "synopsis" }));

        LLMChain reviewChain = new LLMChain();
        reviewChain.setLlm(llm);
        reviewChain.setPrompt(promptTemplate2);

        SimpleSequentialChain overallChain = new SimpleSequentialChain();
        List<Chain> chains = new ArrayList<>();
        chains.add(synopsisChain);
        chains.add(reviewChain);
        overallChain.setChains(chains);

        Map<String, Object> inputs = new HashMap<>();
//        inputs.put("input", "Tragedy at sunset on the beach");
        inputs.put("input", "海滩日落悲剧");
        Map<String, Object> response = overallChain.run(inputs);
        System.out.println("response:" + JSON.toJSONString(response));
    }
}
