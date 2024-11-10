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
package com.alibaba.langengine.core.caches;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.outputs.Generation;
import com.alibaba.langengine.core.prompt.MessageInfoDO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache that stores things in memory.
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class InMemoryCache extends BaseCache {

    private static final String SESSION_PREFIX_KEY = "chatmessage_";

    private Map<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public List<Generation> get(String prompt, String llmString) {
        String cacheKey = getCacheKey(prompt, llmString);
        return (List<Generation>)cache.get(cacheKey);
    }

    @Override
    public void update(String prompt, String llmString, List<Generation> returnVal) {
        String cacheKey = getCacheKey(prompt, llmString);
        cache.put(cacheKey, returnVal);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    public List<MessageInfoDO> getMessageInfo(String sessionId) {
        String cacheKey = SESSION_PREFIX_KEY + sessionId;
        if(cache.get(cacheKey) == null) {
            return null;
        }
        return (List<MessageInfoDO>)cache.get(cacheKey);
    }

    public void updateMessageInfo(String sessionId, List<MessageInfoDO> messageInfoDOs) {
        String cacheKey = SESSION_PREFIX_KEY + sessionId;
        try {
            cache.put(cacheKey, messageInfoDOs);
        } catch (Exception e) {
            log.warn("put inmemory error#cacheKey = " + cacheKey + ", value = " + JSON.toJSONString(messageInfoDOs), e);
        }
    }

    public void remove(String sessionId) {
        String cacheKey = SESSION_PREFIX_KEY + sessionId;
        try {
            cache.remove(cacheKey);
        } catch (Exception e) {
            log.warn("delete redis error#cacheKey = " + cacheKey, e);
        }
    }
}
