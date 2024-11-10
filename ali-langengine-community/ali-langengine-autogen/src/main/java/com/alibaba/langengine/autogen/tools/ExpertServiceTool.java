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
package com.alibaba.langengine.autogen.tools;

import com.alibaba.langengine.autogen.agentchat.AssistantAgent;
import com.alibaba.langengine.autogen.agentchat.UserProxyAgent;
import com.alibaba.langengine.core.model.FakeAI;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

/**
 * 专家服务工具
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class ExpertServiceTool extends StructuredTool {

    public ExpertServiceTool() {
        setName("ask_expert");
        setDescription("ask expert when you can't solve the problem satisfactorily.");
        setStructuredSchema(new ExpertServiceSchema());
    }

    @Override
    public ToolExecuteResult execute(String message) {
//        log.error("ExpertServiceTool message:" + message);

        FakeAI llm = new FakeAI();

        AssistantAgent assistant_for_expert = new AssistantAgent("assistant_for_expert", llm);

        UserProxyAgent expert = new UserProxyAgent("expert", llm, new HashMap<String, Object>() {{
            put("work_dir", "expert");
        }});
        expert.setHumanInputMode("ALWAYS");

        expert.initiateChat(assistant_for_expert, message);
        expert.stopReplyAtReceive(assistant_for_expert);
//        expert.send("summarize the solution and explain the answer in an easy-to-understand way", assistant_for_expert,
//                null, false);

        String content = (String) expert.getLastMessage(null).get("content");

        return new ToolExecuteResult(content);
    }
}
