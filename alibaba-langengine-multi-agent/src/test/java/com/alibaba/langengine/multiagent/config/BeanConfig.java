package com.alibaba.langengine.multiagent.config;

import com.alibaba.langengine.agentframework.model.AgentEngineConfiguration;
import com.alibaba.langengine.agentframework.model.FrameworkEngineConfiguration;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class BeanConfig {

    @Bean
    public FrameworkEngineConfiguration agentEngineConfiguration() {
        AgentEngineConfiguration agentEngineConfiguration = new DefaultAgentEngineConfiguration();
        return agentEngineConfiguration;
    }
}
