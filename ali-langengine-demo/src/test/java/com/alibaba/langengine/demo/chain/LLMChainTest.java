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

import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 这是一个简单的链，由PromptTemplate和LLM组成，它使用提供的输入键值格式化提示模板，将格式化的字符串传递给LLM，并返回LLM的输出
 *
 * @author xiaoxuan.lp
 */
public class LLMChainTest {

    @Test
    public void test_run() {
        // success
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(1024);

        PromptTemplate prompt = new PromptTemplate();
        prompt.setTemplate("已知信息：\n" +
                "{context}" +
                "\n" +
                "根据上述已知信息，简洁和专业的来回答用户的问题，请选择最匹配的一条信息。如果无法从中得到答案，请说 " +
                "“根据已知信息无法回答该问题” 或 “没有提供足够的相关信息”，不允许在答案中添加编造成分，答案请使用中文。 问题是：\n" +
                "{input}");

        LLMChain chain = new LLMChain();
        chain.setLlm(llm);
        chain.setPrompt(prompt);

        Map<String, Object> inputs = new HashMap<>();
        //知识库上下文
        inputs.put("context",
                "1、淘宝开放平台创建应用，可以参考：https://open.taobao.com 。\n" +
                "2、top接口传数据是有大小限制，API调用请求最大报文限制在10M以内。\n");
        //prompt
        inputs.put("input", "top接口请求有大小限制吗？");
        Map<String, Object> response = chain.run(inputs);
        System.out.println(response.get("text"));
    }
}
