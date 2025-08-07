package com.alibaba.agentic.core.executor;

import io.reactivex.rxjava3.processors.FlowableProcessor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/4 17:47
 */
@Data
@Accessors(chain = true)
public class Request {

    private InvokeMode invokeMode = InvokeMode.SYNC;

    private FlowableProcessor<Map<String, Object>> processor;

    private Map<String, Object> param;
}
