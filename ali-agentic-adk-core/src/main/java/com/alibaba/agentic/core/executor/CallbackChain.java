package com.alibaba.agentic.core.executor;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/8 11:16
 */
public interface CallbackChain {

    void execute(SystemContext systemContext, Request request, Result result);

    void receive(SystemContext systemContext, Request request, Result result);
}
