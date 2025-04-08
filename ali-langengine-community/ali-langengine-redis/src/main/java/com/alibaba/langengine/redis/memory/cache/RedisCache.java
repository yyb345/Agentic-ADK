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
package com.alibaba.langengine.redis.memory.cache;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.outputs.Generation;
import com.alibaba.langengine.core.caches.BaseCache;
import com.alibaba.langengine.core.prompt.MessageInfoDO;

import com.alibaba.langengine.redis.memory.RedisConfiguration;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Cache that stores things in redis.
 *
 * @author liuchunhe.lch on 2023/9/2 09:51
 * well-meaning people get together do meaningful things
 **/
@Slf4j
@Data
public class RedisCache extends BaseCache {

    private static final String SESSION_PREFIX_KEY = "chatmessage_";
    private Integer redisExpireTimeSeconds;

    private AiRedisTemplate aiRedisTemplate;

    public RedisCache() {
        String expireTime = StringUtils.isBlank(RedisConfiguration.REDIS_SESSION_EXPIRE_TIME_SECONDS) ? "60"
            : RedisConfiguration.REDIS_SESSION_EXPIRE_TIME_SECONDS;
        this.redisExpireTimeSeconds = Integer.valueOf(expireTime);
    }

    public RedisCache(AiRedisTemplate aiRedisTemplate) {
        this.aiRedisTemplate = aiRedisTemplate;
        String expireTime = StringUtils.isBlank(RedisConfiguration.REDIS_SESSION_EXPIRE_TIME_SECONDS) ? "60"
            : RedisConfiguration.REDIS_SESSION_EXPIRE_TIME_SECONDS;
        this.redisExpireTimeSeconds = Integer.valueOf(expireTime);
    }

    @Override
    public List<Generation> get(String prompt, String llmString) {
        String cacheKey = getCacheKey(prompt, llmString);
        String value = aiRedisTemplate.get(cacheKey);
        if (StringUtils.isBlank(value)) {
            return null;
        } else {
            return JSON.parseArray(value, Generation.class);
        }
    }

    @Override
    public void update(String prompt, String llmString, List<Generation> returnVal) {
        String cacheKey = getCacheKey(prompt, llmString);
        try {
            aiRedisTemplate.setEx(cacheKey, JSON.toJSONString(returnVal), redisExpireTimeSeconds);
        } catch (Exception e) {
            log.warn("put redis error#cacheKey = " + cacheKey + ", value = " + JSON.toJSONString(returnVal), e);
        }
    }

    @Override
    public void clear() {

    }

    public List<MessageInfoDO> getMessageInfo(String sessionId) {
        String cacheKey = SESSION_PREFIX_KEY + sessionId;
        String value = aiRedisTemplate.get(cacheKey);
        if (StringUtils.isBlank(value)) {
            return null;
        } else {
            return JSON.parseArray(value, MessageInfoDO.class);
        }
    }

    public void updateMessageInfo(String sessionId, List<MessageInfoDO> messageInfoDOs) {
        String cacheKey = SESSION_PREFIX_KEY + sessionId;
        try {
            aiRedisTemplate.setEx(cacheKey, JSON.toJSONString(messageInfoDOs), redisExpireTimeSeconds);
        } catch (Exception e) {
            log.warn("put redis error#cacheKey = " + cacheKey + ", value = " + JSON.toJSONString(messageInfoDOs), e);
        }
    }

    public void remove(String sessionId) {
        String cacheKey = SESSION_PREFIX_KEY + sessionId;
        try {
            aiRedisTemplate.delete(cacheKey);
        } catch (Exception e) {
            log.warn("delete redis error#cacheKey = " + cacheKey, e);
        }
    }
}
