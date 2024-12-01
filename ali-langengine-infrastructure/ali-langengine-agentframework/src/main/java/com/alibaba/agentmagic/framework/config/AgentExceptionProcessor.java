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
package com.alibaba.agentmagic.framework.config;

import com.alibaba.agentmagic.framework.delegation.provider.AgentTraceHelper;
import com.alibaba.agentmagic.framework.domain.ProcessExecuteLog;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.smart.framework.engine.configuration.ExceptionProcessor;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.exception.EngineException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AgentExceptionProcessor implements ExceptionProcessor {

    @Override
    public void process(Exception exception, Object context) {
        log.info("AgentExceptionProcessor exception is " + exception.getClass().getName());
        if(exception instanceof AgentMagicException) {
            AgentMagicException agentMagicException = (AgentMagicException) exception;
            ExecutionContext executionContext = (ExecutionContext)context;

            ProcessExecuteLog.stop(executionContext, agentMagicException);
            // 工作流调试
            boolean result = AgentTraceHelper.traceNodeException(executionContext, agentMagicException);
            if(result) {
                return;
            }
            throw agentMagicException;
        }
        throw new EngineException(exception);
    }
}
