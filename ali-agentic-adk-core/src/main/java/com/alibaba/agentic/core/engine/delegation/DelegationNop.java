package com.alibaba.agentic.core.engine.delegation;

import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DelegationNop extends FrameworkDelegationBase {

    @Override
    public Flowable<Result> invoke(SystemContext systemContext, Request request) throws Throwable {
        return Flowable.just(Result.success(null));
    }
}
