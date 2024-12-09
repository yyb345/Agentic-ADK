package com.alibaba.langengine.multiagent.config;

import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.delegation.JavaDelegation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockDelegation implements JavaDelegation {

    @Override
    public void execute(ExecutionContext executionContext) {
        log.info("MockDelegation execute");
    }
}
