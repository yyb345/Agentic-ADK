package com.alibaba.langengine.core.dflow.agent;

import java.util.concurrent.CompletableFuture;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.DFlowConstructionException;
import com.alibaba.langengine.core.memory.BaseChatMemory;

public abstract class DFlowReActAgent extends DFlowBaseAgent {

    protected String systemPrompt;
    protected String nextStepPrompt;

    public DFlowReActAgent(String name, BaseChatMemory memory) {
        super(name, memory);
    }

    public abstract DFlow<Boolean> think(ContextStack c) throws DFlowConstructionException;

    public abstract DFlow<String> act(ContextStack c) throws DFlowConstructionException;

    @Override
    public DFlow<String> step(ContextStack ctx) throws DFlowConstructionException {
        try {
            return think(ctx)
                .flatMap((c,x) -> !x ?
                    DFlow.just("Thinking complete - no action needed")
                    : act(c)).id("DFlowReActAgentStep");
        } catch (Exception e) {
            return DFlow.just("Error during step execution: " + e.getMessage());
        }
    }
}
