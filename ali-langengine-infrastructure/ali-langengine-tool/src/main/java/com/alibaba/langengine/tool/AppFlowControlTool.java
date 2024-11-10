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
package com.alibaba.langengine.tool;

import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class AppFlowControlTool extends DefaultTool {

    public AppFlowControlTool() {
        setName("AppFlowControlTool");
        setDescription("这是一个APP应用流量清零工具。当您处理有关于应用流控问题时很有用。输入应该是一个appkey的数字值。");
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.warn("toolInput:" + toolInput);
        return new ToolExecuteResult("Answer: appkey流量已清空," + System.currentTimeMillis(), true);
    }
}
