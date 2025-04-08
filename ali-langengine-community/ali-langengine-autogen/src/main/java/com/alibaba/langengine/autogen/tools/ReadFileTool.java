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
package com.alibaba.langengine.autogen.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Slf4j
public class ReadFileTool extends StructuredTool {

    public ReadFileTool() {
        setName("read_file");
        setDescription("Read file from disk");
        setStructuredSchema(new ReadFileSchema());
    }

    @Override
    public ToolExecuteResult execute(String message) {
        log.error("ReadFileTool message:" + message);
        Map<String, Object> messageMap = JSON.parseObject(message, Map.class);
        if(!messageMap.containsKey("filePath")) {
            return new ToolExecuteResult("ReadFileTool error");
        }
        String filePath = messageMap.get("filePath").toString();

        String path = "/Users/xiaoxuan.lp/works/sources/xiaoxuan.lp/alibaba-langengine/langengine-docs/" + filePath;
        File file = new File(path);
        try {
            String text = FileUtils.readFileToString(file);
            return new ToolExecuteResult(text.trim().replaceAll("\n", ""));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
