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
package com.alibaba.langengine.metagpt;

import com.alibaba.langengine.metagpt.actions.WriteDesign;
import com.alibaba.langengine.metagpt.actions.WriteTasks;
import com.alibaba.langengine.metagpt.roles.Engineer;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TestEngineer {

    private static ChatOpenAI llm = new ChatOpenAI();
    static {
        llm.setModel(OpenAIModelConstants.GPT_4);
    }

    @Test
    public void testWriteCode(){
        // success
        Environment environment = new Environment();
        Engineer engineer = new Engineer(llm);
        engineer.getRc().setEnv(environment);

        Message messageDesign = new Message();
        messageDesign.setCauseBy(WriteDesign.class);
        Map<String, Object> deMsgIns = new HashMap<>();
        deMsgIns.put(WriteDesign.PYTHON_PACKAGE_NAME,"hello_world_app");
        messageDesign.setInstructContent(deMsgIns);
        messageDesign.setContent("```json\n" +
                "{\n" +
                "    \"Implementation approach\": \"We will use the Flask web framework to create a web application that outputs 'Hello World' in the console. Flask is a lightweight and easy-to-use web framework that is perfect for this simple requirement. It is also well-documented and widely used, making it a great choice for this project.\",\n" +
                "    \"Python package name\": \"hello_world_app\",\n" +
                "    \"File list\": [\"app.py\"],\n" +
                "    \"Data structures and interface definitions\": '\n" +
                "    classDiagram\n" +
                "        class HelloWorldApp{\n" +
                "            +init()\n" +
                "            +output_hello_world()\n" +
                "        }\n" +
                "    ',\n" +
                "    \"Program call flow\": '\n" +
                "    sequenceDiagram\n" +
                "        participant User\n" +
                "        User->>HelloWorldApp: start app\n" +
                "        HelloWorldApp->>HelloWorldApp: init\n" +
                "        HelloWorldApp->>console: output 'Hello World'\n" +
                "    ',\n" +
                "    \"Anything UNCLEAR\": \"The requirements are clear to me. However, if there are any changes or additions to the requirements, I will need to adjust the implementation approach and make any necessary modifications to the code.\"\n" +
                "}\n" +
                "```");

        Message messageTask = new Message();
        messageTask.setCauseBy(WriteTasks.class);
        Map<String, Object> deMsgTask = new HashMap<>();
        deMsgTask.put(WriteTasks.TASK_LIST,"[ \"app.py\"]");
        messageTask.setInstructContent(deMsgTask);
        messageTask.setContent("```json\n" +
                "{\n" +
                "    \"Required Python third-party packages\": [\n" +
                "        \"flask==1.1.2\",\n" +
                "        \"bcrypt==3.2.0\"\n" +
                "    ],\n" +
                "    \"Required Other language third-party packages\": [\n" +
                "        \"No third-party packages required\"\n" +
                "    ],\n" +
                "    \"Full API spec\": \"\"\"\n" +
                "        openapi: 3.0.0\n" +
                "        info:\n" +
                "          title: HelloWorld Flask App\n" +
                "          version: 1.0.0\n" +
                "        paths:\n" +
                "          /:\n" +
                "            get:\n" +
                "              responses:\n" +
                "                '200':\n" +
                "                  description: Returns \"Hello World\" in the response body\n" +
                "                  content:\n" +
                "                    application/json:\n" +
                "                      schema:\n" +
                "                        type: string\n" +
                "                        example: \"Hello World\"\n" +
                "    \"\"\",\n" +
                "    \"Logic Analysis\": [\n" +
                "        [\"app.py\", \"init\"],\n" +
                "        [\"app.py\", \"output_hello_world\"]\n" +
                "    ],\n" +
                "    \"Task list\": [\n" +
                "        \"app.py\"\n" +
                "    ],\n" +
                "    \"Shared Knowledge\": \"\"\"\n" +
                "        The `HelloWorldApp` class in `app.py` is responsible for initializing the Flask application and outputting the \"Hello World\" message.\n" +
                "    \"\"\",\n" +
                "    \"Anything UNCLEAR\": \"Is there a specific format for the output message? Should the Flask application run on a specific port?\"\n" +
                "}```");


        engineer.getRc().getEnv().getMemory().add(messageDesign);
        engineer.getRc().getEnv().getMemory().add(messageTask);
        engineer.run();
    }
}
