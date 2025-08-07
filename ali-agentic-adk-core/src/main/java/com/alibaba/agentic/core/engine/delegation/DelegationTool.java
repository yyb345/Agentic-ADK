package com.alibaba.agentic.core.engine.delegation;

import com.alibaba.agentic.core.engine.delegation.domain.FunctionCallRequest;
import com.alibaba.agentic.core.engine.node.sub.ToolParam;
import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.tools.BaseTool;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DESCRIPTION
 * 工具管理、调用代理
 *
 * @author baliang.smy
 * @date 2025/7/16 10:44
 */
@Component
@Slf4j
public class DelegationTool extends FrameworkDelegationBase {

    private final static Map<String, BaseTool> toolMap = new ConcurrentHashMap<>();

    /**
     * 注册工具
     *
     * @param tool
     */
    public static void register(BaseTool tool) {
        if (toolMap.containsKey(tool.name())) {
            log.warn(String.format("duplicated tool name of %s", tool.name()));
            return;
        }
        toolMap.put(tool.name(), tool);
    }

    @PostConstruct
    public void init() {
        ServiceLoader<BaseTool> loader = ServiceLoader.load(BaseTool.class);
        for (BaseTool tool : loader) {
            register(tool);
        }
    }

    /**
     * 获取工具
     *
     * @param toolName
     * @return
     */
    protected BaseTool getTool(String toolName) {
        if (MapUtils.isEmpty(toolMap) || !toolMap.containsKey(toolName)) {
            throw new BaseException(String.format("tool:%s is not exits.", toolName), ErrorEnum.SYSTEM_ERROR);
        }
        return toolMap.get(toolName);
    }


    @Override
    public Flowable<Result> invoke(SystemContext systemContext, Request request) throws Throwable {
        FunctionCallRequest functionCallRequest = new JSONObject(request.getParam()).toJavaObject(FunctionCallRequest.class);
        BaseTool tool = getTool(functionCallRequest.getToolName());
        try {
            return tool.run(functionCallRequest.getToolParameter(), systemContext)
                    .map(Result::success)
                    .onErrorReturn(Result::fail);
        } catch (Throwable throwable) {
            return Flowable.fromCallable(() -> Result.fail(throwable));
        }
    }

    @Override
    public Map<String, Object> generateRequest(ExecutionContext executionContext, String activityId) {
        FunctionCallRequest request = new FunctionCallRequest();
        Map<String, Object> properties = super.generateRequest(executionContext, activityId);

        if (MapUtils.isEmpty(properties)) {
            return JSONObject.parseObject(JSONObject.toJSONString(request));
        }
        request.setToolName(String.valueOf(properties.get("functionToolName")));

        List<ToolParam> paramList = JSONArray.parseArray(String.valueOf(properties.get("paramList")), ToolParam.class);
        if (CollectionUtils.isEmpty(paramList)) {
            request.setToolParameter(new HashMap<>());
        } else {
            Map<String, Object> toolParameter = new HashMap<>();
            for (ToolParam param : paramList) {
                toolParameter.put(param.getName(), param.getValue());
            }
            request.setToolParameter(toolParameter);
        }
        return JSONObject.parseObject(JSONObject.toJSONString(request));
    }

}
