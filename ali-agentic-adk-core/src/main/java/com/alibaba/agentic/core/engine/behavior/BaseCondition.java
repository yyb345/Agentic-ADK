package com.alibaba.agentic.core.engine.behavior;

import com.alibaba.agentic.core.executor.SystemContext;

public interface BaseCondition {

    Boolean eval(SystemContext systemContext);
}
