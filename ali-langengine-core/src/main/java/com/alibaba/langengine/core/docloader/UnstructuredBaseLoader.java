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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.alibaba.langengine.core.indexes.Document;

/**
 * @author aihe.ah
 * @time 2023/12/13
 * 功能说明：
 */
public abstract class UnstructuredBaseLoader extends BaseLoader {

    private String mode;
    private Map<String, Object> unstructuredKwargs;
    private List<Function<Document, Document>> postProcessors = Collections.emptyList();

    // 构造函数
    public UnstructuredBaseLoader(String mode, List<Function<Document, Document>> postProcessors,
        Map<String, Object> unstructuredKwargs) {
        // 验证和设置模式
        validateAndSetMode(mode);

        // 验证unstructured版本（需要自己实现）
        validateUnstructuredVersion(unstructuredKwargs);

        this.mode = mode;
        this.unstructuredKwargs = unstructuredKwargs;
        this.postProcessors = postProcessors;
    }

    private void validateAndSetMode(String mode) {
        List<String> validModes = Arrays.asList("single", "elements", "paged");
        if (!validModes.contains(mode)) {
            throw new IllegalArgumentException("Invalid mode: " + mode);
        }
        this.mode = mode;
    }

    private void validateUnstructuredVersion(Map<String, Object> unstructuredKwargs) {
        // 这里需要自己实现版本验证逻辑
    }

    // 抽象方法
    protected abstract List<Document> getElements();

    protected abstract Map<String, Object> getMetadata();

    // 后处理元素
    private List<Document> postProcessElements(List<Document> elements) {
        for (Document element : elements) {
            for (Function<Document, Document> postProcessor : postProcessors) {
                element = postProcessor.apply(element);
            }
        }
        return elements;
    }

    @Override
    public List<Document> load() {
        List<Document> elements = getElements(); // 假设已经定义了这个方法
        List<Document> docs = new ArrayList<>();

        switch (mode) {
            case "elements":
                for (Document element : elements) {
                    Map<String, Object> metadata = getMetadata();
                    // 检查并更新元数据
                    if (element.hasMetadata()) {
                        metadata.putAll(element.getMetadata());
                    }
                    if (element.hasCategory()) {
                        metadata.put("category", element.getCategory());
                    }
                    docs.add(new Document(element.toString(), metadata));
                }
                break;
            case "paged":
                Map<Integer, String> textDict = new HashMap<>();
                Map<Integer, Map<String, Object>> metaDict = new HashMap<>();

                for (int idx = 0; idx < elements.size(); idx++) {
                    Document element = elements.get(idx);
                    Map<String, Object> metadata = getMetadata();
                    // 检查并更新元数据
                    if (element.hasMetadata()) {
                        metadata.putAll(element.getMetadata());
                    }
                    int pageNumber = Integer.valueOf(String.valueOf(metadata.getOrDefault("page_number", 1)));

                    textDict.putIfAbsent(pageNumber, "");
                    textDict.put(pageNumber, textDict.get(pageNumber) + element.toString() + "\n\n");

                    metaDict.putIfAbsent(pageNumber, new HashMap<>());
                    metaDict.get(pageNumber).putAll(metadata);
                }

                for (Integer key : textDict.keySet()) {
                    docs.add(new Document(textDict.get(key), metaDict.get(key)));
                }
                break;
            case "single":
                Map<String, Object> metadata = getMetadata();
                String text = elements.stream().map(Object::toString).collect(Collectors.joining("\n\n"));
                docs.add(new Document(text, metadata));
                break;
            default:
                throw new IllegalArgumentException("Mode " + mode + " not supported.");
        }
        return docs;
    }

}
