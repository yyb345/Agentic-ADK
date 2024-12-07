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
package com.alibaba.langengine.metagpt.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class FileUtils {

    public static String writeFile(String path, String fileName, String content) {
        fileName = fileName.replace("\"", "").replace("\n", "");
        File file = new File(path, fileName);
        file.getParentFile().mkdirs();
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();
            return file.getAbsolutePath();
        } catch (Throwable e) {
            log.error("writeFile error,path:{}", path, e);
        }
        return "";
    }

    public static String readFile(String path, String fileName) {
        fileName = fileName.replace("\"", "").replace("\n", "");
        try {
            List<String> lines = Files.readAllLines(Paths.get(path + "/" + fileName));
            return String.join(System.lineSeparator(), lines);
        } catch (Throwable e) {
            log.warn("readFile error,path:{}", path);
        }
        return "";
    }
}
