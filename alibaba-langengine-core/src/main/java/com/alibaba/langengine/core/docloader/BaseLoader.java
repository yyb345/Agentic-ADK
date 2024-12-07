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
import com.alibaba.langengine.core.textsplitter.RecursiveCharacterTextSplitter;
import com.alibaba.langengine.core.textsplitter.TextSplitter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 加载文档基类
 *
 * @author xiaoxuan.lp
 */
public abstract class BaseLoader {

    /**
     * 将数据加载到文档对象中
     *
     * @return
     */
    public abstract List<Document> load();

    public List<Document> load(Map<String, Object> documentMeta) {
        if(documentMeta == null || documentMeta.size() == 0) {
            return load();
        }
        return fetchContent(documentMeta);
    }

    public List<Document> fetchContent(Map<String, Object> documentMeta) {
        return load();
    }

    /**
     * 加载并分割文档，默认实现
     *
     * @param textSplitter
     * @return
     */
    protected List<Document> loadAndSplit(Optional<TextSplitter> textSplitter) {
        TextSplitter _textSplitter = textSplitter.orElse(new RecursiveCharacterTextSplitter());
        List<Document> docs = load();
        return _textSplitter.splitDocuments(docs);
    }

    /**
     * 懒加载文档的方法，暂时抛出未实现的异常
     *
     * @return
     */
    protected Iterator<Document> lazyLoad() {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not implement lazyLoad()");
    }
}
