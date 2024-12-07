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

import com.alibaba.langengine.core.outputparser.BaseOutputParser;
import lombok.Data;

/**
 * BaseAutoGPTOutputParser
 *
 * @author xiaoxuan.lp
 */
@Data
public abstract class BaseAutoGPTOutputParser extends BaseOutputParser<AutoGPTAction> {

    @Override
    public abstract AutoGPTAction parse(String text);
}
