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

import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.PreDestroy;


/**
 * @author liuchunhe.lch on 2023/9/2 09:51
 * well-meaning people get together do meaningful things
 **/
@Slf4j
public class AiRedisTemplate {
    private JedisPool jedisPool;

    public AiRedisTemplate(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public void set(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.set(key, value);
        } catch (Exception ex) {
            log.error("redis set method occur exception", ex);
            throw  ex;
        }finally {
            releaseConnection(jedis);
        }
    }

    public void setEx(String key, String value, int time) {
        Jedis jedis = null;
        try  {
            jedis = jedisPool.getResource();
            jedis.setex(key, time, value);
        } catch (Exception ex) {
            log.error("redis setEx method occur exception", ex);
            throw ex;
        }finally {

            releaseConnection(jedis);
        }
    }


    public String get(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.get(key);
        } catch (Exception ex) {
            log.error("redis get method occur exception", ex);
            throw  ex;
        }finally {
            releaseConnection(jedis);
        }
    }

    public <T> T get(String key, Class<T> type) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            byte[] bytes = jedis.get(key.getBytes(Charsets.UTF_8));
            if (null != bytes && bytes.length > 0) {
                return JSON.parseObject(bytes, type);
            }else{
                return null;
            }
        } catch (Exception ex) {
            log.error("redis get method occur exception", ex);
            throw ex;
        }finally {
            releaseConnection(jedis);
        }
    }


    public void setObject(String key, Object obj, int sec) {
        String val = JSON.toJSONString(obj);
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.setex(key.getBytes(Charsets.UTF_8), sec, val.getBytes(Charsets.UTF_8));
        } catch (Exception ex) {
            log.error("redis setObject method occur exception ", ex);
            throw ex;
        }finally {
            releaseConnection(jedis);
        }
    }

    public <T> T getObject(String key, Class<T> type) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            byte[] bytes = jedis.get(key.getBytes(Charsets.UTF_8));
            if (null != bytes && bytes.length > 0) {
                return JSON.parseObject(bytes, type);
            }else{
                return null;
            }

        } catch (Exception ex) {
            log.error("redis getObject method occur exception ", ex);
            throw ex;
        }finally {
            releaseConnection(jedis);
        }

    }

    public void delete(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.del(key);
        } catch (Exception ex) {
            log.error("redis delete method occur exception ", ex);
            throw ex;
        }finally {
            releaseConnection(jedis);
        }
    }

    @PreDestroy
    void destroy() {
        if (null == jedisPool) {
            log.info("redis is null dont need to destroy");
        } else {
            jedisPool.destroy();
            log.info("destroy redis success");
        }
    }

    private void releaseConnection(Jedis connection) {
        if (connection != null) {
            connection.close();
        }
    }
}
