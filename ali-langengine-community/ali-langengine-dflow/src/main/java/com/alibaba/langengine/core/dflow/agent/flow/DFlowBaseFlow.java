package com.alibaba.langengine.core.dflow.agent.flow;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.func.ValidClosure;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.DFlowConstructionException;
import com.alibaba.langengine.core.dflow.agent.DFlowBaseAgent;
import com.alibaba.langengine.core.dflow.agent.DFlowBaseAgent.InitParam;
import com.alibaba.langengine.core.memory.BaseChatMemory;

public abstract class DFlowBaseFlow implements ValidClosure {

    protected Map<String, DFlowBaseAgent> agents;
    protected List<String> tools;

    private BiConsumer<String,String> logger;

    private BaseChatMemory memory;

    public DFlowBaseFlow(Map<String, DFlowBaseAgent> agents, Map<String, Object> data) {
        this(agents,data, null);
    }
    public DFlowBaseFlow(Map<String, DFlowBaseAgent> agents,Map<String, Object> data, BaseChatMemory memory) {
        setAgents(agents);
        data.put("agents", agents);
        this.memory = memory;
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

    public void setLogger(BiConsumer<String,String> logger) {
        this.logger = logger;
    }

    protected void log(String sessionid,String log){
        if(logger != null){
            logger.accept(sessionid,log);
        }
    }

    public DFlow<String> execute(String inputText) throws DFlowConstructionException {
        InitParam input = new InitParam(inputText, UUID.randomUUID().toString());
        return DFlow.just(input)
            .flatMap((c,x)->execute(c, x)).id("PlanningFlowExecute");
    }
    public DFlow<String> execute(String inputText, String sessionId) throws DFlowConstructionException {
        InitParam input = new InitParam(inputText, sessionId);
        return DFlow.just(input)
            .flatMap((c,x)->execute(c, x)).id("PlanningFlowExecute");
    }

    public DFlowBaseAgent getAgent(String key) {
        return agents.get(key);
    }

    public void addAgent(String key, DFlowBaseAgent agent) {
        agents.put(key, agent);
    }

    public abstract DFlow<String> execute(ContextStack contextStack,InitParam inputText) throws DFlowConstructionException;

    public Map<String, DFlowBaseAgent> getAgents() {
        return agents;
    }

    public void setAgents(Map<String, DFlowBaseAgent> agents) {
        this.agents = agents;
    }


    public static enum PlanStepStatus {
        NOT_STARTED("not_started"),
        IN_PROGRESS("in_progress"),
        COMPLETED("completed"),
        BLOCKED("blocked");

        private final String value;

        PlanStepStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static List<String> getAllStatuses() {
            // Return a list of all possible step status values
            return Arrays.stream(PlanStepStatus.values())
                .map(PlanStepStatus::getValue)
                .collect(Collectors.toList());
        }

        public static List<String> getActiveStatuses() {
            // Return a list of values representing active statuses (not started or in progress)
            return Arrays.asList(NOT_STARTED.getValue(), IN_PROGRESS.getValue());
        }

        public static Map<String, String> getStatusMarks() {
            // Return a mapping of statuses to their marker symbols
            return new HashMap<String, String>() {{
                put(COMPLETED.getValue(), "[✓]");
                put(IN_PROGRESS.getValue(), "[→]");
                put(BLOCKED.getValue(), "[!]");
                put(NOT_STARTED.getValue(), "[ ]");
            }};
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
