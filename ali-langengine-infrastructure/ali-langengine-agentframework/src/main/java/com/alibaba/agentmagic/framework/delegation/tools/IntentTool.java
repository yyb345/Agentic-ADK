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
package com.alibaba.agentmagic.framework.delegation.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 意图识别内置工具
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class IntentTool extends DefaultTool {

    @Override
    public ToolExecuteResult run(String toolInput) {
        Map<String, Object> result = new HashMap<>();
        result.put("name", getName());
        if(!StringUtils.isEmpty(toolInput)) {
            try {
                Map<String, Object> toolInputMap = JSON.parseObject(toolInput, Map.class);
                if (toolInputMap != null) {
                    result.put("variables", toolInputMap);
                }
            }catch (Exception e) {
                log.warn("parse error and ignore", e);
            }
        }
        return new ToolExecuteResult(JSON.toJSONString(result), true);
    }
}
