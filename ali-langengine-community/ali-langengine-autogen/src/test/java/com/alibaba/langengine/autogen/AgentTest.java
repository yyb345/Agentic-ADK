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
package com.alibaba.langengine.autogen;

import com.alibaba.langengine.autogen.agentchat.AssistantAgent;
import com.alibaba.langengine.autogen.agentchat.UserProxyAgent;
import com.alibaba.langengine.autogen.support.ExecuteCommandResult;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import org.junit.jupiter.api.Test;

public class AgentTest {

    @Test
    public void test_demo1() {
        ChatOpenAI llm = new ChatOpenAI();
        AssistantAgent assistant = new AssistantAgent("assistant", llm);
        UserProxyAgent userProxy = new UserProxyAgent("user_proxy", llm);
        userProxy.initiateChat(assistant, "1加1等于多少？");
    }

    @Test
    public void test_run_code() {
//        String[] cmd = {
//                "sh",
//                "/Users/xiaoxuan.lp/works/sources/xiaoxuan.lp/ali-langengine/coding/tmp_code_35eb386bd255bfb6ddf882873e9a45de.sh",
//        };
//        String output = CodeUtils.executeCommand(cmd);
//        System.out.println(output);
    }

    @Test
    public void test_run_code_1() {
//        String[] cmd = {
//                "python3",
//                "/Users/xiaoxuan.lp/works/sources/xiaoxuan.lp/ali-langengine/coding/tmp_code_d767079d043195fd852a3099a23af386.py",
//        };
        String[] cmd = {
                "python3",
                "/Users/xiaoxuan.lp/works/sources/xiaoxuan.lp/ali-langengine/coding/tmp_code_0af5239a5ae14435dd6aa825f901ee44.py"
        };
        ExecuteCommandResult executeCommandResult = CodeUtils.executeCommand(cmd);
        System.out.println(executeCommandResult);
    }
}
