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
package com.alibaba.langengine.core.agent.autogpt;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * AutoGPTOutputParser
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class AutoGPTOutputParser extends BaseAutoGPTOutputParser {

    @Override
    public AutoGPTAction parse(String text) {
        try {
            AutoGPTPlan autoGPTPlan = JSON.parseObject(text, AutoGPTPlan.class);
            return autoGPTPlan.getCommand();
        } catch (Exception e) {
            log.error("AutoGPTOutputParser parse error", e);
            return null;
        }
    }
}
