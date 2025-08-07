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

import com.alibaba.agentic.core.engine.behavior.ExclusiveGatewayBehavior;
import com.alibaba.agentic.core.engine.parser.SequenceFlowParser;
import com.alibaba.agentic.core.flows.service.TaskExecutionService;
import com.alibaba.agentic.core.flows.service.TaskInstanceService;
import com.alibaba.agentic.core.flows.service.impl.CustomInstanceAccessService;
import com.alibaba.agentic.core.flows.service.impl.DefaultTaskExecutionService;
import com.alibaba.agentic.core.flows.service.impl.DefaultTaskInstanceService;
import com.alibaba.agentic.core.flows.service.impl.FlowProcessService;
import com.alibaba.agentic.core.runner.Runner;
import com.alibaba.smart.framework.engine.SmartEngine;
import com.alibaba.smart.framework.engine.configuration.InstanceAccessor;
import com.alibaba.smart.framework.engine.configuration.ListenerExecutor;
import com.alibaba.smart.framework.engine.configuration.ProcessEngineConfiguration;
import com.alibaba.smart.framework.engine.configuration.impl.DefaultProcessEngineConfiguration;
import com.alibaba.smart.framework.engine.configuration.impl.DefaultSmartEngine;
import com.alibaba.smart.framework.engine.extension.scanner.SimpleAnnotationScanner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(SmartEngine.class)
public class FrameworkConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public SmartEngine constructSmartEngine(InstanceAccessor instanceAccessor, ListenerExecutor listenerExecutor) {
        ProcessEngineConfiguration processEngineConfiguration = new DefaultProcessEngineConfiguration();
        // 添加扫描包路径
        processEngineConfiguration.setAnnotationScanner(new SimpleAnnotationScanner(
                SmartEngine.class.getPackage().getName(),
                this.getClass().getPackage().getName(),
                ExclusiveGatewayBehavior.class.getPackage().getName(),
                SequenceFlowParser.class.getPackage().getName()
        ));
        processEngineConfiguration.setInstanceAccessor(instanceAccessor);
        processEngineConfiguration.setListenerExecutor(listenerExecutor);

        SmartEngine smartEngine = new DefaultSmartEngine();
        smartEngine.init(processEngineConfiguration);

        return smartEngine;
    }

    @Bean
    @ConditionalOnMissingBean
    public InstanceAccessor instanceAccessor() {
        return new CustomInstanceAccessService();
    }

    @Bean
    public FlowProcessService processService(SmartEngine constructSmartEngine) {
        return new FlowProcessService(constructSmartEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskInstanceService taskInstanceService() {
        return new DefaultTaskInstanceService();
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskExecutionService taskExecutionService(TaskInstanceService taskInstanceService, Runner runner) {
        return new DefaultTaskExecutionService(taskInstanceService, runner);
    }


}
