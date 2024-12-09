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
package com.alibaba.langengine.tair.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.aliyun.tair.tairvector.TairVector;
import com.taobao.eagleeye.redis.clients.jedis.JedisPool;
import com.taobao.eagleeye.redis.clients.jedis.JedisPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * tair bean配置
 *
 * @author xiaoxuan.lp
 */
@Configuration
public class TairConfig {

    private static final String TAIR_HOST = TairVectorstoreConfiguration.TAIR_HOST;
    private static final String TAIR_PORT = TairVectorstoreConfiguration.TAIR_PORT;

    private static final String TAIR_P = TairVectorstoreConfiguration.TAIR_P;

    @Bean
    public JedisPool jedisPool() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, TAIR_HOST, Integer.parseInt(TAIR_PORT), 60 * 1000, TAIR_P);
        return jedisPool;
    }

    /**
     * tair vector
     */
    @Bean
    public TairVector tairVector(JedisPool jedisPool) {
        TairVector tairVector = new TairVector(jedisPool);
        return tairVector;
    }

    @Bean
    public Tair tair(TairVector tairVector,
                     @Qualifier(value = "embedding") Embeddings embedding) {
        Tair tair = new Tair();
        tair.setTairVector(tairVector);
        tair.setEmbedding(embedding);
        return tair;
    }
}
