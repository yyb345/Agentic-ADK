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
package com.alibaba.agentic.core.engine.listener;

import com.alibaba.agentic.core.engine.constants.ExecutionConstant;
import com.alibaba.agentic.core.executor.InvokeMode;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.runner.AsyncConsumer;
import com.alibaba.smart.framework.engine.configuration.impl.DefaultListenerExecutor;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.model.assembly.ExtensionElementContainer;
import com.alibaba.smart.framework.engine.pvm.event.PvmEventConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/28 15:35
 */
@Slf4j
@Service
public class EndListenerExecutor extends DefaultListenerExecutor {

    @Resource
    Optional<List<AsyncConsumer>> asyncConsumerList;


    @Override
    public void execute(PvmEventConstant event, ExtensionElementContainer extensionElementContainer, ExecutionContext context) {
        SystemContext systemContext = (SystemContext) context.getRequest().get(ExecutionConstant.SYSTEM_CONTEXT);
        if (PvmEventConstant.PROCESS_END.equals(event) && InvokeMode.ASYNC.equals(systemContext.getInvokeMode())) {
            Result result = (Result) context.getResponse().get(ExecutionConstant.CALLBACK_RESULT);
            asyncConsumerList.ifPresent(asyncConsumers ->
                    asyncConsumers.forEach(asyncConsumer ->
                            asyncConsumer.accept(result))
            );
        }
        super.execute(event, extensionElementContainer, context);
    }
}
