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
package com.alibaba.langengine.core.docloader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.textsplitter.TextSplitter;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author aihe.ah
 * @time 2023/12/13
 * 功能说明：
 */
@Slf4j
@Data
public class UnstructuredTxtLoader extends BaseLoader {

    private String filePath;

    private TextSplitter textSplitter;

    /**
     * 元数据
     */
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Txt 文件最大支持100 MB
     */
    private Long maxFileSize = 100 * 1024 * 1024L;

    public UnstructuredTxtLoader(String filePath, TextSplitter textSplitter) {
        this.filePath = filePath;
        this.textSplitter = textSplitter;
    }

    @Override
    public List<Document> load() {
        metadata.put("source", filePath);
        if (isUrl(filePath)) {
            return loadFromURL(filePath);
        } else {
            return loadFromFile(filePath);
        }
    }

    private boolean isUrl(String path) {
        return path.startsWith("http://") || path.startsWith("https://");
    }

    private List<Document> loadFromURL(String urlString) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(urlString).openStream()))) {
            String content = reader.lines().collect(Collectors.joining("\n"));
            return textSplitter.createDocuments(Lists.newArrayList(content), Lists.newArrayList(metadata));
        } catch (Exception e) {
            // Handle exceptions
            log.error("loadFromURL error", e);
            return Lists.newArrayList();
        }
    }

    private List<Document> loadFromFile(String filePath) {
        //Path path = Paths.get(filePath);
        try {
            URL resource = getClass().getClassLoader().getResource(filePath);
            Path path = Paths.get(resource.getPath());
            if (Files.size(path) > maxFileSize) {
                throw new RuntimeException("File is too large to process");
            }

            String content = new String(Files.readAllBytes(path));
            return textSplitter.createDocuments(Lists.newArrayList(content), Lists.newArrayList(metadata));
        } catch (Exception e) {
            // Handle exceptions
            log.error("loadFromFile error", e);
            return Lists.newArrayList();
        }
    }
}
