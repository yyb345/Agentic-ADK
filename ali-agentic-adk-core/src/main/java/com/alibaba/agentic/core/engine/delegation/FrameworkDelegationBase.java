package com.alibaba.agentic.core.engine.delegation;

import com.alibaba.agentic.core.engine.constants.ExecutionConstant;
import com.alibaba.agentic.core.engine.utils.DelegationUtils;
import com.alibaba.agentic.core.engine.utils.SmartEngineUtils;
import com.alibaba.agentic.core.executor.*;
import com.alibaba.agentic.core.flows.service.TaskExecutionService;
import com.alibaba.agentic.core.flows.service.TaskInstanceService;
import com.alibaba.agentic.core.flows.service.domain.AsyncRequest;
import com.alibaba.agentic.core.flows.service.domain.TaskInstance;
import com.alibaba.agentic.core.utils.AssertUtils;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.delegation.JavaDelegation;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;

import javax.annotation.Resource;
import java.util.Map;
import java.util.UUID;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/15 15:20
 */
@Slf4j
public abstract class FrameworkDelegationBase implements JavaDelegation, Executor {

    protected static final String CURRENT_ACTIVITY_ID = "currentActivityId";
    @Resource
    TaskInstanceService taskInstanceService;
    @Resource
    TaskExecutionService taskExecutionService;

    @Override
    public void execute(ExecutionContext executionContext) {
        log.debug("smart engine execute: {}", executionContext.getRequest());
        Map<String, Object> request = executionContext.getRequest();
        Map<String, Object> response = executionContext.getResponse();
        SystemContext systemContext = (SystemContext) request.get(ExecutionConstant.SYSTEM_CONTEXT);
        systemContext.setExecutor(this);
        if (Boolean.TRUE.equals(request.get(ExecutionConstant.IS_CALLBACK))) {
            Result result = (Result) response.get(ExecutionConstant.CALLBACK_RESULT);
            DelegationUtils.saveInterOutput(executionContext.getExecutionInstance().getProcessDefinitionActivityId(), systemContext, result);
            request.remove(ExecutionConstant.IS_CALLBACK);
            response.put(ExecutionConstant.INVOKE_RESULT, Flowable.fromCallable(() -> result));
            return;
        }
        Flowable<Result> flowable = (Flowable<Result>) response.get(ExecutionConstant.INVOKE_RESULT);
        if (flowable == null) {
            Flowable<Result> restFlow = executeImpl(executionContext, systemContext, request);
            response.put(ExecutionConstant.INVOKE_RESULT, restFlow);
        } else {
            flowable.doOnNext(result -> {
                if (!result.isSuccess()) {
                    log.debug("node error, skip");
                    return;
                }
                Flowable<Result> restFlow = executeImpl(executionContext, systemContext, request);
                response.put(ExecutionConstant.INVOKE_RESULT, restFlow);
            }).onErrorReturn(Result::fail).subscribe();
        }
    }

    protected Flowable<Result> executeImpl(ExecutionContext executionContext, SystemContext systemContext, Map<String, Object> requestMap) {
        Request request = (Request) requestMap.get(ExecutionConstant.ORIGIN_REQUEST);
        Map<String, Object> originRequestParam = generateRequest(executionContext, executionContext.getExecutionInstance().getProcessDefinitionActivityId());
        request.setParam(originRequestParam);

        InvokeMode invokeMode = systemContext.getInvokeMode();
        AssertUtils.assertNotNull(invokeMode);
        if (InvokeMode.ASYNC.equals(invokeMode)) {
            String taskId = processAsyncTask(executionContext, systemContext, request);
            executionContext.setNeedPause(true);
            return Flowable.just(AsyncTaskResult.success(taskId));
        }
        String activityId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();
        return DelegationExecutor.invoke(systemContext, request)
                .map(result -> {
                    DelegationUtils.saveInterOutput(activityId, systemContext, result);
                    return result;
                }).onErrorReturn(Result::fail);
    }


    protected String processAsyncTask(ExecutionContext executionContext, SystemContext systemContext, Request request) {
        String activityId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();
        log.debug("activityId: {}", activityId);
        // persist && submit task
        TaskInstance taskInstance = new TaskInstance()
                .setId(UUID.randomUUID().toString())
                .setRequest(request)
                .setSystemContext(systemContext)
                .setProcessInstance(executionContext.getProcessInstance())
                .setActivityId(activityId);
        String taskId = taskInstanceService.persistTaskInstance(taskInstance);
        taskExecutionService.submitTask(new AsyncRequest().setTaskId(taskId)
                .setRequest(request)
                .setSystemContext(systemContext));
        return taskId;
    }

    protected Map<String, Object> generateRequest(ExecutionContext executionContext, String activityId) {
        Map<String, Object> smartEngineResultMap = SmartEngineUtils.getAllProperties(executionContext, activityId);
        if (MapUtils.isEmpty(smartEngineResultMap)) {
            return Map.of();
        }
        smartEngineResultMap.put(CURRENT_ACTIVITY_ID, activityId);
        return smartEngineResultMap;
    }

}
