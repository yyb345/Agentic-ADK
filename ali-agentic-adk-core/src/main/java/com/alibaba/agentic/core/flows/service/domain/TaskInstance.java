package com.alibaba.agentic.core.flows.service.domain;

import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.smart.framework.engine.model.instance.ProcessInstance;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/25 08:58
 */
@Data
@Accessors(chain = true)
public class TaskInstance {

    private String id;

    private ProcessInstance processInstance;

    private SystemContext systemContext;

    private Request request;

    private String activityId;

}
