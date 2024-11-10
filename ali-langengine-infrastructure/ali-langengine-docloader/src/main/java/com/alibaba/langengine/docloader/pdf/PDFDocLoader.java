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
package com.alibaba.langengine.docloader.pdf;

import com.alibaba.langengine.core.docloader.BaseLoader;
import com.alibaba.langengine.core.indexes.Document;
import lombok.Data;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PDF DocLoader
 *
 * @author xiaoxuan.lp
 */
@Data
public class PDFDocLoader extends BaseLoader {

    private String filePath;

    @Override
    public List<Document> load() {
        return loadPDF(filePath);
    }

    /**
     * 从文件路径加载PDF文档
     *
     * @param filePath PDF文件路径
     * @return 文档列表
     */
    private List<Document> loadPDF(String filePath) {
        try (PDDocument pdfDocument = PDDocument.load(new File(filePath))) {
            return extractDocuments(pdfDocument, filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从输入流加载PDF文档
     *
     * @param inputStream PDF输入流
     * @return 文档列表
     */
    private List<Document> loadPDF(InputStream inputStream) {
        try (PDDocument pdfDocument = PDDocument.load(inputStream)) {
            return extractDocuments(pdfDocument, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 提取PDF文档中的内容并转换为Document对象列表
     *
     * @param pdfDocument PDF文档对象
     * @param source      文档来源(文件路径或null)
     * @return 文档列表
     * @throws IOException 提取内容时发生的I/O异常
     */
    private List<Document> extractDocuments(PDDocument pdfDocument, String source) throws IOException {
        List<Document> documents = new ArrayList<>();
        int pageCount = pdfDocument.getNumberOfPages();
        for (int pageNumber = 1; pageNumber <= pageCount; pageNumber++) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(pageNumber);
            stripper.setEndPage(pageNumber);
            String content = stripper.getText(pdfDocument);

            Document document = new Document();
            document.setPageContent(content);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", source);
            metadata.put("pageNumber", pageNumber);
            document.setMetadata(metadata);
            documents.add(document);
        }
        return documents;
    }

    @Override
    public List<Document> fetchContent(Map<String, Object> documentMeta) {
        if (documentMeta.get("filePath") != null) {
            String filePath = (String) documentMeta.get("filePath");
            return loadPDF(filePath);
        } else if (documentMeta.get("inputStream") != null) {
            InputStream inputStream = (InputStream) documentMeta.get("inputStream");
            return loadPDF(inputStream);
        }
        return load();
    }
}
