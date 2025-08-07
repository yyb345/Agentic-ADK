package com.alibaba.agentic.core.flows.storage.redis;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisCache {

    public static JedisPool jedisPool(String host, String port, String password) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        return new JedisPool(jedisPoolConfig, host, Integer.parseInt(port), 60 * 1000, password);
    }
}
