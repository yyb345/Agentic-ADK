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
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.alibaba.langengine.demo.agent.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DauTool extends StructuredTool {

    public DauTool() {
        this.setName("Dau Select Tool");
        setHumanName("Dau Select Tool");
        setDescription("使用该工具查询主播dau. 输入参数是主播id。");
        setStructuredSchema(new TestToolSchema());
    }

    public ToolExecuteResult execute(String toolInput) {
        JSONObject input_dic = JSON.parseObject(toolInput);
        String input = (String) input_dic.getOrDefault("id","54321");
        String result = "huyu success DauTool" + input ;
        log.debug("huyu tools debug result is", result);
        return new ToolExecuteResult("huyu success DauTool"+toolInput, true);

    }
}

