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
package com.alibaba.langengine.core.prompt.impl;

import com.alibaba.langengine.core.outputparser.BaseOutputParser;
import com.alibaba.langengine.core.prompt.PromptConverter;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Prompt schema模版
 *
 * @author xiaoxuan.lp
 */
@Data
public class PromptTemplate extends StringPromptTemplate {

    private String template;

    public PromptTemplate() {

    }

    public PromptTemplate(String template) {
        setTemplate(template);
    }

    public PromptTemplate(String template, List<String> inputVariables) {
        setTemplate(template);
        setInputVariables(inputVariables);
    }

    public PromptTemplate(String template, List<String> inputVariables, BaseOutputParser outputParser) {
        setTemplate(template);
        setInputVariables(inputVariables);
        setOutputParser(outputParser);
    }

    @Override
    public String getPromptType() {
        return "prompt";
    }

    @Override
    public String format(Map<String, Object> args) {
        if(args == null) {
            return template;
        }
        String realTemplate = template;
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            if(entry.getValue() instanceof String) {
                realTemplate = realTemplate.replaceAll("\\{" + entry.getKey() + "\\}", Matcher.quoteReplacement(entry.getValue().toString()));
            } else {
                // 如果value非字符串，默认被忽略了，这里需要转换成json
                realTemplate = realTemplate.replaceAll("\\{" + entry.getKey() + "\\}", Matcher.quoteReplacement(PromptConverter.toJson(entry.getValue())));
            }
        }
        return realTemplate;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
