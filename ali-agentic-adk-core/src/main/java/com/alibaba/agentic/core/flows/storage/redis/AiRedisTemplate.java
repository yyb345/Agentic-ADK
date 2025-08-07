package com.alibaba.agentic.core.flows.storage.redis;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Objects;

@Slf4j
public class AiRedisTemplate {

    private final JedisPool jedisPool;

    public AiRedisTemplate(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public void set(String key, String value) {
        if (Objects.isNull(jedisPool)) {
            throw new RuntimeException("redis session not available");
        }
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
        } catch (Exception ex) {
            log.error("redis set method occur exception", ex);
            throw ex;
        }
    }

    public void setEx(String key, String value, int time) {
        if (Objects.isNull(jedisPool)) {
            throw new RuntimeException("redis session not available");
        }
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, time, value);
        } catch (Exception ex) {
            log.error("redis setEx method occur exception", ex);
            throw ex;
        }
    }

    public Long setNx(String key, String value) {
        if (Objects.isNull(jedisPool)) {
            throw new RuntimeException("redis session not available");
        }
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setnx(key, value);
        } catch (Exception ex) {
            log.error("redis setMx method occur exception", ex);
            throw ex;
        }
    }

    public String get(String key) {
        if (Objects.isNull(jedisPool)) {
            throw new RuntimeException("redis session not available");
        }
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        } catch (Exception ex) {
            log.error("redis get method occur exception", ex);
            throw ex;
        }
    }

    public String getSet(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.getSet(key, value);
        } catch (Exception ex) {
            log.error("redis get method occur exception", ex);
            throw ex;
        }
    }

    public void delete(String key) {
        if (Objects.isNull(jedisPool)) {
            throw new RuntimeException("redis session not available");
        }
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        } catch (Exception ex) {
            log.error("redis del method occur exception", ex);
//            throw  ex;
        }
    }

    public Long incr(String key) {
        if (Objects.isNull(jedisPool)) {
            throw new RuntimeException("redis session not available");
        }
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incr(key);
        } catch (Exception ex) {
            log.error("redis incr method occur exception", ex);
            throw ex;
        }
    }

    public Long incrBy(String key, Long count) {
        if (Objects.isNull(jedisPool)) {
            throw new RuntimeException("redis session not available");
        }
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incrBy(key, count);
        } catch (Exception ex) {
            log.error("redis incrBy method occur exception", ex);
            throw ex;
        }
    }

    public Long decr(String key) {
        if (Objects.isNull(jedisPool)) {
            throw new RuntimeException("redis session not available");
        }
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.decr(key);
        } catch (Exception ex) {
            log.error("redis decr method occur exception", ex);
            throw ex;
        }
    }

    public Long expire(String key, int seconds) {
        if (Objects.isNull(jedisPool)) {
            throw new RuntimeException("redis session not available");
        }
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.expire(key, seconds);
        } catch (Exception ex) {
            log.error("redis expire method occur exception", ex);
            throw ex;
        }
    }

    /**
     * 获取指定 key 的过期时间（剩余生存时间，以秒为单位）
     *
     * @param key Redis 中的键
     * @return 如果 key 存在且设置了过期时间，返回剩余生存时间（秒）
     * 如果 key 存在但没有设置过期时间，返回 -1
     * 如果 key 不存在，返回 -2
     */
    public Long getExpireTime(String key) {
        if (Objects.isNull(jedisPool)) {
            throw new RuntimeException("redis session not available");
        }
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.ttl(key);
        } catch (Exception ex) {
            log.error("redis getExpireTime method occur exception", ex);
            throw ex;
        }
    }

    /**
     * 检查指定的 key 是否存在
     *
     * @param key Redis 中的键
     * @return 如果 key 存在返回 true，否则返回 false
     */
    public boolean exists(String key) {
        if (Objects.isNull(jedisPool)) {
            throw new RuntimeException("redis session not available");
        }
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        } catch (Exception ex) {
            log.error("redis exists method occur exception", ex);
            throw ex;
        }
    }
}