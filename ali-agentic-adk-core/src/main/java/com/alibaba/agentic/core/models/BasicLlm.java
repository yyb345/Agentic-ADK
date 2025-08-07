package com.alibaba.agentic.core.models;


import com.alibaba.agentic.core.engine.delegation.domain.LlmRequest;
import com.alibaba.agentic.core.engine.delegation.domain.LlmResponse;
import com.alibaba.agentic.core.executor.SystemContext;
import io.reactivex.rxjava3.core.Flowable;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/17 09:49
 */
public interface BasicLlm {


    // model or model-prefix
    String model();

    Flowable<LlmResponse> invoke(LlmRequest llmRequest, SystemContext systemContext);

}
