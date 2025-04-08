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
package com.alibaba.langengine.core.util;

import com.alibaba.langengine.core.prompt.PromptConverter;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * LLM 辅助工具
 *
 * @author xiaoxuan.lp
 */
@UtilityClass
public class LLMUtils {

    public static final String LLM_FLAG_PROMPT_MARCO = "<|im_start|>system\nYou are a helpful assistant<|im_end|>\n<|im_start|>user\nSystem: This is a chat between a user and an artificial intelligence assistant. The assistant gives helpful, detailed, and polite answers to the user's questions based on the context. The assistant should also indicate when the answer cannot be found in the context. \nPlease give a full and complete answer for the question.\nUser: {input}\nAssistant:\n<|im_end|>\n<|im_start|>assistant\n";
    public static final String LLM_FLAG_PROMPT_QWEN = "<|im_start|>system\nYou are a helpful assistant.<|im_end|>\n<|im_start|>user\n{input}<|im_end|>\n<|im_start|>assistant\n";
    public static final String LLM_FLAG_PROMPT_LLAMA3 = "<|begin_of_text|><|start_header_id|>user<|end_header_id|>\n\n{input}<|eot_id|><|start_header_id|>assistant<|end_header_id|>\n\n";
    public static final String MODEL_TYPE_LLAMA3 = "LLAMA3";
    public static final String MODEL_TYPE_QWEN = "QWEN";
    public static final String MODEL_TYPE_MARCO = "MARCO";
    public static final String MODEL_TYPE_OPENAI = "OPENAI";

    public static String generateFinalPrompt(String original, Boolean autoLlmFlag, String modelType) {
        if(original == null) {
            return null;
        }
        if(autoLlmFlag != null && autoLlmFlag) {
            return getFinalPromptValue(original, modelType);
        }
        return original;
    }
    
    public static String getFinalPromptValue(String original, String modelType) {
        if(MODEL_TYPE_LLAMA3.equals(modelType)) {
            return PromptConverter.replacePrompt(LLM_FLAG_PROMPT_LLAMA3, original);
        } else if(MODEL_TYPE_MARCO.equals(modelType)) {
            return PromptConverter.replacePrompt(LLM_FLAG_PROMPT_MARCO, original);
        } else if(MODEL_TYPE_QWEN.equals(modelType)) {
            return PromptConverter.replacePrompt(LLM_FLAG_PROMPT_QWEN, original);
        } else if(MODEL_TYPE_OPENAI.equals(modelType)) {
            return original;
        }
        return original;
    }
    
    /**
     * 由 周长江(三非) 提供代码思路，由于通义千问API当前不支持设置stops，需要手工去干预下
     *
     * @param answer
     * @param stops
     * @return
     */
    public String interceptAnswerWithStopsSplit(String answer, List<String> stops) {
        if (stops == null || stops.size() == 0 || StringUtils.isEmpty(answer)) {
            return answer;
        }
        return enforceStopTokens(answer, stops.stream()
                .map(stop -> stop.replace("[", "\\["))
                .map(stop -> stop.replace("]", "\\]"))
                .collect(Collectors.toList()));
    }

    /**
     * Cut off the text as soon as any stop words occur.
     */
    public String enforceStopTokens(String answer, List<String> stops) {
        if (stops == null || stops.size() == 0 || StringUtils.isEmpty(answer)) {
            return answer;
        }
        // limit=2 means to maxsplit=1
        return answer.split(String.join("|", stops), 2)[0];
    }
}
