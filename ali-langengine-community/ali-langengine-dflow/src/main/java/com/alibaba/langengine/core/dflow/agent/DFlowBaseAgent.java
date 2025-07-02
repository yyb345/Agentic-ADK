package com.alibaba.langengine.core.dflow.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.func.ValidClosure;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.DFlowConstructionException;
import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.memory.BaseChatMemory;
import com.alibaba.langengine.core.memory.BaseChatMessageHistory;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.ToolMessage;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.runnables.RunnableConfig;
import com.alibaba.langengine.core.runnables.RunnableHashMap;
import com.alibaba.langengine.core.runnables.RunnableInput;
import com.alibaba.langengine.core.runnables.RunnableOutput;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public abstract class DFlowBaseAgent implements ValidClosure {

    public static final String SESSIONID = "sessionId";

    public static final String TOOLS = "agenttools";

    public static final String DFLOW_CTX = "dflowctx";
    public static final String IS_STUCK = "isStuck";
    private static final String AGENT_INSTRACTION = "agentInstruction";

    private BiConsumer<String,String> logger;


    // Core attributes
    private String name;
    private String description;

    protected String stuckPrompt
        = "Observed duplicate responses. Consider new strategies and avoid repeating ineffective paths already "
        + "attempted.";

    // Prompts
    private String systemPrompt;
    private String nextStepPrompt;

    protected BaseChatMemory memory;

    // Execution control
    public int maxSteps = 10;
    private int duplicateThreshold = 2;

    // Constructor
    public DFlowBaseAgent(String name, BaseChatMemory memory) {
        this.name = name;
        this.memory = memory;
    }

    public void setLogger(BiConsumer<String,String> logger){
        this.logger = logger;
    }

    protected void log(String sessionid,String log){
        if(logger != null){
            logger.accept(sessionid, log);
        }
    }

    protected void addAIMessage(ContextStack ctx, String content){
        memory.getChatMemory().addAIMessage(ctx.get(SESSIONID), content);
    }
    protected void addUserMessage(ContextStack ctx, String content){
        memory.getChatMemory().addUserMessage(ctx.get(SESSIONID), content);
    }

    protected void addAgentLevelToolMessages(ContextStack ctx, List<ToolMessage> tools){
        List<ToolMessage> all = JSON.parseArray(ctx.get(TOOLS), ToolMessage.class);
        if(all == null){
            all = new ArrayList<>();
        }
        all.addAll(tools);
        ctx.put(TOOLS, JSON.toJSONString(tools));
    }

    protected void addToolMessages(ContextStack ctx, String content){
        memory.getChatMemory().addToolMessage(ctx.get(SESSIONID), content);
    }

    protected List<BaseMessage> getHistory(ContextStack ctx){
        return memory.getChatMemory().getMessages(ctx.get(SESSIONID));
    }


    protected void updateMemoryValues(String sessionId,Map<String,Object> input){
        if (memory != null) {
            Map<String, Object> history = new HashMap<>();
            history = memory.loadMemoryVariables(sessionId,history);
            for (Entry<String, Object> entry : history.entrySet()) {
                input.put(entry.getKey(), entry.getValue().toString());
            }
            if(history.get(memory.getMemoryKey()) != null ) {
                input.put("history", (memory.getChatMemory().getMessages(sessionId)));
            }
        }else{
            input.put("history","");
        }
    }

    // Abstract method for step execution
    protected abstract DFlow<String> step(ContextStack ctx) throws DFlowConstructionException;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Method to update memory

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InitParam {
        private String query;
        private String sessionId;
    }

    @Data
    public static class DFlowCtx {
        private Map<String, Object> inputs;
        private int limit;
        private int currentStep = 0;
        private AgentState state = AgentState.IDLE;
        private List<String> results = new ArrayList<>();
    }

    protected DFlowCtx initCtx(String query){
        DFlowCtx ctx1 = new DFlowCtx();
        initCtx(ctx1,query);
        return ctx1;
    }

    protected void initCtx(DFlowCtx ctx1,String query){
        ctx1.setState(AgentState.RUNNING);
        ctx1.setLimit(maxSteps);
        Map<String, Object> input = initContext(query);
        ctx1.setInputs(input);
    }

    public void setAgentInstruct(ContextStack ctx, String value){
        ctx.put(AGENT_INSTRACTION, value);
    }
    public String getAgentInstruct(ContextStack ctx){
        return ctx.get(AGENT_INSTRACTION);
    }
    public static AgentState getStatus(ContextStack ctx){
        String c = ctx.get(DFLOW_CTX);
        if("null".equals(c) || c == null){
            return null;
        }
        return (JSON.parseObject(ctx.get(DFLOW_CTX), DFlowCtx.class)).getState();
    }
    protected DFlowCtx getFromContext(ContextStack ctx) {
        return JSON.parseObject(ctx.get(DFLOW_CTX), DFlowCtx.class);
    }

    protected void setStatus(ContextStack ctx, AgentState status){
        DFlowCtx c = getFromContext(ctx);
        c.setState(status);
        saveBack(ctx, c);
    }

    protected void saveBack(ContextStack ctx, DFlowCtx dctx) {
        ctx.put(DFLOW_CTX, JSON.toJSONString(dctx));
    }

    // Enum for agent states
    public enum AgentState {
        IDLE,
        RUNNING,
        FINISHED,
        TERMINATED,
        ERROR
    }

    public DFlow<String> run(String query) throws DFlowConstructionException {
        return run(new InitParam(query, null));
    }

    public DFlow<String> run(String query, String sessionId) throws DFlowConstructionException {
        return run(new InitParam(query, sessionId));
    }

    public DFlow<String> run(InitParam query)
        throws DFlowConstructionException {
        return DFlow.just(query).id("DFlowBaseAgentInit")
            .flatMap((ctx, q) -> {
                DFlowCtx ctx1 = initCtx(q.getQuery());
                log(q.getSessionId(), q.getQuery());
                ctx.put(SESSIONID, q.getSessionId());
                saveBack(ctx, ctx1);
                addUserMessage(ctx, q.getQuery());
                return stepUntilFinish(ctx);
            }).id("DFlowBaseAgentInitLoop");
    }

    protected DFlow<String> stepUntilFinish(ContextStack ctx)
        throws DFlowConstructionException {

        DFlowCtx dctx = getFromContext(ctx);

        if (!dctx.getState().equals(AgentState.RUNNING)) {
            return DFlow.just(String.join("\n", dctx.getResults()));
        }
        if (dctx.getCurrentStep() >= maxSteps) {
            List<String> results = dctx.getResults();
            results.add("Terminated: Reached max steps (" + maxSteps + ")");
            dctx.setState(AgentState.ERROR);
            saveBack(ctx, dctx);
            return DFlow.just(String.join("\n", results));
        }
        return step(ctx).id("DFlowBaseAgentStep")
            .flatMap((c, stepResult) -> {
                DFlowCtx dctxi = getFromContext(c);
                dctxi.setCurrentStep(dctxi.getCurrentStep() + 1);
                List<String> results = dctxi.getResults();
                results.add("Step " + dctxi.getCurrentStep() + ": " + stepResult);
                log(c.get(SESSIONID), "Msg(Step):" + dctxi.getCurrentStep() + ": " + stepResult);

                if (isStuck(c.get(SESSIONID))) {
                    handleStuckState(dctxi.getInputs());
                }

                dctxi.setResults(results);
                saveBack(c, dctxi);
                return stepUntilFinish(c);
            }).id("DFlowBaseAgentStepLoop")
            .onErrorReturn(c -> {
                DFlowCtx dctxi = getFromContext(c);
                dctxi.setState(AgentState.ERROR);
                dctxi.getResults().add("Error during step execution: " + c.getErrorMsg());
                saveBack(c, dctxi);
                return String.join("\n", dctxi.getResults());
            });

    }

    protected Object invokeFunc(Runnable<RunnableInput, RunnableOutput> func,
        ContextStack dctx,
        RunnableHashMap i) {
        return invokeFunction(func, dctx, i);
    }

    public static Object invokeFunction(Runnable<RunnableInput, RunnableOutput> func,
        ContextStack dctx,
        RunnableHashMap i) {
        RunnableConfig config = new RunnableConfig();
        //ExecutionContext 不能序列化，所以只能调用之前临时设置一下
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setChainInstanceId(dctx.getId());
        config.setExecutionContext(executionContext);

        Object result = func.invoke(i, config);
        //清掉
        config.setExecutionContext(null);
        return result;
    }

    protected Map<String, Object> initContext(String query) {
        Map<String, Object> input = new HashMap<>();
        input.put("input", query);
        return input;
    }

    protected String getQuery(DFlowCtx dctx) {
        return dctx.getInputs().get("input").toString();
    }

    // Check if the agent is stuck
    private boolean isStuck(String sessionId) {
        List<BaseMessage> messages = memory.getChatMemory().getMessages(sessionId);
        if (messages.size() < 2) {
            return false;
        }

        BaseMessage lastMessage = messages.get(messages.size() - 1);
        if (lastMessage.getContent() == null || lastMessage.getContent().isEmpty()) {
            return false;
        }

        int duplicateCount = 0;
        for (int i = messages.size() - 2; i >= 0; i--) {
            BaseMessage msg = messages.get(i);
            if (msg instanceof AIMessage && msg.getContent().equals(lastMessage.getContent())) {
                duplicateCount++;
            }
        }

        return duplicateCount >= duplicateThreshold;
    }

    // Handle stuck state
    private void handleStuckState(Map<String, Object> inputs) {
        inputs.put(IS_STUCK, "true");

    }

}