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
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.prompt.PromptConverter;
import com.alibaba.langengine.metagpt.Message;
import com.alibaba.langengine.metagpt.utils.CodeParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class WriteTasks extends Action {
    public static final String TASK_LIST = "Task list";

    private static final String PROMPT_TEMPLATE = "# Context\n" +
            "{context}\n" +
            "\n" +
            "## Format example\n" +
            "{format_example}\n" +
            "-----\n" +
            "Role: You are a project manager; the goal is to break down tasks according to PRD/technical design, give a task list, and analyze task dependencies to start with the prerequisite modules\n" +
            "Requirements: Based on the context, fill in the following missing information, each section name is a key in json. Here the granularity of the task is a file, if there are any missing files, you can supplement them\n" +
            "Attention: Use '##' to split sections, not '#', and '## <SECTION_NAME>' SHOULD WRITE BEFORE the code and triple quote.\n" +
            "\n" +
            "## Required Python third-party packages: Provided in requirements.txt format\n" +
            "\n" +
            "## Required Other language third-party packages: Provided in requirements.txt format\n" +
            "\n" +
            "## Full API spec: Use OpenAPI 3.0. Describe all APIs that may be used by both frontend and backend.\n" +
            "\n" +
            "## Logic Analysis: Provided as a Python list[list[str]. the first is filename, the second is class/method/function should be implemented in this file. Analyze the dependencies between the files, which work should be done first\n" +
            "\n" +
            "## Task list: Provided as Python list[str]. Each str is a filename, the more at the beginning, the more it is a prerequisite dependency, should be done first\n" +
            "\n" +
            "## Shared Knowledge: Anything that should be public like utils' functions, config's variables details that should make clear first. \n" +
            "\n" +
            "## Anything UNCLEAR: Provide as Plain text. Make clear here. For example, don't forget a main entry. don't forget to init 3rd party libs.\n" +
            "\n" +
            "output a properly formatted JSON, wrapped inside [CONTENT][/CONTENT] like format example,\n" +
            "and only output the json inside this tag, nothing else\n" +
            "\"\"\",\n" +
            "        \"FORMAT_EXAMPLE\": '''\n" +
            "{\n" +
            "    \"Required Python third-party packages\": [\n" +
            "        \"flask==1.1.2\",\n" +
            "        \"bcrypt==3.2.0\"\n" +
            "    ],\n" +
            "    \"Required Other language third-party packages\": [\n" +
            "        \"No third-party ...\"\n" +
            "    ],\n" +
            "    \"Full API spec\": \"\"\"\n" +
            "        openapi: 3.0.0\n" +
            "        ...\n" +
            "        description: A JSON object ...\n" +
            "     \"\"\",\n" +
            "    \"Logic Analysis\": [\n" +
            "        [\"game.py\",\"Contains...\"]\n" +
            "    ],\n" +
            "    \"Task list\": [\n" +
            "        \"game.py\"\n" +
            "    ],\n" +
            "    \"Shared Knowledge\": \"\"\"\n" +
            "        'game.py' contains ...\n" +
            "    \"\"\",\n" +
            "    \"Anything UNCLEAR\": \"We need ... how to start.\"\n" +
            "}\n" +
            "''',\n" +
            "    },\n" +
            "    \"markdown\": {\n" +
            "        \"PROMPT_TEMPLATE\": \"\"\"\n" +
            "# Context\n" +
            "{context}\n" +
            "\n" +
            "## Format example\n" +
            "{format_example}\n" +
            "-----\n" +
            "Role: You are a project manager; the goal is to break down tasks according to PRD/technical design, give a task list, and analyze task dependencies to start with the prerequisite modules\n" +
            "Requirements: Based on the context, fill in the following missing information, note that all sections are returned in Python code triple quote form seperatedly. Here the granularity of the task is a file, if there are any missing files, you can supplement them\n" +
            "Attention: Use '##' to split sections, not '#', and '## <SECTION_NAME>' SHOULD WRITE BEFORE the code and triple quote.\n" +
            "\n" +
            "## Required Python third-party packages: Provided in requirements.txt format\n" +
            "\n" +
            "## Required Other language third-party packages: Provided in requirements.txt format\n" +
            "\n" +
            "## Full API spec: Use OpenAPI 3.0. Describe all APIs that may be used by both frontend and backend.\n" +
            "\n" +
            "## Logic Analysis: Provided as a Python list[list[str]. the first is filename, the second is class/method/function should be implemented in this file. Analyze the dependencies between the files, which work should be done first\n" +
            "\n" +
            "## Task list: Provided as Python list[str]. Each str is a filename, the more at the beginning, the more it is a prerequisite dependency, should be done first\n" +
            "\n" +
            "## Shared Knowledge: Anything that should be public like utils' functions, config's variables details that should make clear first. \n" +
            "\n" +
            "## Anything UNCLEAR: Provide as Plain text. Make clear here. For example, don't forget a main entry. don't forget to init 3rd party libs.\n" +
            "\n" +
            "\"\"\",\n" +
            "        \"FORMAT_EXAMPLE\": '''\n" +
            "---\n" +
            "## Required Python third-party packages\n" +
            "```python\n" +
            "\"\"\"\n" +
            "flask==1.1.2\n" +
            "bcrypt==3.2.0\n" +
            "\"\"\"\n" +
            "```\n" +
            "\n" +
            "## Required Other language third-party packages\n" +
            "```python\n" +
            "\"\"\"\n" +
            "No third-party ...\n" +
            "\"\"\"\n" +
            "```\n" +
            "\n" +
            "## Full API spec\n" +
            "```python\n" +
            "\"\"\"\n" +
            "openapi: 3.0.0\n" +
            "...\n" +
            "description: A JSON object ...\n" +
            "\"\"\"\n" +
            "```\n" +
            "\n" +
            "## Logic Analysis\n" +
            "```python\n" +
            "[\n" +
            "    [\"game.py\", \"Contains ...\"],\n" +
            "]\n" +
            "```\n" +
            "\n" +
            "## Task list\n" +
            "```python\n" +
            "[\n" +
            "    \"game.py\",\n" +
            "]\n" +
            "```\n" +
            "\n" +
            "## Shared Knowledge\n" +
            "```python\n" +
            "\"\"\"\n" +
            "'game.py' contains ...\n" +
            "\"\"\"\n" +
            "```\n" +
            "\n" +
            "## Anything UNCLEAR\n" +
            "We need ... how to start.\n" +
            "---";

    private static final String PROMPT_JSON ="# Context\n" +
            "{context}\n" +
            "\n" +
            "## Format example\n" +
            "{format_example}\n" +
            "-----\n" +
            "Role: You are a project manager; the goal is to break down tasks according to PRD/technical design, give a task list, and analyze task dependencies to start with the prerequisite modules\n" +
            "Requirements: Based on the context, fill in the following missing information, each section name is a key in json. Here the granularity of the task is a file, if there are any missing files, you can supplement them\n" +
            "Attention: Use '##' to split sections, not '#', and '## <SECTION_NAME>' SHOULD WRITE BEFORE the code and triple quote.\n" +
            "\n" +
            "## Required Python third-party packages: Provided in requirements.txt format\n" +
            "\n" +
            "## Required Other language third-party packages: Provided in requirements.txt format\n" +
            "\n" +
            "## Full API spec: Use OpenAPI 3.0. Describe all APIs that may be used by both frontend and backend.\n" +
            "\n" +
            "## Logic Analysis: Provided as a Python list[list[str]. the first is filename, the second is class/method/function should be implemented in this file. Analyze the dependencies between the files, which work should be done first\n" +
            "\n" +
            "## Task list: Provided as Python list[str]. Each str is a filename, the more at the beginning, the more it is a prerequisite dependency, should be done first\n" +
            "\n" +
            "## Shared Knowledge: Anything that should be public like utils' functions, config's variables details that should make clear first. \n" +
            "\n" +
            "## Anything UNCLEAR: Provide as Plain text. Make clear here. For example, don't forget a main entry. don't forget to init 3rd party libs.\n" +
            "\n" +
            "output a properly formatted JSON, wrapped inside [CONTENT][/CONTENT] like Format example,\n" +
            "and only output the json inside this tag, nothing else";
    private static final String FORMAT_EXAMPLE ="[CONTENT]\n"+
            "{\n" +
            "    \"Required Python third-party packages\": [\n" +
            "        \"flask==1.1.2\",\n" +
            "        \"bcrypt==3.2.0\"\n" +
            "    ],\n" +
            "    \"Required Other language third-party packages\": [\n" +
            "        \"No third-party ...\"\n" +
            "    ],\n" +
            "    \"Full API spec\": \"\n" +
            "        openapi: 3.0.0\n" +
            "        ...\n" +
            "        description: A JSON object ...\n" +
            "     \",\n" +
            "    \"Logic Analysis\": [\n" +
            "        [\"game.py\",\"Contains...\"]\n" +
            "    ],\n" +
            "    \"Task list\": [\n" +
            "        \"game.py\"\n" +
            "    ],\n" +
            "    \"Shared Knowledge\": \"\n" +
            "        'game.py' contains ...\n" +
            "    \",\n" +
            "    \"Anything UNCLEAR\": \"We need ... how to start.\"\n" +
            "}\n"+
            "[/CONTENT]";

    public WriteTasks() {
        super("", null, null);
    }

//    public WriteTasks(String name, Object context, BaseLanguageModel llm) {
//        super(name, context, llm);
//    }

    @Override
    public ActionOutput run(List<Message> messages) {
        log.warn("WriteTasks:" + JSON.toJSONString(messages));
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("context", messages.get(0).getContent());
        inputs.put("format_example", FORMAT_EXAMPLE);
        String prompt = PromptConverter.replacePrompt(PROMPT_JSON, inputs);
        log.info("WriteTasks,prompt :{}",prompt);

        String taskRsp = getCache().readeCache("WriteTasks", "task.txt");
        if (StringUtils.isEmpty(taskRsp)) {
            taskRsp = getLlm().predict(prompt);
            getCache().writeCache("WriteTasks", "task.txt", taskRsp);
        }

        log.warn("WriteTasks,taskRsp:" + taskRsp);

        ActionOutput actionOutput = new ActionOutput();
        actionOutput.setContent(taskRsp);
        actionOutput.setInstructContent(parseInstructContent(taskRsp));
        return actionOutput;
    }
    public static Map<String, Object> parseInstructContent(String taskRsp){
        String jsonStr = CodeParser.parseLangCode(taskRsp,"json");
        jsonStr = CodeParser.parseBlockCode(jsonStr,"CONTENT");
        jsonStr = jsonStr.replace("[CONTENT]","");
        JSONObject jsonObj = JSON.parseObject(jsonStr);
        Map<String, Object> rslt = new HashMap<>();
        rslt.put(TASK_LIST,jsonObj.getString(TASK_LIST));
        return rslt;
    }

}
