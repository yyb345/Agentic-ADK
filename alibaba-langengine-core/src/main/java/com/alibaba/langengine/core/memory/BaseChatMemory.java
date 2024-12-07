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
package com.alibaba.langengine.core.memory;

import com.alibaba.langengine.core.messages.MessageConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import com.alibaba.fastjson.JSON;

import lombok.Data;

import static com.alibaba.langengine.core.util.Constants.CALLBACK_ERROR_KEY;

/**
 * BaseChatMemory
 *
 * @author xiaoxuan.lp
 */
@Data
public abstract class BaseChatMemory extends BaseMemory {

    public abstract BaseChatMessageHistory getChatMemory();

    private String outputKey = "text";

    private String inputKey;

    private boolean returnMessages;

    private String ignoreChatSuffix = null;

    //保存空对话会导致异常数据被记录
    private boolean saveEmptyMessage = true;

    @Override
    public void saveContext(String sessionId, Map<String, Object> inputs, Map<String, Object> outputs) {
        String inputStr = getPromptInputKey(inputs);
        String outputStr = null;
        if (outputs.size() == 1) {
            // 判断下key是否是错误的异常，如果是错误的异常，直接抛出，否则系统也会自己抛出。
            // 因为preOutput是放在塞进错误之后的，因此如果检测到错误，在这里先抛出异常。
            // 否则系统会抛出 cannot be cast to java.lang.String错误，也无法执行下一步的代码

            if (outputs.containsKey(CALLBACK_ERROR_KEY)) {
                throw new RuntimeException("LangEngine执行异常：" + JSON.toJSONString(outputs.get(CALLBACK_ERROR_KEY)));
            }

            for (Map.Entry<String, Object> entry : outputs.entrySet()) {
                if(entry.getValue() instanceof String) {
                    outputStr = (String) entry.getValue();
                } else {
                    outputStr = JSON.toJSONString(entry.getValue());
                }
            }
        } else {
            if(outputs.get(outputKey) != null) {
                if(outputs.get(outputKey) instanceof String) {
                    outputStr = (String)outputs.get(outputKey);
                } else {
                    outputStr = JSON.toJSONString(outputs.get(outputKey));
                }
            } else {
                outputStr = JSON.toJSONString(outputs);
            }
        }
        inputStr = replaceIgnoreSuffixIfNeed(inputStr);
        outputStr = replaceIgnoreSuffixIfNeed(outputStr);
        if(saveEmptyMessage || StringUtils.isNotEmpty(outputStr)){
            if (!getIgnoreHuman()) {
                getChatMemory().addUserMessage(sessionId, inputStr);
            }
            if (!getIgnoreAI()) {
                getChatMemory().addAIMessage(sessionId, outputStr);
            }
        }
    }

    private String replaceIgnoreSuffixIfNeed(String message) {
        if (ignoreChatSuffix != null && message != null && message.contains(ignoreChatSuffix)) {
            return message.substring(0, message.indexOf(ignoreChatSuffix));
        }else{
            return message;
        }
    }

    @Override
    public void clear(String sessionId) {
        getChatMemory().clear();
    }

    protected String getPromptInputKey(Map<String, Object> inputs) {
        String key = inputKey;
        if(StringUtils.isEmpty(inputKey)) {
            List<String> promptInputKeys = new ArrayList<>(inputs.keySet());
            promptInputKeys.removeAll(memoryVariables());
            promptInputKeys.remove("stop");
            key = promptInputKeys.get(0);
        }
        return inputs.get(key).toString();
    }

    /**
     * 将始终返回内存变量列表
     *
     * @return
     */
    public List<String> memoryVariables() {
        return Arrays.asList(new String[] { getMemoryKey() });
    }

    public Map<String, Object> loadMemoryVariables(String sessionId, Map<String, Object> inputs) {
        Map<String, Object> map = new HashMap<>();
        map.put(getMemoryKey(), buffer(sessionId));
        return map;
    }

    public Object buffer() {
        return buffer(null);
    }

    public Object buffer(String sessionId) {
        if(isReturnMessages()) {
            return getChatMemory().getMessages(sessionId);
        } else {
            return MessageConverter.getBufferString(getChatMemory().getMessages(sessionId), getHumanPrefix(), getAiPrefix(), getSystemPrefix(), null, getToolPrefix());
        }
    }
}
