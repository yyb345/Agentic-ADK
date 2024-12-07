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

/**
 * 基础存储基类
 *
 * @author xiaoxuan.lp
 *
 * @param <T>
 * @param <O>
 */
public abstract class BaseStore<T, O> {

    /**
     * 获取
     *
     * @param key
     * @return
     */
    public abstract O get(T key);

    /**
     * 设置
     *
     * @param key
     * @param value
     */
    public abstract void set(T key, O value);

    /**
     * 删除
     *
     * @param key
     */
    public abstract void delete(T key);
}
