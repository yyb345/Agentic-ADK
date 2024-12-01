//package com.alibaba.langengine.agentframework.agentcore;
//
//import com.alibaba.langengine.agentframework.config.FrameworkFactoryBean;
//import com.alibaba.smart.framework.engine.configuration.ProcessEngineConfiguration;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//
///**
// * AgentFactoryBean
// *
// * @author xiaoxuan.lp
// */
//@Slf4j
//@Component
//public class AgentFactoryBean extends FrameworkFactoryBean {
//
//    @Resource
//    private SimpleListenerExecutor simpleListenerExecutor;
//
//    public ProcessEngineConfiguration getProcessEngineConfiguration() {
//        log.info("AgentFactoryBean init");
//        ProcessEngineConfiguration processEngineConfiguration = super.getProcessEngineConfiguration();
//        processEngineConfiguration.setListenerExecutor(simpleListenerExecutor);
//        return processEngineConfiguration;
//    }
//}
