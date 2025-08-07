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
import com.alibaba.langengine.core.prompt.PromptConverter;
import com.alibaba.langengine.metagpt.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class WritePRD extends Action {

    private static final String JSON_PROMPT_TEMPLATE = "# Context\n" +
            "## Original Requirements\n" +
            "{requirements}\n" +
            "\n" +
            "## Search Information\n" +
            "{search_information}\n" +
            "\n" +
            "## mermaid quadrantChart code syntax example. DONT USE QUOTO IN CODE DUE TO INVALID SYNTAX. Replace the <Campaign X> with REAL COMPETITOR NAME\n" +
            "```mermaid\n" +
            "quadrantChart\n" +
            "    title Reach and engagement of campaigns\n" +
            "    x-axis Low Reach --> High Reach\n" +
            "    y-axis Low Engagement --> High Engagement\n" +
            "    quadrant-1 We should expand\n" +
            "    quadrant-2 Need to promote\n" +
            "    quadrant-3 Re-evaluate\n" +
            "    quadrant-4 May be improved\n" +
            "    \"Campaign: A\": [0.3, 0.6]\n" +
            "    \"Campaign B\": [0.45, 0.23]\n" +
            "    \"Campaign C\": [0.57, 0.69]\n" +
            "    \"Campaign D\": [0.78, 0.34]\n" +
            "    \"Campaign E\": [0.40, 0.34]\n" +
            "    \"Campaign F\": [0.35, 0.78]\n" +
            "    \"Our Target Product\": [0.5, 0.6]\n" +
            "```\n" +
            "\n" +
            "## Format example\n" +
            "{format_example}\n" +
            "-----\n" +
            "Role: You are a professional product manager; the goal is to design a concise, usable, efficient product\n" +
            "Requirements: According to the context, fill in the following missing information, each section name is a key in json ,If the requirements are unclear, ensure minimum viability and avoid excessive design\n" +
            "\n" +
            "## Original Requirements: Provide as Plain text, place the polished complete original requirements here\n" +
            "\n" +
            "## Product Goals: Provided as Python list[str], up to 3 clear, orthogonal product goals. If the requirement itself is simple, the goal should also be simple\n" +
            "\n" +
            "## User Stories: Provided as Python list[str], up to 5 scenario-based user stories, If the requirement itself is simple, the user stories should also be less\n" +
            "\n" +
            "## Competitive Analysis: Provided as Python list[str], up to 7 competitive product analyses, consider as similar competitors as possible\n" +
            "\n" +
            "## Competitive Quadrant Chart: Use mermaid quadrantChart code syntax. up to 14 competitive products. Translation: Distribute these competitor scores evenly between 0 and 1, trying to conform to a normal distribution centered around 0.5 as much as possible.\n" +
            "\n" +
            "## Requirement Analysis: Provide as Plain text. Be simple. LESS IS MORE. Make your requirements less dumb. Delete the parts unnessasery.\n" +
            "\n" +
            "## Requirement Pool: Provided as Python list[list[str], the parameters are requirement description, priority(P0/P1/P2), respectively, comply with PEP standards; no more than 5 requirements and consider to make its difficulty lower\n" +
            "\n" +
            "## UI Design draft: Provide as Plain text. Be simple. Describe the elements and functions, also provide a simple style description and layout description.\n" +
            "## Anything UNCLEAR: Provide as Plain text. Make clear here.\n" +
            "\n" +
            "output a properly formatted JSON, wrapped inside [CONTENT][/CONTENT] like format example,\n" +
            "and only output the json inside this tag, nothing else\n";

    private static final String MARKDOWN_PROMPT_TEMPLATE = "# Context\n" +
            "## Original Requirements\n" +
            "{requirements}\n" +
            "\n" +
            "## Search Information\n" +
            "{search_information}\n" +
            "\n" +
            "## mermaid quadrantChart code syntax example. DONT USE QUOTO IN CODE DUE TO INVALID SYNTAX. Replace the <Campaign X> with REAL COMPETITOR NAME\n" +
            "```mermaid\n" +
            "quadrantChart\n" +
            "    title Reach and engagement of campaigns\n" +
            "    x-axis Low Reach --> High Reach\n" +
            "    y-axis Low Engagement --> High Engagement\n" +
            "    quadrant-1 We should expand\n" +
            "    quadrant-2 Need to promote\n" +
            "    quadrant-3 Re-evaluate\n" +
            "    quadrant-4 May be improved\n" +
            "    \"Campaign: A\": [0.3, 0.6]\n" +
            "    \"Campaign B\": [0.45, 0.23]\n" +
            "    \"Campaign C\": [0.57, 0.69]\n" +
            "    \"Campaign D\": [0.78, 0.34]\n" +
            "    \"Campaign E\": [0.40, 0.34]\n" +
            "    \"Campaign F\": [0.35, 0.78]\n" +
            "    \"Our Target Product\": [0.5, 0.6]\n" +
            "```\n" +
            "\n" +
            "## Format example\n" +
            "{format_example}\n" +
            "-----\n" +
            "Role: You are a professional product manager; the goal is to design a concise, usable, efficient product\n" +
            "Requirements: According to the context, fill in the following missing information, note that each sections are returned in Python code triple quote form separately. If the requirements are unclear, ensure minimum viability and avoid excessive design\n" +
            "ATTENTION: Use '##' to SPLIT SECTIONS, not '#'. AND '## <SECTION_NAME>' SHOULD WRITE BEFORE the code and triple quote. Output carefully referenced \"Format example\" in format.\n" +
            "\n" +
            "## Original Requirements: Provide as Plain text, place the polished complete original requirements here\n" +
            "\n" +
            "## Product Goals: Provided as Python list[str], up to 3 clear, orthogonal product goals. If the requirement itself is simple, the goal should also be simple\n" +
            "\n" +
            "## User Stories: Provided as Python list[str], up to 5 scenario-based user stories, If the requirement itself is simple, the user stories should also be less\n" +
            "\n" +
            "## Competitive Analysis: Provided as Python list[str], up to 7 competitive product analyses, consider as similar competitors as possible\n" +
            "\n" +
            "## Competitive Quadrant Chart: Use mermaid quadrantChart code syntax. up to 14 competitive products. Translation: Distribute these competitor scores evenly between 0 and 1, trying to conform to a normal distribution centered around 0.5 as much as possible.\n" +
            "\n" +
            "## Requirement Analysis: Provide as Plain text. Be simple. LESS IS MORE. Make your requirements less dumb. Delete the parts unnessasery.\n" +
            "\n" +
            "## Requirement Pool: Provided as Python list[list[str], the parameters are requirement description, priority(P0/P1/P2), respectively, comply with PEP standards; no more than 5 requirements and consider to make its difficulty lower\n" +
            "\n" +
            "## UI Design draft: Provide as Plain text. Be simple. Describe the elements and functions, also provide a simple style description and layout description.\n" +
            "## Anything UNCLEAR: Provide as Plain text. Make clear here.\n" +
            "\"\"\",\n" +
            "        \"FORMAT_EXAMPLE\": \"\"\"\n" +
            "---\n" +
            "## Original Requirements\n" +
            "The boss ... \n" +
            "\n" +
            "## Product Goals\n" +
            "```python\n" +
            "[\n" +
            "    \"Create a ...\",\n" +
            "]\n" +
            "```\n" +
            "\n" +
            "## User Stories\n" +
            "```python\n" +
            "[\n" +
            "    \"As a user, ...\",\n" +
            "]\n" +
            "```\n" +
            "\n" +
            "## Competitive Analysis\n" +
            "```python\n" +
            "[\n" +
            "    \"Python Snake Game: ...\",\n" +
            "]\n" +
            "```\n" +
            "\n" +
            "## Competitive Quadrant Chart\n" +
            "```mermaid\n" +
            "quadrantChart\n" +
            "    title Reach and engagement of campaigns\n" +
            "    ...\n" +
            "    \"Our Target Product\": [0.6, 0.7]\n" +
            "```\n" +
            "\n" +
            "## Requirement Analysis\n" +
            "The product should be a ...\n" +
            "\n" +
            "## Requirement Pool\n" +
            "```python\n" +
            "[\n" +
            "    [\"End game ...\", \"P0\"]\n" +
            "]\n" +
            "```\n" +
            "\n" +
            "## UI Design draft\n" +
            "Give a basic function description, and a draft\n" +
            "\n" +
            "## Anything UNCLEAR\n" +
            "There are no unclear points.\n" +
            "---";

    private static final String FORMAT_EXAMPLE="[CONTENT]\n" +
            "## Original Requirements\n" +
            "The boss ... \n" +
            "\n" +
            "## Product Goals\n" +
            "```python\n" +
            "[\n" +
            "    \"Create a ...\",\n" +
            "]\n" +
            "```\n" +
            "\n" +
            "## User Stories\n" +
            "```python\n" +
            "[\n" +
            "    \"As a user, ...\",\n" +
            "]\n" +
            "```\n" +
            "\n" +
            "## Competitive Analysis\n" +
            "```python\n" +
            "[\n" +
            "    \"Python Snake Game: ...\",\n" +
            "]\n" +
            "```\n" +
            "\n" +
            "## Competitive Quadrant Chart\n" +
            "```mermaid\n" +
            "quadrantChart\n" +
            "    title Reach and engagement of campaigns\n" +
            "    ...\n" +
            "    \"Our Target Product\": [0.6, 0.7]\n" +
            "```\n" +
            "\n" +
            "## Requirement Analysis\n" +
            "The product should be a ...\n" +
            "\n" +
            "## Requirement Pool\n" +
            "```python\n" +
            "[\n" +
            "    [\"End game ...\", \"P0\"]\n" +
            "]"+
            "```\n" +
            "## UI Design draft\n" +
            "Give a basic function description, and a draft\n" +
            "## Anything UNCLEAR\n" +
            "There are no unclear points.\n" +
            "[/CONTENT]";

    public WritePRD() {
        super("", null, null);
    }

//    public WritePRD(String name, Object context, BaseLanguageModel llm) {
//        super(name, context, llm);
//    }

    @Override
    public ActionOutput run(List<Message> messages) {
        log.warn("WritePRD:" + JSON.toJSONString(messages));
        // TODO ...
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("requirements", messages.get(0).getContent());
        inputs.put("search_information", "");
        inputs.put("format_example", FORMAT_EXAMPLE);
        String prompt = PromptConverter.replacePrompt(JSON_PROMPT_TEMPLATE, inputs);
        log.info("WritePRD,prompt :{}",prompt);
        String prdRsp = getCache().readeCache("WritePRD", "prd.txt");
        if (StringUtils.isEmpty(prdRsp)) {
            prdRsp = getLlm().predict(prompt);
            getCache().writeCache("WritePRD", "prd.txt", prdRsp);
        }
        log.warn("WritePRD,prdRsp:" + prdRsp);
        ActionOutput actionOutput = new ActionOutput();
        actionOutput.setContent(prdRsp);
        return actionOutput;
    }
}
