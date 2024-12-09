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
package com.alibaba.langengine.chroma.vectorstore.service;

import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import lombok.Data;

import java.time.Duration;

/**
 * Chroma 服务
 *
 * @author xiaoxuan.lp
 */
@Data
public class ChromaService extends RetrofitInitService<ChromaApi> {

    public ChromaService(String serverUrl, Duration timeout) {
        super(serverUrl, timeout, false, null, null);
    }

    @Override
    public Class<ChromaApi> getServiceApiClass() {
        return ChromaApi.class;
    }

    public ChromaCollection createCollection(CreateCollectionRequest request) {
        return execute(getApi().createCollection(request));
    }

    public ChromaCollection collection(String collectionName) {
        return execute(getApi().collection(collectionName));
    }

    public boolean addEmbeddings(String collectionId, ChromaEmbeddingsRequest chromaEmbeddingsRequest) {
        return execute(getApi().addEmbeddings(collectionId, chromaEmbeddingsRequest));
    }

    public ChromaQueryResponse queryCollection(String collectionId, ChromaQueryRequest queryRequest) {
        return execute(getApi().queryCollection(collectionId, queryRequest));
    }
}
