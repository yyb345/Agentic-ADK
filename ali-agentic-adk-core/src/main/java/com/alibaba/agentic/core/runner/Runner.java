package com.alibaba.agentic.core.runner;

import com.alibaba.agentic.core.engine.constants.ExecutionConstant;
import com.alibaba.agentic.core.engine.dto.FlowDefinition;
import com.alibaba.agentic.core.engine.node.FlowCanvas;
import com.alibaba.agentic.core.executor.InvokeMode;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.flows.service.AgentProcessService;
import com.alibaba.agentic.core.flows.service.domain.TaskInstance;
import com.alibaba.agentic.core.flows.service.impl.FlowProcessService;
import com.alibaba.agentic.core.flows.storage.FlowStorageService;
import com.alibaba.agentic.core.runner.pipeline.PipelineRequest;
import com.alibaba.agentic.core.runner.pipeline.PipelineUtil;
import com.alibaba.agentic.core.utils.AssertUtils;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import io.reactivex.rxjava3.processors.ReplayProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/10 14:30
 */
@Service
@Slf4j
public class Runner {


    private static AgentProcessService agentProcessService;

    private static FlowProcessService flowProcessService;

    public Runner() {
    }

    @Autowired
    public void setAgentProcessService(AgentProcessService agentProcessService, FlowProcessService flowProcessService) {
        Runner.agentProcessService = agentProcessService;
        Runner.flowProcessService = flowProcessService;
    }

    /**
     * 通过canvas运行
     *
     * @param canvas
     * @param request
     * @return
     */
    public Flowable<Result> run(FlowCanvas canvas, Request request) {
        AssertUtils.assertNotNull(request.getInvokeMode());
        FlowableProcessor<Result> resultClient = ReplayProcessor.create();
        FlowDefinition flowDefinition = agentProcessService.deploy(canvas);
        log.info("flowDefinition: " + flowDefinition);
        if (InvokeMode.BIDI.equals(request.getInvokeMode())) {
            request.getProcessor().subscribe(event -> {
                        try {
                            run(flowDefinition, new Request().setInvokeMode(InvokeMode.BIDI).setParam(event)).subscribe(resultClient::onNext);
                        } catch (Throwable throwable) {
                            resultClient.onNext(Result.fail(throwable));
                        }
                    },
                    throwable -> resultClient.onNext(Result.fail(throwable)),
                    resultClient::onComplete);
            return resultClient;
        }
        return run(flowDefinition, request);

    }

    /**
     * 异步任务回调
     *
     * @param taskInstance
     * @param result
     */
    public void signal(TaskInstance taskInstance, Result result) {
        flowProcessService.signal(taskInstance.getProcessInstance(),
                taskInstance.getActivityId(),
                Map.of(ExecutionConstant.ORIGIN_REQUEST, taskInstance.getRequest(),
                        ExecutionConstant.SYSTEM_CONTEXT, taskInstance.getSystemContext()),
                Map.of(ExecutionConstant.CALLBACK_RESULT, result));
    }


    private Flowable<Result> run(FlowDefinition flowDefinition, Request request) {
        return PipelineUtil.doPipe(PipelineRequest.builder()
                .flowDefinition(flowDefinition)
                .request(Map.of(ExecutionConstant.ORIGIN_REQUEST, request,
                        ExecutionConstant.SYSTEM_CONTEXT, new SystemContext()
                                .setInvokeMode(request.getInvokeMode())
                                .setRequestParameter(request.getParam()))).build());
    }

}
