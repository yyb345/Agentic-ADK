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

import com.alibaba.langengine.agentframework.agentcore.SimpleListenerExecutor;
import com.alibaba.langengine.agentframework.behavior.AgentBehaviorHelper;
import com.alibaba.smart.framework.engine.SmartEngine;
import com.alibaba.smart.framework.engine.configuration.ConfigurationOption;
import com.alibaba.smart.framework.engine.configuration.LockStrategy;
import com.alibaba.smart.framework.engine.configuration.ProcessEngineConfiguration;
import com.alibaba.smart.framework.engine.configuration.impl.DefaultProcessEngineConfiguration;
import com.alibaba.smart.framework.engine.configuration.impl.DefaultSmartEngine;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.exception.LockException;
import com.alibaba.smart.framework.engine.extension.scanner.SimpleAnnotationScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
public class FrameworkFactoryBean implements FactoryBean<SmartEngine>, InitializingBean {

    private SmartEngine smartEngine;

    @Resource
    private AgentExpressionEvaluator agentExpressionEvaluator;

    @Resource
    private AgentExceptionProcessor agentExceptionProcessor;

    @Autowired
    private ExecutorService taskExecutor;

    @Resource
    private SimpleListenerExecutor simpleListenerExecutor;

    @Override
    public void afterPropertiesSet() throws Exception {
        ProcessEngineConfiguration processEngineConfiguration = getProcessEngineConfiguration();
        smartEngine = new DefaultSmartEngine();
        smartEngine.init(processEngineConfiguration);
    }

    public ProcessEngineConfiguration getProcessEngineConfiguration() {
        log.info("FrameworkFactoryBean init");
        ProcessEngineConfiguration processEngineConfiguration = new DefaultProcessEngineConfiguration();
        processEngineConfiguration.setInstanceAccessor(new CustomInstanceAccessor());

//        processEngineConfiguration.setAnnotationScanner(new SimpleAnnotationScanner(SmartEngine.class.getPackage().getName(),
//                this.getClass().getPackage().getName()));

        processEngineConfiguration.setExpressionEvaluator(agentExpressionEvaluator);
        processEngineConfiguration.setExceptionProcessor(agentExceptionProcessor);
        processEngineConfiguration.setExecutorService(taskExecutor);
        processEngineConfiguration.getOptionContainer().put(ConfigurationOption.SERVICE_ORCHESTRATION_OPTION);
        processEngineConfiguration.setAnnotationScanner(new SimpleAnnotationScanner(SmartEngine.class.getPackage().getName(),
                this.getClass().getPackage().getName(),
                AgentBehaviorHelper.class.getPackage().getName()));

        LockStrategy lockStrategy = new LockStrategy() {
            @Override
            public void tryLock(String s, ExecutionContext executionContext) throws LockException {

            }

            @Override
            public void unLock(String s, ExecutionContext executionContext) throws LockException {

            }
        };
        processEngineConfiguration.setLockStrategy(lockStrategy);

        processEngineConfiguration.setParallelServiceOrchestration(new AgentParallelServiceOrchestration());
        processEngineConfiguration.setDelegationExecutor(new AgentDelegationExecutor());

        processEngineConfiguration.setListenerExecutor(simpleListenerExecutor);

        return processEngineConfiguration;
    }

    @Override
    public SmartEngine getObject(){
        return smartEngine;
    }

    @Override
    public Class<?> getObjectType() {
        return SmartEngine.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
