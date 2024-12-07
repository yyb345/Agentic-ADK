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

import com.alibaba.langengine.redis.memory.RedisConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis cache config
 * @author liuchunhe.lch on 2023/9/2 09:52
 * well-meaning people get together do meaningful things
 **/
@Configuration
public class RedisCacheConfig {

    private static final String REDIS_HOST = RedisConfiguration.REDIS_HOST;
    private static final String REDIS_PORT = RedisConfiguration.REDIS_PORT;
    private static final String REDIS_P = RedisConfiguration.REDIS_P;


    @Bean(name = "memoryRedisPool",destroyMethod = "close")
    public JedisPool jedisPool() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, REDIS_HOST, Integer.parseInt(REDIS_PORT), 60 * 1000, REDIS_P);
        return jedisPool;
    }

    @Bean(name = "aiRedisTemplate")
    public AiRedisTemplate aiRedisTemplate() {
        return new AiRedisTemplate(jedisPool());
    }

    @Bean
    public RedisCache redisCache(AiRedisTemplate aiRedisTemplate) {
        RedisCache redisCache = new RedisCache();
        redisCache.setAiRedisTemplate(aiRedisTemplate);
        return redisCache;
    }
}
