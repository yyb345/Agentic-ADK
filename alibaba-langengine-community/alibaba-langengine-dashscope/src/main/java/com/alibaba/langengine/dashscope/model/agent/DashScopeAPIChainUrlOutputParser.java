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
package com.alibaba.langengine.dashscope.model.agent;

import com.alibaba.langengine.core.agent.AgentOutputParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DashScopeAPIChainUrlOutputParser extends AgentOutputParser {

    @Override
    public String getFormatInstructions() {
        return null;
    }

    @Override
    public String getParserType() {
        return "apichainurl";
    }

    @Override
    public Object parse(String text) {
        String regex = "```(bash|ruby)*(.*?)```?";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if(matcher.find()) {
            String url = matcher.group(2).replaceAll("\n", "").trim();
            return url;
        }
        return text;
    }

    public static void main(String[] args){
        String text = "Based on the given API documentation, the full API URL to call for answering the user question would be:\n" +
                "```bash\n" +
                "http://localhost:7001/api/population/france\n" +
                "```";

        DashScopeAPIChainUrlOutputParser outputParser = new DashScopeAPIChainUrlOutputParser();
        Object result = outputParser.parse(text);
        System.out.println(result);
    }
}