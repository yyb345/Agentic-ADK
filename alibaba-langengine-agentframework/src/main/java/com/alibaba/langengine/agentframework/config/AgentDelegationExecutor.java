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

import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.smart.framework.engine.common.util.MapUtil;
import com.alibaba.smart.framework.engine.configuration.DelegationExecutor;
import com.alibaba.smart.framework.engine.configuration.ExceptionProcessor;
import com.alibaba.smart.framework.engine.configuration.InstanceAccessor;
import com.alibaba.smart.framework.engine.configuration.ProcessEngineConfiguration;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.delegation.ContextBoundedJavaDelegation;
import com.alibaba.smart.framework.engine.delegation.JavaDelegation;
import com.alibaba.smart.framework.engine.delegation.TccDelegation;
import com.alibaba.smart.framework.engine.exception.EngineException;
import com.alibaba.smart.framework.engine.model.assembly.Activity;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;


@Slf4j
public class AgentDelegationExecutor implements DelegationExecutor {

    @Override
    public void execute(ExecutionContext context, Activity activity) {
        log.info("AgentDelegationExecutor execute");
        Map<String, String> properties = activity.getProperties();
        if(MapUtil.isNotEmpty(properties)){
            String className  =  properties.get("class");
            if(null != className){
                execute(context, className,activity);
            }else {
                log.info("No behavior found:"+activity.getId());
            }
        }
    }

    private static void execute(ExecutionContext context, String className, Activity activity) {
        ProcessEngineConfiguration processEngineConfiguration = context.getProcessEngineConfiguration();
        ExceptionProcessor exceptionProcessor = processEngineConfiguration.getExceptionProcessor();

        InstanceAccessor instanceAccessor = processEngineConfiguration
            .getInstanceAccessor();
        Object delegation = instanceAccessor.access(className);

        try{
            if (delegation instanceof ContextBoundedJavaDelegation) {
                ContextBoundedJavaDelegation contextBoundedJavaDelegation = (ContextBoundedJavaDelegation)delegation;
                contextBoundedJavaDelegation.setClassName(className);
                contextBoundedJavaDelegation.setActivity(activity);

                contextBoundedJavaDelegation.execute(context);

            } else if (delegation instanceof JavaDelegation) {
                JavaDelegation javaDelegation = (JavaDelegation)delegation;
                javaDelegation.execute(context);

            } else if (delegation instanceof TccDelegation) {

                TccDelegation tccDelegation = (TccDelegation)delegation;

                tccDelegation.tryExecute(context);

            } else {
                throw new EngineException("The delegation is not support : " + delegation.getClass());
            }

        }
        catch (AgentMagicException e){
            log.error("AgentDelegationExecutor execute error", e);
            Object sys_AgentPvmActivityTask = context.getRequest().get("sys_AgentPvmActivityTask");
            log.info("sys_AgentPvmActivityTask is " + (sys_AgentPvmActivityTask != null));
            dealException(exceptionProcessor, e, context);
            if(context.getRequest().get("sys_AgentPvmActivityTask") != null) {
                throw e;
            }
        }
        catch (Exception e){
            dealException(exceptionProcessor, e, context);
        }

    }

    private static void dealException(ExceptionProcessor exceptionProcessor, Exception exception,ExecutionContext context) {

        if (null != exceptionProcessor) {
            exceptionProcessor.process(exception,context);
        } else if (exception instanceof RuntimeException) {
            throw (RuntimeException)exception;
        } else {
            throw new EngineException(exception);
        }
    }


}