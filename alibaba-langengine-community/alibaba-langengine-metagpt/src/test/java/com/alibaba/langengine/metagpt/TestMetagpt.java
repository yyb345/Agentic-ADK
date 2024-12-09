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

import com.alibaba.langengine.metagpt.actions.WriteCode;
import com.alibaba.langengine.metagpt.actions.WriteDesign;
import com.alibaba.langengine.metagpt.actions.WritePRD;
import com.alibaba.langengine.metagpt.actions.WriteTasks;
import com.alibaba.langengine.metagpt.roles.*;
import com.alibaba.langengine.metagpt.utils.CodeParser;

import com.alibaba.langengine.openai.model.ChatOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import org.junit.jupiter.api.Test;

import java.util.*;

public class TestMetagpt {

    private static ChatOpenAI llm = new ChatOpenAI();
    static {
        llm.setModel(OpenAIModelConstants.GPT_4);
    }

    @Test
    public void testLLM(){
        // success
        String prompt = "NOTICE\n" +
                "Role: You are a professional engineer; the main goal is to write PEP8 compliant, elegant, modular, easy to read and maintain Python 3.9 code (but you can also use other programming language)\n" +
                "ATTENTION: Use '##' to SPLIT SECTIONS, not '#'. Output format carefully referenced \"Format example\".\n" +
                "\n" +
                "## Code: main.py Write code with triple quoto, based on the following list and context.\n" +
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
                "\n" +
                "```json\n" +
                "{\n" +
                "    \"Implementation approach\": \"We will use the Flask framework to create a web application that outputs 'Hello World' in the console. Flask is a lightweight and flexible web framework that is easy to use and has a large community.\",\n" +
                "    \"Python package name\": \"hello_world_app\",\n" +
                "    \"File list\": [\"main.py\", \"app.py\"],\n" +
                "    \"Data structures and interface definitions\": '\n" +
                "    classDiagram\n" +
                "        class HelloWorldApp{\n" +
                "            +__init__()\n" +
                "            +say_hello()\n" +
                "        }\n" +
                "    ',\n" +
                "    \"Program call flow\": '\n" +
                "    sequenceDiagram\n" +
                "        participant User\n" +
                "        User->>HelloWorldApp: visit website\n" +
                "        HelloWorldApp->>HelloWorldApp: say_hello()\n" +
                "        HelloWorldApp->>User: output \"Hello World\"\n" +
                "    ',\n" +
                "    \"Anything UNCLEAR\": \"It is clear to me that the requirement is to output 'Hello World' in the console using Python and Flask. If there are any further questions or concerns, please let me know.\"\n" +
                "}\n" +
                "```\n" +
                "\n" +
                "```json\n" +
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
                "          title: HelloWorld App API\n" +
                "          version: 1.0.0\n" +
                "        paths:\n" +
                "          /:\n" +
                "            get:\n" +
                "              summary: Output \"Hello World\"\n" +
                "              responses:\n" +
                "                '200':\n" +
                "                  description: Output \"Hello World\"\n" +
                "                  content:\n" +
                "                    text/plain:\n" +
                "                      schema:\n" +
                "                        type: string\n" +
                "                        example: Hello World\n" +
                "    \"\"\",\n" +
                "    \"Logic Analysis\": [\n" +
                "        [\"main.py\", \"Contains main logic for Flask application\"],\n" +
                "        [\"app.py\", \"Contains routes and functions for Flask application\"]\n" +
                "    ],\n" +
                "    \"Task list\": [\n" +
                "        \"main.py\",\n" +
                "        \"app.py\"\n" +
                "    ],\n" +
                "    \"Shared Knowledge\": \"\"\"\n" +
                "        \"main.py\" contains the entry point for the Flask application and initializes the Flask app instance.\n" +
                "        \"app.py\" contains the Flask routes and functions that handle incoming HTTP requests.\n" +
                "    \"\"\",\n" +
                "    \"Anything UNCLEAR\": \"Please provide more details on the expected output format for the Flask application. For example, should the output be a JSON object or a plain text message?\"\n" +
                "}\n" +
                "```\n" +
                "-----\n" +
                "## Format example\n" +
                "-----\n" +
                "## Code: main.py\n" +
                "```python\n" +
                "## main.py\n" +
                "...\n" +
                "```\n" +
                "-----";
        String rslt = llm.run(prompt,new ArrayList<>(), null);
        System.out.println(rslt);
    }

    @Test
    public void testSoftwareCompany(){
        // success
        //软件公司
        SoftwareCompany company = new SoftwareCompany();
        //角色设定
        List<Role> roles = new ArrayList<>();

        roles.add(new ProductManager(llm)); //产品经理角色
        roles.add(new Architect(llm)); //架构师
        roles.add(new ProjectManager(llm)); //项目经理
        roles.add(new Engineer(llm)); //开发工程师角色
        //雇佣员工
        company.hire(roles);
        //老板需求设定
        String idea = "实现通过控制台输出'当前时间'";
//        String idea = "实现一个贪吃蛇游戏";
        company.startProject(idea);
        //开始运行
        company.run(3);
    }
    @Test
    public void testProCache(){
        //软件公司
        SoftwareCompany company = new SoftwareCompany();
        //角色设定
        List<Role> roles = new ArrayList<>();

        roles.add(new ProductManager(llm)); //产品经理角色
        roles.add(new Architect(llm)); //架构师
        roles.add(new ProjectManager(llm)); //项目经理
        roles.add(new Engineer(llm)); //开发工程师角色
        //雇佣员工
        company.hire(roles);
        //老板需求设定
        company.startProject("实现一个贪吃蛇游戏","/Users/zhouchangjiang/dev/pycharm/metagpt_workspace/","PS_003");
        //开始运行
        company.run(3);
    }
    @Test
    public void testCodeParser(){
        // success
        String srcCode = "## Code: app.py\n" +
                "```python\n" +
                "from flask import Flask\n" +
                "\n" +
                "class HelloWorldApp:\n" +
                "    def __init__(self):\n" +
                "        self.app = Flask(__name__)\n" +
                "        self.app.config['SECRET_KEY'] = 'secret-key'\n" +
                "\n" +
                "    def output_hello_world(self):\n" +
                "        return 'Hello World'\n" +
                "\n" +
                "if __name__ == '__main__':\n" +
                "    app = HelloWorldApp()\n" +
                "    app.run(debug=True)\n" +
                "```";
       String mapInfo =  CodeParser.parseLangCode(srcCode,"python");

        System.out.println("==="+mapInfo);

    }
    @Test
    public void testWritePRD(){
        WritePRD writePRD = new WritePRD();
        Message message = new Message();
        message.setContent("实现控制台输出Hello Word功能");
        writePRD.run(Arrays.asList(message));
    }
    @Test
    public void testRunWriteCode(){
        WriteCode writeCode = new WriteCode();
        writeCode.setLlm(llm);

        Message messageDesign = new Message();
        messageDesign.setCauseBy(WriteDesign.class);
        Map<String, Object> deMsgIns = new HashMap<>();
        deMsgIns.put("pythonPackageName","hello_world_app");
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
        deMsgTask.put("tasklist","[ \"app.py\"]");
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


        writeCode.run(Arrays.asList(messageDesign,messageTask));
    }


}
