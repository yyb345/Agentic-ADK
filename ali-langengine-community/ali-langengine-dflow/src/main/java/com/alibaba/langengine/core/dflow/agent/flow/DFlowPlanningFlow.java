package com.alibaba.langengine.core.dflow.agent.flow;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.func.ValidClosure;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.DFlowConstructionException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.dflow.agent.DFlowBaseAgent;
import com.alibaba.langengine.core.dflow.agent.DFlowBaseAgent.AgentState;
import com.alibaba.langengine.core.dflow.agent.DFlowBaseAgent.InitParam;
import com.alibaba.langengine.core.dflow.agent.DFlowConversationAgentExecutor.RunnableSeq;
import com.alibaba.langengine.core.dflow.agent.DFlowToolCallAgent.ToolCallResult;
import com.alibaba.langengine.core.dflow.agent.formatter.Qwen25ToolInputFormatter;
import com.alibaba.langengine.core.dflow.agent.outputparser.Qwen25FunctionOutputParser;
import com.alibaba.langengine.core.memory.BaseChatMemory;
import com.alibaba.langengine.core.memory.BaseMemory;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.messages.SystemMessage;
import com.alibaba.langengine.core.model.BaseLLM;
import com.alibaba.langengine.core.model.fastchat.runs.ToolCallFunction;
import com.alibaba.langengine.core.outputparser.BaseOutputParser;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.runnables.RunnableHashMap;
import com.alibaba.langengine.core.runnables.RunnableInput;
import com.alibaba.langengine.core.runnables.RunnableInputFormatter;
import com.alibaba.langengine.core.runnables.RunnableOutput;
import com.alibaba.langengine.core.tool.ToolExecuteResult;

import lombok.extern.slf4j.Slf4j;

import static com.alibaba.langengine.core.dflow.agent.DFlowBaseAgent.SESSIONID;
import static com.alibaba.langengine.core.dflow.agent.DFlowBaseAgent.invokeFunction;

@Slf4j
public class DFlowPlanningFlow extends DFlowBaseFlow {

    public static final String PLANNING_FLOW_RESULT = "planningFlowResult";
    private static final String CURRENT_STEP_INDEX = "currentStepIndex";
    public static final String TEMP_VAR = "tempVar";
    private static final String ACTIVE_PLAN_ID = "activePlanId";
    private BaseLLM llm;
    private DPlanningTool planningTool;
    private List<String> executorKeys;
    //private String activePlanId;
    //private Integer currentStepIndex;
    public DFlowPlanningFlow(String name,DFlowBaseAgent agent) {
        this(name,agent,null);

    }
    public DFlowPlanningFlow(String name,DFlowBaseAgent agent, BaseChatMemory memory) {
        this(new HashMap<String, DFlowBaseAgent>(){{this.put(name, agent);}}, new HashMap<>(),memory);
    }

    public DFlowPlanningFlow(Map<String, DFlowBaseAgent> agents, Map<String, Object> data, BaseChatMemory memory) {
        super(agents, data, memory);

        executorKeys = new ArrayList<>();

        if (data.containsKey("executors")) {
            this.executorKeys = (List<String>)data.remove("executors");
        }

        if (!data.containsKey("planning_tool")) {
            this.planningTool = new DPlanningTool();
        } else {
            this.planningTool = (DPlanningTool)data.get("planning_tool");
        }

        if (executorKeys.isEmpty()) {
            executorKeys.addAll(agents.keySet());
        }
    }

    public DFlowBaseAgent getExecutor(String stepType) {
        if (stepType != null && agents.containsKey(stepType)) {
            return agents.get(stepType);
        }

        for (String key : executorKeys) {
            if (agents.containsKey(key)) {
                return agents.get(key);
            }
        }
        throw new RuntimeException("agent not found");
    }
    @Override
    public DFlow<String> execute(ContextStack contextStack, InitParam request) throws DFlowConstructionException {
        try {
            contextStack.put(PLANNING_FLOW_RESULT, "");
            String activePlanId = contextStack.getId();
            contextStack.put(ACTIVE_PLAN_ID, activePlanId);
            contextStack.put(SESSIONID, request.getSessionId());
            if (request.getQuery() != null && !request.getQuery().isEmpty()) {
                createInitialPlan(contextStack, request.getQuery());

                if (!planningTool.getPlans(contextStack).containsKey(activePlanId)) {
                    log.error("Plan creation failed. Plan ID " + activePlanId + " not found in planning tool.");
                    return DFlow.just("Failed to create plan for: " + request.getQuery());
                }
                return executeUntilFinish(contextStack);

            } else {
                return executeUntilFinish(contextStack);
            }
        } catch (Exception e) {
            log.error("Error in PlanningFlow", e);
            return DFlow.just("Execution failed: " + e.getMessage());
        }
    }

    private DFlow<String> executeUntilFinish(ContextStack contextStack)
        throws DFlowConstructionException {
        String result = contextStack.get(PLANNING_FLOW_RESULT);

        Map.Entry<Integer, Map<String, String>> stepInfoEntry = getCurrentStepInfo(contextStack);
        Integer currentStepIndex = null;
        if (stepInfoEntry != null) {
            currentStepIndex = stepInfoEntry.getKey();
        }
        if (currentStepIndex == null) {
            String finalResult = finalizePlan(contextStack);
            result = result + "\n" + finalResult;
            log(contextStack.get(SESSIONID), "Msg(Final):"+finalResult);
            contextStack.put(PLANNING_FLOW_RESULT, result);
            return DFlow.just(finalResult);
        }
        contextStack.put(CURRENT_STEP_INDEX, currentStepIndex);

        Map<String, String> stepInfo = stepInfoEntry.getValue();

        String stepType = stepInfo != null ? stepInfo.get("type") : null;
        DFlowBaseAgent executor = getExecutor(stepType);
        return executeStep(contextStack, executor, stepInfo)
            .flatMap((c, stepResult) -> {
                String res = c.get(PLANNING_FLOW_RESULT);
                log(c.get(SESSIONID),"Msg(StepResult):"+stepResult);
                res = res + stepResult + "\n";
                c.put(PLANNING_FLOW_RESULT, res);
                if (DFlowBaseAgent.getStatus(c).equals(AgentState.TERMINATED)) {
                    return DFlow.just(res);
                } else {
                    return executeUntilFinish(c);
                }
            }).id("PlanningFlowExecuteStepLoop");
    }

    Runnable<RunnableInput, RunnableOutput> initPlanFunction;
    Runnable<RunnableInput, RunnableOutput> finalizePlanFunction;

    private Supplier<Runnable<RunnableInput, RunnableOutput>> initPlanFunctionGetter;
    private Supplier<Runnable<RunnableInput, RunnableOutput>> finalizePlanFunctionGetter;
    public void setInitPlanFunction(Supplier<Runnable<RunnableInput, RunnableOutput>> initPlanFunction) {
        this.initPlanFunctionGetter = initPlanFunction;
    }
    public void setFinalizePlanFunction(Supplier<Runnable<RunnableInput, RunnableOutput>> finalizePlanFunction) {
        this.finalizePlanFunctionGetter = finalizePlanFunction;
    }

    public void setInitPlanFunction(Runnable<RunnableInput, RunnableOutput> initPlanFunction) {
        this.initPlanFunction = initPlanFunction;
    }
    private Runnable<RunnableInput, RunnableOutput> getInitPlanFunction() {
        if(initPlanFunctionGetter != null){
            Runnable<RunnableInput, RunnableOutput> res = initPlanFunctionGetter.get();
            if(res != null){
                return res;
            }
        }
        if(initPlanFunction == null){
            setInitPlanFunction(genQwen25InitFunction(llm));
        }
        return initPlanFunction;
    }



    public void setFinalizePlanFunction(Runnable<RunnableInput, RunnableOutput> finalizePlanFunction) {
        this.finalizePlanFunction = finalizePlanFunction;
    }
    private Runnable<RunnableInput, RunnableOutput> getFinalizePlanFunction() {
        if(finalizePlanFunctionGetter != null){
            Runnable<RunnableInput, RunnableOutput> res = finalizePlanFunctionGetter.get();
            if(res != null){
                return res;
            }
        }
        if(finalizePlanFunction == null){
            setFinalizePlanFunction(genQwen25FinalizeFunction(llm));
        }
        return finalizePlanFunction;
    }

    public ToolExecuteResult createInitialPlan(ContextStack contextStack, String request)
        throws DFlowConstructionException {
        log.info("Creating initial plan with ID: " + getActivePlanId(contextStack));


        String userMessage = ( request);

        RunnableHashMap input = new RunnableHashMap();
        input.put("input", userMessage);
        input.put("tools", Arrays.asList(planningTool));

        String res = invokeFunc(getInitPlanFunction(), contextStack, input).toString();
        ToolCallResult result = JSON.parseObject(res, ToolCallResult.class);
        List<ToolCallFunction> toolcalls = result.getToolCalls();
        if (toolcalls != null && toolcalls.size() > 0) {
            for (ToolCallFunction toolCall : toolcalls) {
                if ("planning".equals(toolCall.getName())) {
                    String arguments = toolCall.getArguments();
                    try {
                        Map<String, Object> argumentsMap = JSON.parseObject(arguments,
                            new TypeReference<Map<String, Object>>() {});
                        argumentsMap.put("plan_id", getActivePlanId(contextStack));
                        return planningTool.dflowRun(contextStack, JSON.toJSONString(argumentsMap));
                        //log(contextStack.get(SESSIONID), "Plan created:"+result.getOutput());

                    } catch (Throwable e) {
                        log.error("Failed to parse tool arguments: " + arguments);
                    }
                }
            }
        }

        log.warn("Creating default plan");

        Map<String, Object> defaultArgumentMap = new HashMap<>();
        defaultArgumentMap.put("command", "create");
        defaultArgumentMap.put("plan_id", getActivePlanId(contextStack));
        defaultArgumentMap.put("title",
            "Plan for: " + request.substring(0, Math.min(request.length(), 50)) + (request.length() > 50 ? "..." : ""));
        defaultArgumentMap.put("steps", Arrays.asList("Analyze request", "Execute task", "Verify results"));
        return planningTool.dflowRun(contextStack, JSON.toJSONString(defaultArgumentMap));

        //log(contextStack.get(SESSIONID), "Default plan created:"+result.getOutput());

    }

    public Map.Entry<Integer, Map<String, String>> getCurrentStepInfo(ContextStack contextStack) {
        String activePlanId = getActivePlanId(contextStack);
        if (!planningTool.getPlans(contextStack).containsKey(activePlanId)) {
            log.error("Plan with ID " + activePlanId + " not found");
            return null;
        }

        try {
            Map planData = (Map)planningTool.getPlans(contextStack).get(activePlanId);
            List<String> steps = (List<String>)planData.getOrDefault("steps", new ArrayList<String>());
            List<String> stepStatuses = (List<String>)planData.getOrDefault("step_statuses", new ArrayList<String>());

            for (int i = 0; i < steps.size(); i++) {
                String status;
                if (i >= stepStatuses.size()) {
                    status = PlanStepStatus.NOT_STARTED.getValue();
                } else {
                    status = stepStatuses.get(i);
                }

                if (PlanStepStatus.getActiveStatuses().contains(status)) {
                    Map<String, String> stepInfo = new HashMap<>();
                    stepInfo.put("text", steps.get(i));

                    Pattern pattern = Pattern.compile("\\[([A-Z_]+)\\]");
                    Matcher matcher = pattern.matcher(steps.get(i));
                    if (matcher.find()) {
                        stepInfo.put("type", matcher.group(1).toLowerCase());
                    }

                    try {
                        final int index = i;
                        Map<String, Object> argsMap = new HashMap<String, Object>() {{
                            put("command", "mark_step");
                            put("plan_id", activePlanId);
                            put("step_index", index);
                            put("step_status", PlanStepStatus.IN_PROGRESS.getValue());
                        }};
                        planningTool.dflowRun(contextStack, JSON.toJSONString(argsMap));
                    } catch (Exception e) {
                        log.error("Error marking step as in_progress", e);
                        if (i < stepStatuses.size()) {
                            stepStatuses.set(i, PlanStepStatus.IN_PROGRESS.getValue());
                        } else {
                            while (stepStatuses.size() < i) {
                                stepStatuses.add(PlanStepStatus.NOT_STARTED.getValue());
                            }
                            stepStatuses.add(PlanStepStatus.IN_PROGRESS.getValue());
                        }
                        planData.put("step_statuses", stepStatuses);
                    }

                    return new AbstractMap.SimpleEntry<>(i, stepInfo);
                }
            }

            return null;

        } catch (Exception e) {
            log.error("Error finding current step index: " + e.getMessage());
            return null;
        }
    }

    protected String buildAgentInstruct(ContextStack contextStack, Map<String, String> stepInfo) {
        return stepInfo.getOrDefault("text", "Step " + contextStack.get(CURRENT_STEP_INDEX));
    }
    public DFlow<String> executeStep(ContextStack contextStack, DFlowBaseAgent executor, Map<String, String> stepInfo)
        throws DFlowConstructionException {
        try {
            String planStatus = getPlanText(contextStack);
            String stepText = buildAgentInstruct(contextStack, stepInfo);

            String stepPrompt = String.format(
                "CURRENT PLAN STATUS:\n%s\n\nYOUR CURRENT TASK:\nYou are now working on step %s: \"%s\"\n\nPlease "
                    + "execute this step using the appropriate tools. When you're done, provide a summary of what you"
                    + " accomplished.",
                planStatus, contextStack.get(CURRENT_STEP_INDEX), stepText
            );
            executor.setAgentInstruct(contextStack, stepText);

            return executor.run(stepPrompt,contextStack.get(SESSIONID)).map((c, result) -> {
                markStepCompleted(c);
                return result;
            }).id("DFlowPlanningFlowExecuteStep").onErrorReturn(c ->
                 "Error during step agent execution"
            );

        } catch (Exception e) {
            log.error("Error preparing execution context: " + e.getMessage());
            return DFlow.just("Error preparing execution context: " + e.getMessage());
        }
    }

    public void markStepCompleted(ContextStack contextStack) throws DFlowConstructionException {
        Integer currentStepIndex = contextStack.get(CURRENT_STEP_INDEX, Integer.class);
        if (currentStepIndex == null) {
            return;
        }

        String activePlanId = getActivePlanId(contextStack);
        try {
            Map<String, Object> argsMap = new HashMap<String, Object>() {{
                put("command", "mark_step");
                put("plan_id", activePlanId);
                put("step_index", currentStepIndex);
                put("step_status", PlanStepStatus.COMPLETED.getValue());
            }};
            ToolExecuteResult result = planningTool.dflowRun(contextStack, JSON.toJSONString(argsMap));
            log.info("Marked step " + currentStepIndex + " as completed in plan " + activePlanId+ "."+ result.getOutput());
        } catch (Exception e) {
            log.error("Failed to update plan status: " + e.getMessage());

            Map<String, Map<String, Object>> plans = planningTool.getPlans(contextStack);
            if (plans.containsKey(activePlanId)) {
                Map<String, Object> planData = plans.get(activePlanId);
                List<String> stepStatuses = (List<String>)planData.getOrDefault("step_statuses",
                    new ArrayList<String>());

                while (stepStatuses.size() <= currentStepIndex) {
                    stepStatuses.add(PlanStepStatus.NOT_STARTED.getValue());
                }

                stepStatuses.set(currentStepIndex, PlanStepStatus.COMPLETED.getValue());
                planData.put("step_statuses", stepStatuses);
            }
        }
    }

    public String getPlanText(ContextStack contextStack) {
        try {
            Map<String, Object> argsMap = new HashMap<String, Object>() {{
                put("command", "get");
                put("plan_id", getActivePlanId(contextStack));
            }};
            ToolExecuteResult result = planningTool.dflowRun(contextStack, JSON.toJSONString(argsMap));

            return result.getOutput() != null ? result.getOutput() : result.toString();
        } catch (Exception e) {
            log.error("Error getting plan: " + e.getMessage());
            return generatePlanTextFromStorage(contextStack);
        }
    }

    public String getActivePlanId(ContextStack contextStack) {
        return contextStack.get(ACTIVE_PLAN_ID);
    }

    public String generatePlanTextFromStorage(ContextStack contextStack) {
        String activePlanId = getActivePlanId(contextStack);
        try {

            Map<String, Map<String, Object>> plans = planningTool.getPlans(contextStack);
            if (!plans.containsKey(getActivePlanId(contextStack))) {
                return "Error: Plan with ID " + getActivePlanId(contextStack) + " not found";
            }

            Map<String, Object> planData = plans.get(activePlanId);
            String title = (String)planData.getOrDefault("title", "Untitled Plan");
            List<String> steps = (List<String>)planData.getOrDefault("steps", new ArrayList<String>());
            List<String> stepStatuses = (List<String>)planData.getOrDefault("step_statuses", new ArrayList<String>());
            List<String> stepNotes = (List<String>)planData.getOrDefault("step_notes", new ArrayList<String>());

            while (stepStatuses.size() < steps.size()) {
                stepStatuses.add(PlanStepStatus.NOT_STARTED.getValue());
            }
            while (stepNotes.size() < steps.size()) {
                stepNotes.add("");
            }

            Map<String, Integer> statusCounts = new HashMap<>();
            for (String status : PlanStepStatus.getAllStatuses()) {
                statusCounts.put(status, 0);
            }

            for (String status : stepStatuses) {
                statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
            }

            int completed = statusCounts.get(PlanStepStatus.COMPLETED.getValue());
            int total = steps.size();
            double progress = total > 0 ? (completed / (double)total) * 100 : 0;

            StringBuilder planText = new StringBuilder();
            planText.append("Plan: ").append(title).append(" (ID: ").append(activePlanId).append(")\n");

            for (int i = 0; i < planText.length() - 1; i++) {
                planText.append("=");
            }
            planText.append("\n\n");

            planText.append(String.format("Progress: %d/%d steps completed (%.1f%%)\n", completed, total, progress));
            planText.append(String.format("Status: %d completed, %d in progress, ",
                statusCounts.get(PlanStepStatus.COMPLETED.getValue()),
                statusCounts.get(PlanStepStatus.IN_PROGRESS.getValue())));
            planText.append(
                String.format("%d blocked, %d not started\n\n", statusCounts.get(PlanStepStatus.BLOCKED.getValue()),
                    statusCounts.get(PlanStepStatus.NOT_STARTED.getValue())));
            planText.append("Steps:\n");

            Map<String, String> statusMarks = PlanStepStatus.getStatusMarks();

            for (int i = 0; i < steps.size(); i++) {
                String step = steps.get(i);
                String status = stepStatuses.get(i);
                String notes = stepNotes.get(i);
                String statusMark = statusMarks.getOrDefault(status,
                    statusMarks.get(PlanStepStatus.NOT_STARTED.getValue()));

                planText.append(String.format("%d. %s %s\n", i, statusMark, step));
                if (!notes.isEmpty()) {
                    planText.append("   Notes: ").append(notes).append("\n");
                }
            }

            return planText.toString();
        } catch (Exception e) {
            log.error("Error generating plan text from storage: " + e.getMessage());
            return "Error: Unable to retrieve plan with ID " + activePlanId;
        }
    }

    public String finalizePlan(ContextStack c) throws DFlowConstructionException {
        String planText = getPlanText(c);
        try {
            HumanMessage userMessage = new HumanMessage(
                planText);
            RunnableHashMap input = new RunnableHashMap();
            input.put("input", userMessage);

            updateMemoryValues(c.get(SESSIONID), input);

            Object res = invokeFunc(getFinalizePlanFunction(), c, input);
            return res.toString();
        } catch (Exception e) {
            log.error("Error finalizing plan with LLM: " + e.getMessage());
            return "Plan completed. Error generating summary.";
        }
    }

    public DPlanningTool getPlanningTool() {
        return planningTool;
    }

    public void setPlanningTool(DPlanningTool planningTool) {
        this.planningTool = planningTool;
    }

    protected Object invokeFunc(Runnable<RunnableInput, RunnableOutput> func,
        ContextStack dctx,
        RunnableHashMap i) {
        return invokeFunction(func, dctx, i);
    }

    public Runnable<RunnableInput, RunnableOutput> genQwen25InitFunction(BaseLLM llm) {
        Runnable<RunnableInput, RunnableOutput> prompt;
        RunnableInputFormatter inputFormatter;
        Runnable model;
        BaseOutputParser outputParser;

        prompt = new PromptTemplate(DefaultPrompt.DEFAULT_INIT_PROMPT);

        HashMap<String, Function<Object, String>> inputFormatters = new HashMap<>();
        inputFormatters.put("tools", new Qwen25ToolInputFormatter());
        inputFormatter = new RunnableInputFormatter(inputFormatters);

        model = llm;

        outputParser = new Qwen25FunctionOutputParser();
        return new RunnableSeq(Runnable.sequence(inputFormatter,prompt, model,outputParser));
    }
    public Runnable<RunnableInput, RunnableOutput> genQwen25FinalizeFunction(BaseLLM llm) {
        Runnable<RunnableInput, RunnableOutput> prompt;
        RunnableInputFormatter inputFormatter;
        Runnable model;
        BaseOutputParser outputParser = new FinalOutputParser();

        prompt = new PromptTemplate(DefaultPrompt.DEFAULT_FINAL_PROMPT);

        HashMap<String, Function<Object, String>> inputFormatters = new HashMap<>();
        inputFormatter = new RunnableInputFormatter(inputFormatters);

        model = llm;

        return new RunnableSeq(Runnable.sequence(inputFormatter,prompt, model, outputParser));
    }
    class FinalOutputParser extends BaseOutputParser<String> {
        @Override
        public String parse(String s) {
            return "Plan completed:\n\n" + s;
        }
    }
    class DefaultPrompt {
        public static final String DEFAULT_INIT_PROMPT = "<|im_start|>system\n"
            + "You are a planning assistant. Create a concise, actionable plan with clear steps. "
            + "Focus on key milestones rather than detailed sub-steps. "
            + "Optimize for clarity and efficiency.\n"
            + "# Tools\n"
            + "\n"
            + "You must call one or more functions to assist with the user query.\n"
            + "\n"
            + "You are provided with function signatures within <tools></tools> XML tags:\n"
            + "<tools>\n"
            + "{tools}\n"
            + "</tools>\n"
            + "\n"
            + "For each function call, return a json object with function name and arguments within <tool_call></tool_call> XML tags:\n"
            + "<tool_call>\n"
            + "{\"name\": <function-name>, \"arguments\": <args-json-object>}\n"
            + "</tool_call><|im_end|>\n"
            + "<|im_start|>user"
            + "Create a reasonable plan with clear steps to accomplish the task: "
            + "{input}<|im_end|>\n"
            + "<|im_start|>assistant";

        public static final String DEFAULT_FINAL_PROMPT = "<|im_start|>system\n"
            + "You are a planning assistant. Your task is to summarize the completed plan."
            + "<|im_end|>\n"
            + "<|im_start|>user"
            + "The plan has been completed. Here is the final plan status:\n\n"
            + "{input}\n\n"
            + "Please provide a summary of what was accomplished and any final thoughts.<|im_end|>\n"
            + "<|im_start|>assistant";
    }
}