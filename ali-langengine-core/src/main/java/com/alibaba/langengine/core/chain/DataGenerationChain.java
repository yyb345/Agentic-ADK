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
package com.alibaba.langengine.core.chain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;

import lombok.Data;

/**
 * 创建一个链，用于根据提供的字段和偏好来生成句子。
 *
 * @author xiaoxuan.lp
 */
@Data
public class DataGenerationChain extends LLMChain {

    public static final String EN_SENTENCE_TEMPLATE = "Given the following fields, create a sentence about them. \n" +
        "Make the sentence detailed and interesting. Use every given field.\n" +
        "If any additional preferences are given, use them during sentence construction as well.\n" +
        "Fields:\n" +
        "{fields}\n" +
        "Preferences:\n" +
        "{preferences}\n" +
        "Sentence:";

    public static final String CN_SENTENCE_TEMPLATE = "给定以下字段，请创建一个关于它们的句子。\n" +
        "使句子详细且有趣。使用每一个给定的字段。\n" +
        "如果给出了任何其他的偏好，请在句子构造中使用它们。\n" +
        "字段：\n" +
        "{fields}\n" +
        "偏好：\n" +
        "{preferences}\n" +
        "句子：";

    private PromptTemplate promptTemplate;

    private Map<String, Object> inputs;

    /**
     * 默认使用英文模板的构造函数。
     */
    public DataGenerationChain() {
        this(false);
    }

    /**
     * 根据参数选择使用中文或英文模板的构造函数。
     *
     * @param useChineseTemplate 是否使用中文模板。
     */
    public DataGenerationChain(boolean useChineseTemplate) {
        this.promptTemplate = new PromptTemplate(useChineseTemplate ? CN_SENTENCE_TEMPLATE : EN_SENTENCE_TEMPLATE,
            Arrays.asList("fields", "preferences"));
        setPrompt(promptTemplate);
        this.inputs = new HashMap<>();
    }

    /**
     * 设置要在句子中使用的字段。
     *
     * @param fields 要使用的字段列表。
     */
    public void setFields(List<String> fields) {
        this.inputs.put("fields", fields);
    }

    /**
     * 设置句子构造中要使用的偏好。
     * 用户可以自定义偏好的Key、Value
     *
     * @param preferences 偏好设置。
     */
    public void setPreferences(Map<String, Object> preferences) {
        this.inputs.put("preferences", preferences);
    }

    /**
     * 根据提供的字段和偏好生成句子。
     *
     * @return 生成的句子。
     */
    public Map<String, Object> generateSentence() {
        // 把Input里面的内容都转为String
        return this.run(inputs);
    }
}
