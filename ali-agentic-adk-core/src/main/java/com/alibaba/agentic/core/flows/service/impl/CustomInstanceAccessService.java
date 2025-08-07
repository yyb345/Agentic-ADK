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
