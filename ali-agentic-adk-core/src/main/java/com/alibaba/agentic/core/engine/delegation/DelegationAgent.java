package com.alibaba.agentic.core.engine.delegation;

import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import io.reactivex.rxjava3.core.Flowable;
import org.springframework.stereotype.Component;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/17 10:13
 */
@Component
public class DelegationAgent extends FrameworkDelegationBase {


    @Override
    public Flowable<Result> invoke(SystemContext systemContext, Request request) throws Throwable {
        // TODO coordinate
        return null;
    }


}
