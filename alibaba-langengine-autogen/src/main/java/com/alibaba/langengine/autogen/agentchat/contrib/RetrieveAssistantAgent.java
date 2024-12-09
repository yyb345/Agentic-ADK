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
package com.alibaba.langengine.autogen.agentchat.contrib;

import com.alibaba.langengine.autogen.Agent;
import com.alibaba.langengine.autogen.agentchat.AssistantAgent;
import com.alibaba.langengine.autogen.agentchat.support.ReplyResult;
import com.alibaba.langengine.autogen.tools.CheckUpdateContext;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Retrieve Assistant agent, designed to solve a task with LLM.
 *
 *     RetrieveAssistantAgent is a subclass of AssistantAgent configured with a default system message.
 *     The default system message is designed to solve a task with LLM,
 *     including suggesting python code blocks and debugging.
 *     `human_input_mode` is default to "NEVER"
 *     and `code_execution_config` is default to False.
 *     This agent doesn't execute code by default, and expects the user to execute the code.
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class RetrieveAssistantAgent extends AssistantAgent {

    public RetrieveAssistantAgent(String name, BaseLanguageModel llm) {
        super(name, llm);
    }

    @Override
    public Object generateReply(List<Map<String, Object>> messages, Agent sender) {
        if (messages == null && sender == null) {
            String errorMsg = "Either messages or sender must be provided.";
            log.error(errorMsg);
            throw new AssertionError(errorMsg);
        }

        if (messages == null) {
            messages = getOaiMessages().get(sender);
        }

        ReplyResult replyResult = generateRetrieveAssistantReply(messages, sender);
        if(replyResult.isFinalFlag()) {
            return replyResult.getReply() != null ? replyResult.getReply() : replyResult.getOutput();
        }

        return super.generateReply(messages, sender);
    }

    private ReplyResult generateRetrieveAssistantReply(List<Map<String, Object>> messages, Agent sender) {
        if (messages == null) {
            messages = getOaiMessages().get(sender);
        }
        Map<String, Object> message = messages.get(messages.size() - 1);
        String content = (String) message.get("content");

        if (content.contains("exitcode: 0 (execution succeeded)")) {
            return new ReplyResult(true, "TERMINATE");
        } else if (CheckUpdateContext.isUpdateContext(content)) {
            return new ReplyResult(true, "UPDATE CONTEXT");
        } else {
            return new ReplyResult(false, null);
        }
    }


}
