package com.alibaba.agentic.core.flows.service.impl;

import com.alibaba.agentic.core.utils.ApplicationContextUtil;
import com.alibaba.smart.framework.engine.configuration.InstanceAccessor;
import com.alibaba.smart.framework.engine.configuration.impl.DefaultInstanceAccessor;
import com.alibaba.smart.framework.engine.util.ClassUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CustomInstanceAccessService implements InstanceAccessor {

    private final InstanceAccessor defaultInstanceAccessor = new DefaultInstanceAccessor();
    private final Map<String, Object> beansContainer = new ConcurrentHashMap<>();

    @Override
    public Object access(String classNameOrBeanName) {
        try {
            Object bean = beansContainer.get(classNameOrBeanName);
            if (null != bean) {
                return bean;
            } else {
                Class<?> clazz = ClassUtil.getContextClassLoader().loadClass(classNameOrBeanName);
                bean = ApplicationContextUtil.getBean(clazz);
                beansContainer.put(classNameOrBeanName, bean);
                return bean;
            }
        } catch (NoSuchBeanDefinitionException e) {
            log.error("CustomInstanceAccessService bean not exist", e);
            return defaultInstanceAccessor.access(classNameOrBeanName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
