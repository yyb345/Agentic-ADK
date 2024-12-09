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

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.docloader.BaseLoader;
import com.alibaba.langengine.core.indexes.Document;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * easyexcel docloader old
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class EasyExcelOldDocLoader<T> extends BaseLoader {

    private String filePath;

    private Class<T> tClass;

    public EasyExcelOldDocLoader(String filePath, Class<T> tClass) {
        setFilePath(filePath);
        setTClass(tClass);
    }

    @Override
    public List<Document> load() {
        List<Document> documents = new ArrayList<>();
        EasyExcel.read(filePath, tClass, new PageReadListener<T>(dataList -> {
            for (T tdata : dataList) {
                Document document = new Document();
                String content = JSON.toJSONString(tdata);
                document.setPageContent(content);
                document.setMetadata(new HashMap<>());
                document.getMetadata().put("data", tdata);
                documents.add(document);
            }
        })).sheet().doRead();
        return documents;
    }
}
