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
package com.alibaba.agentic.core.runner.pipe.impl;

import com.alibaba.agentic.core.engine.constants.ExecutionConstant;
import com.alibaba.agentic.core.engine.dto.FlowDefinition;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.flows.service.impl.FlowProcessService;
import com.alibaba.agentic.core.runner.pipe.PipeInterface;
import com.alibaba.agentic.core.runner.pipeline.PipelineRequest;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * DESCRIPTION
 * agent执行样例参考，后续使用smart engine实现
 *
 * @author baliang.smy
 * @date 2025/7/10 17:07
 */
@Slf4j
@Component
public class AgentExecutePipe implements PipeInterface {

    @Autowired
    private FlowProcessService flowProcessService;

    @Override
    public Flowable<Result> doPipe(PipelineRequest pipelineRequest) {
        FlowDefinition definition = pipelineRequest.getFlowDefinition();
        Map<String, Object> response = new HashMap<>();
        flowProcessService.startFlow(definition, pipelineRequest.getRequest(), response);
        return (Flowable<Result>) response.get(ExecutionConstant.INVOKE_RESULT);
    }


    @Override
    public boolean ignore(PipelineRequest pipelineRequest) {
        return false;
    }

}
