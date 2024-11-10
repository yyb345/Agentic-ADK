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
package com.alibaba.langengine.core.textsplitter;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.tokenizers.Tokenizer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interface for splitting text into chunks.
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public abstract class TextSplitter extends BaseDocumentTransformer {

    private static final String INDEX = "index";

    private int maxChunkSize = 4000;
    private int maxChunkOverlap = 200;
    private boolean addStartIndex = false;
    private boolean keepSeparator = true;

    /**
     * 获取token长度function
     */
    private Function<String, Integer> lengthFunction;

    /**
     * tokenizer
     */
    private Tokenizer tokenizer;

    @Override
    public List<Document> transformDocuments(List<Document> documents) {
        return splitDocuments(documents);
    }

    /**
     * Split documents.
     *
     * @param documents
     * @return
     */
    public List<Document> splitDocuments(List<Document> documents) {
        List<String> texts = new ArrayList<>();
        List<Map<String, Object>> metadatas = new ArrayList<>();
        for (Document document : documents) {
            texts.add(document.getPageContent());
            //防止NPE
            if(document.getMetadata() == null) {
                document.setMetadata(new HashMap<>());
            }
            metadatas.add(document.getMetadata());
        }
        return createDocuments(texts, metadatas);
    }

    public List<Document> createDocuments(String text) {
        return createDocuments(text, new ArrayList<>());
    }

    public List<Document> createDocuments(String text, List<Map<String, Object>> metadatas) {
        return createDocuments(splitText(text), metadatas);
    }

    /**
     * Create documents from a list of texts.
     *
     * @param texts
     * @param metadatas
     * @return
     */
    public List<Document> createDocuments(List<String> texts, List<Map<String, Object>> metadatas) {
        List<Map<String, Object>> _metadatas = metadatas != null && metadatas.size() > 0 ? metadatas : Collections.nCopies(texts.size(), new HashMap<String, Object>());
        List<Document> documents = new ArrayList<>();
        for(int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            int index = -1;
            List<String> chunks = splitText(text);
            for (String chunk : chunks) {
                Map<String, Object> metadata = new HashMap<>(_metadatas.get(i));
                if(addStartIndex) {
                    index = text.indexOf(chunk, index + 1);
                    metadata.put("start_index", index);
                    metadata.put("char_length", chunk.length());
                }
                Document newDoc = new Document();
                newDoc.setPageContent(chunk);
                newDoc.setMetadata(metadata);
                documents.add(newDoc);
            }
        }
        return documents;
    }

    /**
     * 获取长度
     *
     * @param value
     * @return
     */
    public int getLength(String value) {
        if(StringUtils.isEmpty(value)) {
            return 0;
        }
        if(lengthFunction != null) {
            return lengthFunction.apply(value);
        }
        if(tokenizer != null) {
            return tokenizer.getTokenCount(value);
        }
        return value.length();
    }

    public String getKeepSeparatorRegex(String separator) {
        return "(?=" + separator + ")";
//        return "(" + separator + ")";
    }

    public List<String> mergeSplits(List<String> splits, String separator) {
        int separatorLen = getLength(separator);
        List<String> docs = new ArrayList<>();
        List<String> currentDoc = new ArrayList<>();
        int total = 0;
        for (String d : splits) {
            int len = getLength(d);
            if (total + len + (separatorLen > 0 ? separatorLen : 0) > maxChunkSize) {
                if (total > maxChunkSize) {
                    log.warn("Created a chunk of size " + total + ", which is longer than the specified " + maxChunkSize);
                }
                if (currentDoc.size() > 0) {
                    String doc = joinDocs(currentDoc, separator);
                    if (doc != null) {
                        docs.add(doc);
                    }
                    while (total > maxChunkOverlap || (total + len + (separatorLen > 0 ? separatorLen : 0) > maxChunkSize && total > 0)) {
                        total -= getLength(currentDoc.get(0)) + (separatorLen > 0 && currentDoc.size() > 1 ? separatorLen : 0);
                        currentDoc.remove(0);
                    }
                }
            }
            currentDoc.add(d);
            total += len + (separatorLen > 0 && currentDoc.size() > 1 ? separatorLen : 0);
        }
        String doc = joinDocs(currentDoc, separator);
        if (doc != null) {
            docs.add(doc);
        }
        return docs;
    }

    public String joinDocs(List<String> docs, String separator) {
        String text = String.join(separator, docs).trim();
        if(StringUtils.EMPTY.equals(text)) {
            return null;
        } else {
            return text;
        }
    }

    /**
     * Split text into multiple components.
     *
     * @param text
     * @return
     */
    public abstract List<String> splitText(String text);

    public List<String> splitTextWithRegex(String text, String separator, boolean keepSeparator) {
        List<String> newSplits = new ArrayList<>();

        if (separator != null && !separator.isEmpty()) {
            List<String> splits = new ArrayList<>();
            if (keepSeparator) {
                // The parentheses in the pattern keep the delimiters in the result.
                Pattern pattern = Pattern.compile("(" + separator + ")");
                Matcher matcher = pattern.matcher(text);
                int start = 0;
                while (matcher.find()) {
                    String before = text.substring(start, matcher.start());
                    String sep = matcher.group(1);
                    if (!before.isEmpty()) {
                        splits.add(before);
                    }
                    splits.add(sep);
                    start = matcher.end();
                }
                if (start < text.length()) {
                    splits.add(text.substring(start));
                }
//                System.out.println(JSON.toJSONString(splits));

                // 从索引 1 开始遍历 _splits，步长为 2
                for (int i = 1; i < splits.size() - 1; i += 2) {
                    newSplits.add(splits.get(i) + splits.get(i + 1));
                }
                // 如果 _splits 的长度是奇数，则添加最后一个元素到 splits
                if (splits.size() % 2 == 0) {
                    newSplits.add(splits.get(splits.size() - 1));
                }
                // 创建一个新列表，并将 _splits 的第一个元素添加至开头
                newSplits.add(0, splits.get(0));
            } else {
                Pattern pattern = Pattern.compile(separator);
                String[] result = pattern.split(text);
                for (String split : result) {
                    if (!split.isEmpty()) {
                        newSplits.add(split);
                    }
                }
            }
        } else {
            for (char c : text.toCharArray()) {
                newSplits.add(String.valueOf(c));
            }
        }

        return newSplits;
    }
}
