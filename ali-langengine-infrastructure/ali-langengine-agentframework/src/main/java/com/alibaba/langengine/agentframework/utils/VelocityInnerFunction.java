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
package com.alibaba.langengine.agentframework.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.agentframework.model.domain.ChatMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessageRole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.langengine.agentframework.utils.FrameworkUtils.SYS_INTENT_PREFIX;

/**
 * Velocity内置函数
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class VelocityInnerFunction {

    private Map<String, Object> request;

    public VelocityInnerFunction(Map<String, Object> request) {
        this.request = request;
    }

    public String getAutoIntent(String code) {
        log.info("VelocityInnerFunction getAutoIntent:" + code);
        Object codeObj = request.get(code);
        if (codeObj != null) {
            return codeObj.toString();
        }
        Object intentCodeObj = request.get(SYS_INTENT_PREFIX + code);
        return intentCodeObj.toString();
    }

    public String escape(Object value) {
        return StringEscapeUtils.escapeJava(JSON.toJSONString(value));
    }

    public Integer parseInt(Object value) {
        if(value == null) {
            return null;
        }
        if(value instanceof Boolean) {
            return (Boolean)value ? 1 : 0;
        }
        return Integer.parseInt(value.toString());
    }

    public Long parseLong(Object value) {
        if(value == null) {
            return null;
        }
        if(value instanceof Boolean) {
            return (Boolean)value ? 1L : 0L;
        }
        return Long.parseLong(value.toString());
    }

    public Boolean lenEqual(String value, Integer len) {
        if(value == null || len == null) {
            return false;
        }
        Integer valueLen = 0;
        if(value != null) {
            valueLen = value.length();
        }
        return len.equals(valueLen);
    }

    public Boolean lenNotEqual(String value, Integer len) {
        if(value == null || len == null) {
            return false;
        }
        Integer valueLen = 0;
        if(value  != null) {
            valueLen = value.length();
        }
        return !len.equals(valueLen);
    }

    public Boolean lenGreater(String value, Integer len) {
        if(value == null || len == null) {
            return false;
        }
        Integer valueLen = 0;
        if(value  != null) {
            valueLen = value.length();
        }
        return valueLen > len;
    }

    public Boolean lenGreaterEqual(String value, Integer len) {
        if(value == null || len == null) {
            return false;
        }
        Integer valueLen = 0;
        if(value  != null) {
            valueLen = value.length();
        }
        return valueLen >= len;
    }

    public Boolean lenLess(String value, Integer len) {
        if(value == null || len == null) {
            return false;
        }
        Integer valueLen = 0;
        if(value  != null) {
            valueLen = value.length();
        }
        return valueLen < len;
    }

    public Boolean lenLessEqual(String value, Integer len) {
        if(value == null || len == null) {
            return false;
        }
        Integer valueLen = 0;
        if(value  != null) {
            valueLen = value.length();
        }
        return valueLen <= len;
    }




    public Boolean listEqual(List list, Integer len) {
        if(list == null || len == null) {
            return false;
        }
        Integer valueLen = 0;
        if(list != null) {
            valueLen = list.size();
        }
        return len.equals(valueLen);
    }

    public Boolean listNotEqual(List list, Integer len) {
        if(list == null || len == null) {
            return false;
        }
        Integer valueLen = 0;
        if(list != null) {
            valueLen = list.size();
        }
        return !len.equals(valueLen);
    }

    public Boolean listGreater(List list, Integer len) {
        if(list == null || len == null) {
            return false;
        }
        Integer valueLen = 0;
        if(list != null) {
            valueLen = list.size();
        }
        return valueLen > len;
    }

    public Boolean listGreaterEqual(List list, Integer len) {
        if(list == null || len == null) {
            return false;
        }
        Integer valueLen = 0;
        if(list != null) {
            valueLen = list.size();
        }
        return valueLen >= len;
    }

    public Boolean listLess(List list, Integer len) {
        if(list == null || len == null) {
            return false;
        }
        Integer valueLen = 0;
        if(list != null) {
            valueLen = list.size();
        }
        return valueLen < len;
    }

    public Boolean listLessEqual(List list, Integer len) {
        if(list == null || len == null) {
            return false;
        }
        Integer valueLen = 0;
        if(list != null) {
            valueLen = list.size();
        }
        return valueLen <= len;
    }

    public Boolean contain(List list, Object value) {
        if(list == null || value == null) {
            return false;
        }
        return list.contains(value);
    }

    public Boolean notContain(List list, Object value) {
        return !list.contains(value);
    }

    public Boolean indexOf(String value, String target) {
        if(value == null || target == null) {
            return false;
        }
        return value.indexOf(target) >= 0;
    }

    public Boolean notIndexOf(String value, String target) {
        if(value == null || target == null) {
            return true;
        }
        return value.indexOf(target) < 0;
    }

    public Boolean isNull(Object value) {
        return value == null;
    }

    public Boolean isNotNull(Object value) {
        return value != null;
    }

    public String getHistory(String userFormat, String  assistantFormat, List<ChatMessage> historyList) {
        // --User: %s\n
        // --Assistant: %s\n
        if(CollectionUtils.isEmpty(historyList)) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        historyList.stream().forEach(history -> {
            if(ChatMessageRole.USER.value().equals(history.getRole())) {
                builder.append(String.format(userFormat, history.getContent()));
            } else if(ChatMessageRole.ASSISTANT.value().equals(history.getRole())) {
                builder.append(String.format(assistantFormat, history.getContent()));
                builder.append("\n");
            }
        });
        return builder.toString();
    }

    public String getChatHistoryFormat(List<ChatMessage> historyList) {
        return getHistory("--User: %s\n", "--Assistant: %s\n", historyList);
    }

    public static void main(String[] args) {
        VelocityInnerFunction velocityInnerFunction = new VelocityInnerFunction(new HashMap<>());
        Boolean a = false;
        System.out.println(velocityInnerFunction.parseInt(a));
    }
}
