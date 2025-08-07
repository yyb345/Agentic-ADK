package com.alibaba.agentic.core.flows.service.domain;

import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.SystemContext;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/28 14:45
 */
@Data
@Accessors(chain = true)
public class AsyncRequest {

    private String taskId;
    private SystemContext systemContext;

    private Request request;
}
