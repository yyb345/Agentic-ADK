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
package com.alibaba.langengine.moonshot.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.messages.SystemMessage;
import com.alibaba.langengine.core.prompt.ChatPromptValue;
import com.alibaba.langengine.core.runnables.RunnableOutput;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class MoonshotLLMTest {

    @Test
    public void test_run() {
        // success
        MoonshotLLM llm = new MoonshotLLM();
        llm.setMaxTokens(1024);
        long start = System.currentTimeMillis();
        System.out.println("response:" + llm.predict("登鹳雀楼->王之涣\n夜雨寄北->"));
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }

    @Test
    public void test_predict() {
        // success
        MoonshotLLM llm = new MoonshotLLM();
        llm.setMaxTokens(1024);
        long start = System.currentTimeMillis();
//        System.out.println("response:" + llm.predict("你是谁？"));
        String query = "请你扮演下淘宝商家客服的角色，帮助用户收集售后遇到的问题。\n\nuser：你是谁？";

        String prompt = "Below is an instruction that describes a task. Write a response that appropriately completes the request.\n";
        prompt += String.format("### Instruction:\n%s\n\n### Response:\n", query);

        System.out.println("response:" + llm.predict(prompt));
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }

    @Test
    public void test_predict_streamTrue() {
        // success
        MoonshotLLM llm = new MoonshotLLM();
        llm.setStream(true);
        long start = System.currentTimeMillis();
//        System.out.println("response:" + llm.predict("你是谁？"));
        String query = "请你扮演下淘宝商家客服的角色，帮助用户收集售后遇到的问题。\n\nuser：你是谁？";

        String prompt = "Below is an instruction that describes a task. Write a response that appropriately completes the request.\n";
        prompt += String.format("### Instruction:\n%s\n\n### Response:\n", query);
        System.out.println("response:" + llm.predict(prompt,null,t-> System.out.println("==" + t)));
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }

    @Test
    public void test_predict_runnable_model() {
        // success
        MoonshotLLM llm = new MoonshotLLM();
//        llm.setStream(true);
        long start = System.currentTimeMillis();
//        System.out.println("response:" + llm.predict("你是谁？"));
        String query = "请你扮演下淘宝商家客服的角色，帮助用户收集售后遇到的问题。\n\nuser：你是谁？";

        String prompt = "Below is an instruction that describes a task. Write a response that appropriately completes the request.\n";
        prompt += String.format("### Instruction:\n%s\n\n### Response:\n", query);
        ChatPromptValue input = new ChatPromptValue();
        BaseMessage msg = new SystemMessage();
        msg.setContent(prompt);
        input.setMessages(Arrays.asList(msg));
        RunnableOutput out = llm.invoke(input);
        System.out.println("response: "+JSON.toJSONString(out));
        System.out.println((System.currentTimeMillis() - start) + "ms");

        msg = new HumanMessage();
        msg.setContent("有优惠吗？");
        input.setMessages(Arrays.asList(msg));
        out = llm.invoke(input);
        System.out.println("response: "+JSON.toJSONString(out));
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }
}
