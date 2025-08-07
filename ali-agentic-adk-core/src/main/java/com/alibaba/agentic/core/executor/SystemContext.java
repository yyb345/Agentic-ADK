package com.alibaba.agentic.core.executor;

import io.reactivex.rxjava3.processors.FlowableProcessor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/28 09:52
 */
@Data
@Accessors(chain = true)
public class SystemContext {

    private Executor executor;

    private InvokeMode invokeMode;

    private FlowableProcessor<String> processor;

    private Map<String, Object> requestParameter;

    // activityId -> result
    private Map<String, Map<String, Object>> interOutput = new HashMap<>();

}
