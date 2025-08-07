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
