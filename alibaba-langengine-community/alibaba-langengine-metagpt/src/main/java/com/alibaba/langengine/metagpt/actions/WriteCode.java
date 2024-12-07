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
package com.alibaba.langengine.metagpt.actions;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.prompt.PromptConverter;
import com.alibaba.langengine.metagpt.Message;
import com.alibaba.langengine.metagpt.utils.CodeParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class WriteCode extends Action {

    private static final String PROMPT_TEMPLATE = "NOTICE\n" +
            "Role: You are a professional engineer; the main goal is to write PEP8 compliant, elegant, modular, easy to read and maintain Python 3.9 code (but you can also use other programming language)\n" +
            "ATTENTION: Use '##' to SPLIT SECTIONS, not '#'. Output format carefully referenced \"Format example\".\n" +
            "\n" +
            "## Code: {filename} Write code with triple quoto, based on the following list and context.\n" +
            "1. Do your best to implement THIS ONLY ONE FILE. ONLY USE EXISTING API. IF NO API, IMPLEMENT IT.\n" +
            "2. Requirement: Based on the context, implement one following code file, note to return only in code form, your code will be part of the entire project, so please implement complete, reliable, reusable code snippets\n" +
            "3. Attention1: If there is any setting, ALWAYS SET A DEFAULT VALUE, ALWAYS USE STRONG TYPE AND EXPLICIT VARIABLE.\n" +
            "4. Attention2: YOU MUST FOLLOW \"Data structures and interface definitions\". DONT CHANGE ANY DESIGN.\n" +
            "5. Think before writing: What should be implemented and provided in this document?\n" +
            "6. CAREFULLY CHECK THAT YOU DONT MISS ANY NECESSARY CLASS/FUNCTION IN THIS FILE.\n" +
            "7. Do not use public member functions that do not exist in your design.\n" +
            "\n" +
            "-----\n" +
            "# Context\n" +
            "{context}\n" +
            "-----\n" +
            "## Format example\n" +
            "-----\n" +
            "## Code: {filename}\n" +
            "```python\n" +
            "## {filename}\n" +
            "...\n" +
            "```\n" +

            "-----";

    public WriteCode() {
        this("", null, null);
    }

    public WriteCode(String name, Object context, BaseLanguageModel llm) {
        super(name, context, llm);
    }

    public String run(String context, String filename) {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("context", context);
        inputs.put("filename", filename);

        String prompt = PromptConverter.replacePrompt(PROMPT_TEMPLATE, inputs);
        log.info("WriteCode, filename {} prompt:{}",filename,prompt);

        String codeRsp = getCache().readeCache("WriteCode", filename);
        if (StringUtils.isEmpty(codeRsp)) {
            codeRsp = getLlm().predict(prompt);
            getCache().writeCache("WriteCode", filename, codeRsp);
        }
        String code = CodeParser.parseLangCode(codeRsp,"python");
        return code;
    }

    @Override
    public ActionOutput run(List<Message> messages) {
        log.warn("WriteCode:" + JSON.toJSONString(messages));
        String pythonPackageName = "";
        List<String> todoList = new ArrayList<>();
        StringBuilder contentSb = new StringBuilder();
        for (Message message : messages) {
            contentSb.append(message.getContent());
            contentSb.append("\n");
            if (message.getCauseBy() == WriteDesign.class) {
                pythonPackageName = String.valueOf(message.getInstructContent().get(WriteDesign.PYTHON_PACKAGE_NAME));
                continue;
            }
            if (message.getCauseBy() == WriteTasks.class) {
                String todos = String.valueOf(message.getInstructContent().get(WriteTasks.TASK_LIST));
                if (!StringUtils.isEmpty(todos)) {
                    todoList = JSONArray.parseArray(todos, String.class);
                }
            }
        }
       List<WriteCodeResult> writeCodeResultList = new ArrayList<>();
        for (String taskName : todoList) {
            String code = run(contentSb.toString(), taskName);
            log.warn("WriteCode,fileName:{},code:{}", taskName, code);
            writeCodeResultList.add(new WriteCodeResult(pythonPackageName,taskName,code));
        }
        ActionOutput actionOutput = new ActionOutput();
        actionOutput.setContent(JSON.toJSONString(writeCodeResultList));
        return actionOutput;
    }
    public class WriteCodeResult{
        public String packageName;
        public String fileName;

        public String code;

        public WriteCodeResult(String packageName, String fileName, String code) {
            this.packageName = packageName;
            this.fileName = fileName;
            this.code = code;
        }
    }
}
