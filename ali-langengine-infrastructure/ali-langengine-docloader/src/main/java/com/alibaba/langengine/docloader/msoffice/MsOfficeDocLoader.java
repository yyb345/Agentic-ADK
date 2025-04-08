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
package com.alibaba.langengine.docloader.msoffice;

import com.alibaba.langengine.core.docloader.BaseLoader;
import com.alibaba.langengine.core.indexes.Document;
import lombok.Data;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.extractor.POITextExtractor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * microsoft office docloader
 *
 * @author xiaoxuan.lp
 */
@Data
public class MsOfficeDocLoader extends BaseLoader {

    private String filePath;

    @Override
    public List<Document> load() {
        return _load(filePath);
    }

    public List<Document> _load(String filePath) {
        File file = new File(filePath);
        try (POITextExtractor extractor = ExtractorFactory.createExtractor(file)) {
            String text = extractor.getText();

            List<Document> documents = new ArrayList<>();
            Document document = new Document();
            document.setPageContent(text);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", filePath);
            document.setMetadata(metadata);
            documents.add(document);
            return documents;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Document> _load(InputStream inputStream) {
        try (POITextExtractor extractor = ExtractorFactory.createExtractor(inputStream)) {
            String text = extractor.getText();

            List<Document> documents = new ArrayList<>();
            Document document = new Document();
            document.setPageContent(text);
            Map<String, Object> metadata = new HashMap<>();
//            metadata.put("source", filePath);
            document.setMetadata(metadata);
            documents.add(document);
            return documents;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Document> fetchContent(Map<String, Object> documentMeta) {
        if(documentMeta.get("filePath") != null) {
            String filePath = (String) documentMeta.get("filePath");
            return _load(filePath);
        } else if(documentMeta.get("inputStream") != null) {
            InputStream inputStream = (InputStream) documentMeta.get("inputStream");
            return _load(inputStream);
        }
        return load();
    }
}
