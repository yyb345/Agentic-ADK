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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.core.tool.ToolParamField;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 写入文件工具
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class WriteFileTool extends DefaultTool {

    private WriteFileInput writeFileInput;

    /**
     * 文案目录
     */
    private String rootDir = "";

    public WriteFileTool() {
        setName("write_file");
        setDescription("Write file to disk");

        writeFileInput = new WriteFileInput();

        ToolParamField filePath = new ToolParamField();
        filePath.setTitle("File Path");
        filePath.setDescription("name of file");
        filePath.setType("string");
        writeFileInput.setFilePath(filePath);

        ToolParamField text = new ToolParamField();
        text.setTitle("Text");
        text.setDescription("text to write to file");
        text.setType("string");
        writeFileInput.setText(text);
        setArgs(JSON.parseObject(JSON.toJSONString(writeFileInput), new TypeReference<TreeMap>(){}));
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        String result;
        try {
            Map<String, Object> inputMap = JSON.parseObject(toolInput, new TypeReference<HashMap>() {
            });
            String path = (String) inputMap.get("file_path");
            String filePath = rootDir + "/" + path;
            String text = (String) inputMap.get("text");
            File outFile = new File(filePath);
            FileUtils.writeStringToFile(outFile, text, "utf-8");
            result = String.format("File written successfully to %s.", filePath);
        } catch (Throwable e) {
            log.error("WriteFileTool error", e);
            result = "Error: " + e;
        }
        return new ToolExecuteResult(result);
    }
}
