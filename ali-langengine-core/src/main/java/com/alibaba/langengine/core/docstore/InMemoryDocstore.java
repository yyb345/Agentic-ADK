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
package com.alibaba.langengine.core.docstore;

import com.alibaba.langengine.core.indexes.Document;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple in memory docstore in the form of a dict.
 *
 * @author xiaoxuan.lp
 */
@Data
public class InMemoryDocstore extends Docstore {

    private Map<String, Document> docInfo = new HashMap<>();

    @Override
    public Document search(String query) {
        if(!docInfo.containsKey(query)) {
            return null;
        }
        return docInfo.get(query);
    }
}
