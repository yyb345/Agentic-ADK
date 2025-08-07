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


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("ali.agentic.adk.properties")
public class AliAgenticAdkProperties {

    private String flowStorageStrategy;

    private String[] paths;

    private Boolean generateRedisSessionService;

    private String redisHost;

    private String redisPort;

    private String redisPassword;

    private String redisKeyPrefix;


    public String[] getPaths() {
        return paths;
    }

    public void setPaths(String[] paths) {
        this.paths = paths;
    }

    public Boolean getGenerateRedisSessionService() {
        return generateRedisSessionService;
    }

    public void setGenerateRedisSessionService(Boolean generateRedisSessionService) {
        this.generateRedisSessionService = generateRedisSessionService;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public String getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(String redisPort) {
        this.redisPort = redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public String getRedisKeyPrefix() {
        return redisKeyPrefix;
    }

    public void setRedisKeyPrefix(String redisKeyPrefix) {
        this.redisKeyPrefix = redisKeyPrefix;
    }

    public String getFlowStorageStrategy() {
        return flowStorageStrategy;
    }

    public void setFlowStorageStrategy(String flowStorageStrategy) {
        this.flowStorageStrategy = flowStorageStrategy;
    }

}
