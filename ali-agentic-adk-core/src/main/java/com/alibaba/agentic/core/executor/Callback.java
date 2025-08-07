package com.alibaba.agentic.core.executor;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/8 11:41
 */
public interface Callback extends CallbackConfig {

    void execute(SystemContext systemContext, Request request, Result result, CallbackChain chain);

    void receive(SystemContext systemContext, Request request, Result result, CallbackChain chain);

}
