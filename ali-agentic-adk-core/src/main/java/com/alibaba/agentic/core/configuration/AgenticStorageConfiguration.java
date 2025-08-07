/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.core.configuration;

import com.alibaba.agentic.core.flows.storage.bpmn.FlowDataStorage;
import com.alibaba.agentic.core.flows.storage.bpmn.InMemoryFlowDataStorage;
import com.alibaba.agentic.core.flows.storage.bpmn.RedisFlowDataStorage;
import com.alibaba.agentic.core.flows.storage.redis.AiRedisTemplate;
import com.alibaba.agentic.core.flows.storage.redis.RedisCache;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Objects;

@EnableConfigurationProperties(AliAgenticAdkProperties.class)
@Configuration
public class AgenticStorageConfiguration {


    private final AliAgenticAdkProperties aliAgenticAdkProperties;

    public AgenticStorageConfiguration(AliAgenticAdkProperties aliAgenticAdkProperties) {
        this.aliAgenticAdkProperties = aliAgenticAdkProperties;
    }

    @ConditionalOnMissingBean(name = "aiRedisTemplate")
    @ConditionalOnProperty(name = "ali.agentic.adk.properties.flowStorageStrategy", havingValue = "redis")
    @Bean
    public AiRedisTemplate aiRedisTemplate() {
        return new AiRedisTemplate(RedisCache.jedisPool(aliAgenticAdkProperties.getRedisHost(),
                aliAgenticAdkProperties.getRedisPort(), aliAgenticAdkProperties.getRedisPassword()));
    }


    /***************BpmnXML保存***************/
    @Bean
    @ConditionalOnProperty(name = "ali.agentic.adk.properties.flowStorageStrategy", havingValue = "inMemory")
    public FlowDataStorage inMemoryFlowDataStorage() {
        return new InMemoryFlowDataStorage();
    }

    @Bean
    @ConditionalOnProperty(name = "ali.agentic.adk.properties.flowStorageStrategy", havingValue = "redis")
    public FlowDataStorage redisFlowDataStorage(AiRedisTemplate redisTemplate, AliAgenticAdkProperties aliAgenticAdkProperties) {
        return new RedisFlowDataStorage(redisTemplate, aliAgenticAdkProperties.getRedisKeyPrefix());
    }


}
