package com.alibaba.agentic.core.engine.delegation;

import com.alibaba.agentic.core.engine.constants.ExecutionConstant;
import com.alibaba.agentic.core.engine.delegation.domain.FlowCanvasRequest;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.flows.service.impl.FlowProcessService;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.google.common.base.Preconditions;
import io.reactivex.rxjava3.core.Flowable;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DelegationFlowCanvas extends FrameworkDelegationBase {

    @Autowired
    private FlowProcessService flowProcessService;

    @Override
    public Flowable<Result> invoke(SystemContext systemContext, Request request) throws Throwable {
        FlowCanvasRequest flowCanvasRequest = new JSONObject(request.getParam()).toJavaObject(FlowCanvasRequest.class);
        Preconditions.checkArgument(StringUtils.isNotEmpty(flowCanvasRequest.getFlowDefinition()));
        Preconditions.checkArgument(StringUtils.isNotEmpty(flowCanvasRequest.getFlowVersion()));
        Map<String, Object> response = new HashMap<>();

        //执行调用
        flowProcessService.startFlow(flowCanvasRequest.getFlowDefinition(),
                flowCanvasRequest.getFlowVersion(),
                flowCanvasRequest.getRequest(), response);

        return (Flowable<Result>) response.get(ExecutionConstant.INVOKE_RESULT);
    }

    @Override
    protected Map<String, Object> generateRequest(ExecutionContext executionContext, String activityId) {
        FlowCanvasRequest request = new FlowCanvasRequest();
        Map<String, Object> properties = super.generateRequest(executionContext, activityId);
        if (MapUtils.isEmpty(properties)) {
            return JSONObject.parseObject(JSONObject.toJSONString(request));
        }
        request.setFlowDefinition(String.valueOf(properties.get("flowDefinition")));
        request.setFlowVersion(String.valueOf(properties.get("flowVersion")));
        request.setRequest(JSONObject.parseObject(String.valueOf(properties.get("parameter"))));
        return JSONObject.parseObject(JSONObject.toJSONString(request));
    }
}
