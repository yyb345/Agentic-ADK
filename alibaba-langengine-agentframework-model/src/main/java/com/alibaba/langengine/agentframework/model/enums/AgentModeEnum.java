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
package com.alibaba.langengine.agentframework.model.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * agent类型
 */
public enum AgentModeEnum {

    BUILTIN(0, "builtin"),
    FLOW(1, "flow"),
    HSF(2, "hsf"),
    BOT_CHAT(3, "bot_chat"),
    ;

    private Integer code;
    private String name;

    static Map<Integer, AgentModeEnum> codeToEnum;

    static {
        codeToEnum = new HashMap<>();
        for (AgentModeEnum typeInstance : values()) {
            codeToEnum.put(typeInstance.code, typeInstance);
        }
    }

    AgentModeEnum(Integer code, String name) {
        this.setCode(code);
        this.setName(name);
    }

    public static AgentModeEnum getEnumByCode(Number number) {
        if (number == null) {
            return null;
        }
        int intValue = number.intValue();
        return codeToEnum.get(intValue);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
