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

import com.alibaba.langengine.core.indexes.Document;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Loader that to load text files.
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class TextDocLoader extends BaseLoader {

    private String filePath;

    @Override
    public List<Document> load() {
        return _load(filePath);
    }

    private List<Document> _load(String filePath) {
        try {
            File file = new File(filePath);
            String text = FileUtils.readFileToString(file);
            List<Document> documents = new ArrayList<>();
            Document document = new Document();
            document.setPageContent(text);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", filePath);
            document.setMetadata(metadata);
            documents.add(document);
            return documents;
        } catch (IOException e) {
            log.error("TextDocLoader.load error", e);
            return null;
        }
    }

    private List<Document> _load(InputStream inputStream) {
        try {
            String text = IOUtils.toString(inputStream);
            List<Document> documents = new ArrayList<>();
            Document document = new Document();
            document.setPageContent(text);
            Map<String, Object> metadata = new HashMap<>();
            document.setMetadata(metadata);
            documents.add(document);
            return documents;
        } catch (IOException e) {
            log.error("TextDocLoader.load error", e);
            return null;
        }
    }

    @Override
    public List<Document> fetchContent(Map<String, Object> documentMeta) {
        if(documentMeta.get("filePath") != null) {
            String filePath = (String) documentMeta.get("filePath");
            return _load(filePath);
        } else if (documentMeta.get("inputStream") != null) {
            InputStream inputStream = (InputStream) documentMeta.get("inputStream");
            return _load(inputStream);
        }
        return load();
    }
}
