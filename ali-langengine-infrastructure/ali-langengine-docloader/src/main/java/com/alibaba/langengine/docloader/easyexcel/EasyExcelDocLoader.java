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
package com.alibaba.langengine.docloader.easyexcel;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.docloader.BaseLoader;
import com.alibaba.langengine.core.indexes.Document;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xiaoxuan.lp aihe.ah
 *
 * 用于加载EasyExcel文档的类。
 */
@Data
@Slf4j
public class EasyExcelDocLoader<T> extends BaseLoader {

    /**
     * 是否读Excel的头部作为Map的Key
     * 默认Excel的第一行是Map的Key
     */
    private Boolean readHeader = true;

    private String filePath;
    private Class<T> tClass;
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 构造函数用于初始化EasyExcel文档加载器。
     *
     * @param filePath 文档路径。
     * @param tClass   文档中数据的类类型。
     */
    public EasyExcelDocLoader(String filePath, Class<T> tClass) {
        this(filePath, tClass, new HashMap<>());
    }

    /**
     * 构造函数用于初始化EasyExcel文档加载器并附带元数据。
     *
     * @param filePath 文档路径。
     * @param tClass   文档中数据的类类型。
     * @param metadata 元数据。
     */
    public EasyExcelDocLoader(String filePath, Class<T> tClass, Map<String, Object> metadata) {
        this.filePath = filePath;
        this.tClass = tClass;
        this.metadata = metadata;
    }

    /**
     * 从指定路径加载文档。
     *
     * @return 文档列表。
     */
    @Override
    public List<Document> load() {
        List<Document> documents = new ArrayList<>();
        final int[] index = {0};
        try {
            InputStream inputStream = getInputStream(filePath);
            if (readHeader) {
                ExcelUtil.readExcelWithHeaders(
                    inputStream, new Consumer<Map<String, Object>>() {
                        @Override
                        public void accept(Map<String, Object> stringObjectMap) {

                            Document document = new Document();
                            String content = JSON.toJSONString(stringObjectMap);
                            document.setPageContent(content);
                            document.setMetadata(new HashMap<>());
                            document.setIndex(index[0]++);
                            documents.add(document);
                            //}
                        }
                    }
                );
            } else {
                EasyExcel.read(inputStream, tClass,
                        new PageReadListener<T>(dataList -> processDocuments(documents, dataList)))
                    .sheet().doRead();
            }

        } catch (Exception e) {
            log.error("Error reading from file or URL: {}", filePath, e);
        }

        return documents;
    }

    /**
     * 根据文件路径获取输入流。
     *
     * @param filePath 文件或URL路径。
     * @return 输入流。
     * @throws Exception 抛出异常。
     */
    private InputStream getInputStream(String filePath) throws Exception {
        if (isUrl(filePath)) {
            return new URL(filePath).openStream();
        } else {
            InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(filePath);
            if (resourceAsStream == null) {
                try {
                    resourceAsStream = Files.newInputStream(Paths.get(filePath));
                } catch (Exception e) {
                    // 如果无法从文件系统读取文件，抛出异常
                    throw new Exception("Could not find resource in classpath or file system: " + filePath, e);
                }
            }

            return resourceAsStream;
        }
    }

    /**
     * 检查路径是否为URL。
     *
     * @param path 文件路径。
     * @return 如果是URL，则返回true，否则返回false。
     */
    private boolean isUrl(String path) {
        return path.startsWith("http://") || path.startsWith("https://");
    }

    /**
     * 处理文档数据。
     *
     * @param documents 文档列表。
     * @param dataList  数据列表。
     */
    private void processDocuments(List<Document> documents, List<T> dataList) {
        // 增加下标
        int index = 0;
        for (T tdata : dataList) {
            Document document = new Document();
            String content = JSON.toJSONString(tdata);
            document.setPageContent(content);
            document.setMetadata(new HashMap<>(this.metadata));

            if (this.metadata.isEmpty()) {
                document.getMetadata().put("data", tdata);
            }
            document.setIndex(index++);
            documents.add(document);
        }
    }
}
