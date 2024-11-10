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
package com.alibaba.langengine.core.storage;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of the BaseStore using a dictionary.
 *
 * @author xiaoxuan.lp
 */
@Data
public class InMemoryEmbeddingsStore extends BaseStore<String, List<Double>> {

    private Map<String, List<Double>> store = new ConcurrentHashMap<>();

    @Override
    public List<Double> get(String key) {
        return store.get(key);
    }

    @Override
    public void set(String key, List<Double> value) {
        store.put(key, value);
    }

    @Override
    public void delete(String key) {
        store.remove(key);
    }
}
