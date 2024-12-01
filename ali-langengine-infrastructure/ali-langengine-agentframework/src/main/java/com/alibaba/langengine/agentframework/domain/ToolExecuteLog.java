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
package com.alibaba.langengine.agentframework.domain;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 工作流执行日志
 *
 * @author xiaoxuan.lp
 */
@Data
public class ToolExecuteLog {

    private static final Logger log = LoggerFactory.getLogger(ToolExecuteLog.class);
    private static final String SPLIT = "#!$";

    private Long startTime;

    private String agentCode;

    private String toolName;

    private String toolDesc;

    private Long executeTime;

    private String requestId;

    private Boolean success = true;

    private String code;

    private String message;

    private String userId;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        append(sb, "startTime", startTime);
        append(sb,"agentCode", agentCode);
        append(sb, "toolName", toolName);
        append(sb, "toolDesc", toolDesc);
        append(sb,"executeTime", executeTime);
        append(sb,"requestId", requestId);
        append(sb,"success", success);
        append(sb,"code", code);
        append(sb,"message", message);
        append(sb,"userId", userId, true);
        return sb.toString();
    }

    private void append(StringBuilder sb,String key, Object value) {
        append(sb,key, value, false);
    }

    private void append(StringBuilder sb, String key, Object value, boolean last) {
        if (value == null) {
            value = "null";
        }
        sb
//                .append(key).append("=")
                .append(String.valueOf(value).replace("\n", "").replace("\r","").replace("=","[eq]").replace("|"," "));

        if (!last) {
            sb.append(SPLIT);
        }
    }

    public void doLog() {
        log.warn(toString());
    }
}
