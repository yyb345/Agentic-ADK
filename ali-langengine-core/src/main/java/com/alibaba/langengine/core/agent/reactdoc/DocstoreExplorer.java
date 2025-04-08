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
package com.alibaba.langengine.core.agent.reactdoc;

import com.alibaba.langengine.core.docstore.Docstore;
import com.alibaba.langengine.core.indexes.Document;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to assist with exploration of a document store.
 * 帮助探索文档存储的类
 *
 * @author xiaoxuan.lp
 */
@Data
public class DocstoreExplorer {

    private Docstore docstore;

    private Document document;

    private String lookupStr;

    private Integer lookupIndex;

    public DocstoreExplorer(Docstore docstore) {
        setDocstore(docstore);
    }

    public String search(String term) {
        Document result = docstore.search(term);
        if(result == null) {
            return null;
        }
        setDocument(result);
        return getSummary();
    }

    public String lookup(String term) {
        if (document == null) {
            throw new RuntimeException("Cannot lookup without a successful search first");
        }
        if (!term.toLowerCase().equals(lookupStr)) {
            lookupStr = term.toLowerCase();
            lookupIndex = 0;
        } else {
            lookupIndex++;
        }
        List<String> lookups = new ArrayList<>();
        for (String p : getParagraphs()) {
            if (p.toLowerCase().contains(lookupStr)) {
                lookups.add(p);
            }
        }
        if (lookups.size() == 0) {
            return "No Results";
        } else if (lookupIndex >= lookups.size()) {
            return "No More Results";
        } else {
            String resultPrefix = "(Result " + (lookupIndex + 1) + "/" + lookups.size() + ")";
            return resultPrefix + " " + lookups.get(lookupIndex);
        }
    }

    private String getSummary() {
        if(document == null) {
            throw new RuntimeException("annot get paragraphs without a document");
        }
        String[] paragraphs = getParagraphs();
        return paragraphs[0];
    }

    private String[] getParagraphs() {
        return document.getPageContent().split("\n\n");
    }
}
