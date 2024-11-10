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
package com.alibaba.langengine.core.doctransformer;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.textsplitter.BaseDocumentTransformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lost in the middle:
 * Performance degrades when models must access relevant information in the middle of long contexts.
 * See: https://arxiv.org/abs//2307.03172
 *
 * @author xiaoxuan.lp
 */
public class LongContextReorder extends BaseDocumentTransformer {

    @Override
    public List<Document> transformDocuments(List<Document> documents) {
        return litmReordering(documents);
    }

    private List<Document> litmReordering(List<Document> documents) {
        List reorderedResult = new ArrayList<>();
        Collections.reverse(documents);
        for (int i = 0; i < documents.size(); i++) {
            if (i % 2 == 1) {
                reorderedResult.add(documents.get(i));
            } else {
                reorderedResult.add(0, documents.get(i));
            }
        }
        return reorderedResult;
    }
}
