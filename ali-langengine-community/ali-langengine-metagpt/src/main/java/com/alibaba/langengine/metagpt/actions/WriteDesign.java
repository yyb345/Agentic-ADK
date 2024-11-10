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
public class WriteDesign extends Action {
    public static final String PYTHON_PACKAGE_NAME = "Python package name";
    public static final String FILE_LIST = "File list";


    private static final String PROMPT = "# Context\n" +
            "{context}\n" +
            "\n" +
            "## Format example\n" +
            "{format_example}\n" +
            "-----\n" +
            "Role: You are an architect; the goal is to design a SOTA PEP8-compliant python system; make the best use of good open source tools\n" +
            "Requirement: Fill in the following missing information based on the context, each section name is a key in json\n" +
            "Max Output: 8192 chars or 2048 tokens. Try to use them up.\n" +
            "\n" +
            "## Implementation approach: Provide as Plain text. Analyze the difficult points of the requirements, select the appropriate open-source framework.\n" +
            "\n" +
            "## Python package name: Provide as Python str with python triple quoto, concise and clear, characters only use a combination of all lowercase and underscores\n" +
            "\n" +
            "## File list: Provided as Python list[str], the list of ONLY REQUIRED files needed to write the program(LESS IS MORE!). Only need relative paths, comply with PEP8 standards. ALWAYS write a main.py or app.py here\n" +
            "\n" +
            "## Data structures and interface definitions: Use mermaid classDiagram code syntax, including classes (INCLUDING __init__ method) and functions (with type annotations), CLEARLY MARK the RELATIONSHIPS between classes, and comply with PEP8 standards. The data structures SHOULD BE VERY DETAILED and the API should be comprehensive with a complete design. \n" +
            "\n" +
            "## Program call flow: Use sequenceDiagram code syntax, COMPLETE and VERY DETAILED, using CLASSES AND API DEFINED ABOVE accurately, covering the CRUD AND INIT of each object, SYNTAX MUST BE CORRECT.\n" +
            "\n" +
            "## Anything UNCLEAR: Provide as Plain text. Make clear here.\n" +
            "\n" +
            "output a properly formatted JSON, wrapped inside [CONTENT][/CONTENT] like format example,\n" +
            "and only output the json inside this tag, nothing else\n" +
            "\"\"\",\n" +
            "        \"FORMAT_EXAMPLE\": \"\"\"\n" +
            "[CONTENT]\n" +
            "{\n" +
            "    \"Implementation approach\": \"We will ...\",\n" +
            "    \"Python package name\": \"snake_game\",\n" +
            "    \"File list\": [\"main.py\"],\n" +
            "    \"Data structures and interface definitions\": '\n" +
            "    classDiagram\n" +
            "        class Game{\n" +
            "            +int score\n" +
            "        }\n" +
            "        ...\n" +
            "        Game \"1\" -- \"1\" Food: has\n" +
            "    ',\n" +
            "    \"Program call flow\": '\n" +
            "    sequenceDiagram\n" +
            "        participant M as Main\n" +
            "        ...\n" +
            "        G->>M: end game\n" +
            "    ',\n" +
            "    \"Anything UNCLEAR\": \"The requirement is clear to me.\"\n" +
            "}\n" +
            "[/CONTENT]\n" +
            "\"\"\",\n" +
            "    },\n" +
            "    \"markdown\": {\n" +
            "        \"PROMPT_TEMPLATE\": \"\"\"\n" +
            "# Context\n" +
            "{context}\n" +
            "\n" +
            "## Format example\n" +
            "{format_example}\n" +
            "-----\n" +
            "Role: You are an architect; the goal is to design a SOTA PEP8-compliant python system; make the best use of good open source tools\n" +
            "Requirement: Fill in the following missing information based on the context, note that all sections are response with code form separately\n" +
            "Max Output: 8192 chars or 2048 tokens. Try to use them up.\n" +
            "Attention: Use '##' to split sections, not '#', and '## <SECTION_NAME>' SHOULD WRITE BEFORE the code and triple quote.\n" +
            "\n" +
            "## Implementation approach: Provide as Plain text. Analyze the difficult points of the requirements, select the appropriate open-source framework.\n" +
            "\n" +
            "## Python package name: Provide as Python str with python triple quoto, concise and clear, characters only use a combination of all lowercase and underscores\n" +
            "\n" +
            "## File list: Provided as Python list[str], the list of ONLY REQUIRED files needed to write the program(LESS IS MORE!). Only need relative paths, comply with PEP8 standards. ALWAYS write a main.py or app.py here\n" +
            "\n" +
            "## Data structures and interface definitions: Use mermaid classDiagram code syntax, including classes (INCLUDING __init__ method) and functions (with type annotations), CLEARLY MARK the RELATIONSHIPS between classes, and comply with PEP8 standards. The data structures SHOULD BE VERY DETAILED and the API should be comprehensive with a complete design. \n" +
            "\n" +
            "## Program call flow: Use sequenceDiagram code syntax, COMPLETE and VERY DETAILED, using CLASSES AND API DEFINED ABOVE accurately, covering the CRUD AND INIT of each object, SYNTAX MUST BE CORRECT.\n" +
            "\n" +
            "## Anything UNCLEAR: Provide as Plain text. Make clear here.\n" +
            "\n" +
            "\"\"\",\n" +
            "        \"FORMAT_EXAMPLE\": \"\"\"\n" +
            "---\n" +
            "## Implementation approach\n" +
            "We will ...\n" +
            "\n" +
            "## Python package name\n" +
            "```python\n" +
            "\"snake_game\"\n" +
            "```\n" +
            "\n" +
            "## File list\n" +
            "```python\n" +
            "[\n" +
            "    \"main.py\",\n" +
            "]\n" +
            "```\n" +
            "\n" +
            "## Data structures and interface definitions\n" +
            "```mermaid\n" +
            "classDiagram\n" +
            "    class Game{\n" +
            "        +int score\n" +
            "    }\n" +
            "    ...\n" +
            "    Game \"1\" -- \"1\" Food: has\n" +
            "```\n" +
            "\n" +
            "## Program call flow\n" +
            "```mermaid\n" +
            "sequenceDiagram\n" +
            "    participant M as Main\n" +
            "    ...\n" +
            "    G->>M: end game\n" +
            "```\n" +
            "\n" +
            "## Anything UNCLEAR\n" +
            "The requirement is clear to me.\n" +
            "---\n";

    private static final String PROMPT_JSON = "# Context\n" +
            "{context}\n" +
            "\n" +
            "## Format example\n" +
            "{format_example}\n" +
            "-----\n" +
            "Role: You are an architect; the goal is to design a SOTA PEP8-compliant python system; make the best use of good open source tools\n" +
            "Requirement: Fill in the following missing information based on the context, each section name is a key in json\n" +
            "Max Output: 8192 chars or 2048 tokens. Try to use them up.\n" +
            "\n" +
            "## Implementation approach: Provide as Plain text. Analyze the difficult points of the requirements, select the appropriate open-source framework.\n" +
            "\n" +
            "## Python package name: Provide as Python str with python triple quoto, concise and clear, characters only use a combination of all lowercase and underscores\n" +
            "\n" +
            "## File list: Provided as Python list[str], the list of ONLY REQUIRED files needed to write the program(LESS IS MORE!). Only need relative paths, comply with PEP8 standards. ALWAYS write a main.py or app.py here\n" +
            "\n" +
            "## Data structures and interface definitions: Use mermaid classDiagram code syntax, including classes (INCLUDING __init__ method) and functions (with type annotations), CLEARLY MARK the RELATIONSHIPS between classes, and comply with PEP8 standards. The data structures SHOULD BE VERY DETAILED and the API should be comprehensive with a complete design. \n" +
            "\n" +
            "## Program call flow: Use sequenceDiagram code syntax, COMPLETE and VERY DETAILED, using CLASSES AND API DEFINED ABOVE accurately, covering the CRUD AND INIT of each object, SYNTAX MUST BE CORRECT.\n" +
            "\n" +
            "## Anything UNCLEAR: Provide as Plain text. Make clear here.\n" +
            "\n" +
            "output a properly formatted JSON, wrapped inside [CONTENT][/CONTENT] like format example,\n" +
            "and only output the json inside this tag, nothing else";

    private static final String FORMAT_EXAMPLE ="[CONTENT]\n" +
            "{\n" +
            "    \"Implementation approach\": \"We will ...\",\n" +
            "    \"Python package name\": \"snake_game\",\n" +
            "    \"File list\": [\"main.py\"],\n" +
            "    \"Data structures and interface definitions\": '\n" +
            "    classDiagram\n" +
            "        class Game{\n" +
            "            +int score\n" +
            "        }\n" +
            "        ...\n" +
            "        Game \"1\" -- \"1\" Food: has\n" +
            "    ',\n" +
            "    \"Program call flow\": '\n" +
            "    sequenceDiagram\n" +
            "        participant M as Main\n" +
            "        ...\n" +
            "        G->>M: end game\n" +
            "    ',\n" +
            "    \"Anything UNCLEAR\": \"The requirement is clear to me.\"\n" +
            "}\n" +
            "[/CONTENT]";


    public WriteDesign() {
        super("", null, null);
    }

//    public WriteDesign(String name, Object context, BaseLanguageModel llm) {
//        super(name, context, llm);
//    }

    @Override
    public ActionOutput run(List<Message> messages) {
        log.warn("WriteDesign:" + JSON.toJSONString(messages));
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("context", messages.get(0).getContent());
        inputs.put("format_example", FORMAT_EXAMPLE);
        String prompt = PromptConverter.replacePrompt(PROMPT_JSON, inputs);
        log.info("WriteDesign,prompt :{}",prompt);

        String designRsp = getCache().readeCache("WriteDesign", "design.txt");
        if (StringUtils.isEmpty(designRsp)) {
            designRsp = getLlm().predict(prompt);
            getCache().writeCache("WriteDesign", "design.txt", designRsp);
        }

        log.warn("WriteDesign,designRsp:" + designRsp);
        ActionOutput actionOutput = new ActionOutput();
        actionOutput.setContent(designRsp);
        actionOutput.setInstructContent(parseInstructContent(designRsp));
        return actionOutput;
    }
    private Map<String, Object> parseInstructContent(String designRsp){
        String jsonStr = CodeParser.parseLangCode(designRsp,"json");
        jsonStr = CodeParser.parseBlockCode(jsonStr,"CONTENT");
        jsonStr = jsonStr.replace("[CONTENT]","");
        JSONObject jsonObj = JSON.parseObject(jsonStr);
        Map<String, Object> rslt = new HashMap<>();
        rslt.put(PYTHON_PACKAGE_NAME,jsonObj.getString(PYTHON_PACKAGE_NAME));
        rslt.put(FILE_LIST,jsonObj.getString(FILE_LIST));
        return rslt;
    }
}
