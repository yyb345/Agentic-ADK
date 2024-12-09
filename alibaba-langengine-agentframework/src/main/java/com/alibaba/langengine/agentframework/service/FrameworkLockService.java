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
package com.alibaba.langengine.agentframework.service;

import com.alibaba.langengine.agentframework.model.FrameworkEngineConfiguration;
import com.alibaba.langengine.agentframework.model.service.LockService;
import com.alibaba.langengine.agentframework.model.service.ServiceBase;
import lombok.extern.slf4j.Slf4j;

/**
 * Framework lock service
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class FrameworkLockService extends ServiceBase implements LockService {

    public FrameworkLockService(FrameworkEngineConfiguration agentEngineConfiguration) {
        super(agentEngineConfiguration);
    }

    @Override
    public Long incr(String key) {
        return null;
    }

    @Override
    public Long incrBy(String key, Long count) {
        return null;
    }

    @Override
    public Long decr(String key) {
        return null;
    }

    @Override
    public Long reset(String key, Long count) {
        return null;
    }

    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public void set(String key, String value) {

    }

    @Override
    public Long expire(String key, int seconds) {
        return null;
    }

    @Override
    public void delete(String key) {

    }
}
