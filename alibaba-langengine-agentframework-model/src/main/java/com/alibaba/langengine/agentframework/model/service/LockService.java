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
package com.alibaba.langengine.agentframework.model.service;

/**
 * 锁服务
 *
 * @author xiaoxuan.lp
 */
public interface LockService {

    Long incr(String key);

    Long incrBy(String key, Long count);

    Long decr(String key);

    Long reset(String key, Long count);

    String get(String key);

    void set(String key, String value);

    Long expire(String key, int seconds);

    void delete(String key);
}
