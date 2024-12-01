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
package com.alibaba.langengine.agentframework.config;

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

        agentEngineConfiguration.setLanguageModelService(new DefaultLanguageModelService());
        agentEngineConfiguration.setToolCallingService(new DefaultToolCallingSearvice());

        String llmTemplateConfigs = "[{\n" +
                "\t\"modelId\": \"1\",\n" +
                "\t\"modelName\": \"gpt-3.5-turbo\",\n" +
                "\t\"modelTemplate\": \"ChatModelOpenAI\",\n" +
                "\t\"modelType\": \"openai\",\n" +
                "    \"autoLlmFlag\": true,\n" +
                "    \"maxNewTokens\": null\n" +
                "}]";
        agentEngineConfiguration.setLlmTemplateConfig(llmTemplateConfigs);

        String cotLlmTemplateConfig = "{\n" +
                "  \"modelId\": \"1\",\n" +
                "  \"modelName\": \"gpt-3.5-turbo\",\n" +
                "  \"modelTemplate\": \"ChatModelOpenAI\",\n" +
                "  \"modelType\": \"openai\",\n" +
                "  \"autoLlmFlag\": true,\n" +
                "  \"maxTokens\": 2048,\n" +
                "  \"topP\": 1.0,\n" +
                "  \"topK\": 1,\n" +
                "  \"temperature\": 0.001,\n" +
                "  \"sseInc\": true\n" +
                "}";
        agentEngineConfiguration.setCotLlmTemplateConfig(cotLlmTemplateConfig);

        agentEngineConfiguration.setCotRetryCount(0);

        agentEngineConfiguration.setCotFallbackLlmTemplateConfig(cotLlmTemplateConfig);

        return agentEngineConfiguration;
    }
}
