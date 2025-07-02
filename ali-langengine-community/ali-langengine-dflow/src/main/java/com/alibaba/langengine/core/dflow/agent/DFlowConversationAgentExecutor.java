package com.alibaba.langengine.core.dflow.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.InitEntry;
import com.alibaba.dflow.func.ValidClosure;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.DFlowConstructionException;
import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.agent.AgentFinish;
import com.alibaba.langengine.core.callback.BaseCallbackManager;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.dflow.agent.formatter.HistoryInputFormatter;
import com.alibaba.langengine.core.dflow.agent.formatter.ToolNameFormatter;
import com.alibaba.langengine.core.dflow.agent.tool.DFlowTool;
import com.alibaba.langengine.core.dflow.exception.AgentException;
import com.alibaba.langengine.core.memory.BaseChatMemory;
import com.alibaba.langengine.core.memory.BaseMemory;
import com.alibaba.langengine.core.memory.impl.ConversationBufferMemory;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.ChatMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.messages.MessageConverter;
import com.alibaba.langengine.core.messages.SystemMessage;
import com.alibaba.langengine.core.model.BaseLLM;
import com.alibaba.langengine.core.outputparser.BaseOutputParser;
import com.alibaba.langengine.core.prompt.PromptConverter;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.runnables.RunnableConfig;
import com.alibaba.langengine.core.runnables.RunnableHashMap;
import com.alibaba.langengine.core.runnables.RunnableInput;
import com.alibaba.langengine.core.runnables.RunnableInputFormatter;
import com.alibaba.langengine.core.runnables.RunnableOutput;
import com.alibaba.langengine.core.runnables.RunnableSequence;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;

import io.reactivex.functions.BiFunction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import static com.alibaba.langengine.core.dflow.agent.DefaultPrompt.TOOL_DESC;

public class DFlowConversationAgentExecutor implements ValidClosure {

    public static final String SESSIONID = "sessionId";

    @Data
    static class DFlowCtx{
        private Map<String, Object> inputs;
        private RunnableConfig cfg;
        private AgentAction currentAction;
        private List<AgentAction> actionHistory;
        private int limit;
    }

    private static final String DFLOW_CTX = "dflowctx";
    private BaseChatMemory memory;
    private Map<String, BaseTool> nameToToolMap = new HashMap<>();
    private boolean isChatML = false;
    public void setMemory(BaseChatMemory memory){
        setMemory(memory,isChatML);
    }
    public void setMemory(BaseChatMemory memory, boolean isChatML){
        this.memory = memory;

        //TODO auto set by prompt??
        //memory.setMemoryKey("chat_history");
    }

    private Runnable<RunnableInput, RunnableOutput> firstCallFunction;
    private Runnable<RunnableInput, RunnableOutput> secondCallFunction;

    public Runnable<RunnableInput, RunnableOutput> getFunction() {
        return firstCallFunction;
    }

    private Function<String,Runnable<RunnableInput, RunnableOutput>> runnableGetter;
    public void setFunction(Function<String,Runnable<RunnableInput, RunnableOutput>> runnableGetter){
        this.runnableGetter = runnableGetter;
    }

    public void setFunction(Runnable<RunnableInput, RunnableOutput> runnable) {
        this.firstCallFunction = runnable;
        this.secondCallFunction = runnable;
    }



    private String aiPrefix = "AI";

    public void setTools(List<BaseTool> tools) {
        setTools(tools, null, null);
    }
    public void setTools(List<BaseTool> tools, String toolsDescribe, String toolNames) {
        registerToolsCallback(tools);

        for (BaseTool tool : tools) {
            nameToToolMap.put(tool.getName(), tool);
        }
    }
    private BaseCallbackManager callbackManager;

    public BaseCallbackManager getCallbackManager() {
        return callbackManager;
    }
    protected void registerToolsCallback(java.util.List<BaseTool> tools){
        tools.stream().forEach(tool -> tool.setCallbackManager(getCallbackManager()));
    }
    @Data
    public static class AgentScratchPadExtractor {
        PromptTemplate promptTemplate = new PromptTemplate(
            "{log}"
                + "Observation: {observation}\n");

        public String extract(AgentAction agentAction) {
            HashMap<String, Object> input = new HashMap<>();
            input.put("log", agentAction.getLog());
            input.put("observation", agentAction.getObservation());
            return promptTemplate.format(input);
        }
    }

    public static String getDefaultPromptBaseOnLLM(BaseLLM llm, boolean needMemory, boolean isCH) {
        BaseMemory memory = needMemory ? new ConversationBufferMemory() : null;
        String prefix = llm.getStructuredChatAgentPrefixPrompt(memory, isCH);
        String suffix = llm.getStructuredChatAgentSuffixPrompt(memory, isCH);
        String formatInstructions = llm.getStructuredChatAgentInstructionsPrompt(memory, isCH);
        String template = String.join("\n\n", prefix, "{tools}", formatInstructions
            , suffix);
        return template;
    }

    private Runnable<RunnableInput, RunnableOutput> getFirstFunction() {
        if(runnableGetter != null) {
            Runnable<RunnableInput, RunnableOutput> result = runnableGetter.apply("first");
            if (result != null) {
                return result;
            }
        }
        return firstCallFunction;
    }
    private Runnable<RunnableInput, RunnableOutput> getSecondFunction() {
        if(runnableGetter != null) {
            Runnable<RunnableInput, RunnableOutput> result = runnableGetter.apply("second");
            if (result != null) {
                return result;
            }
        }
        return secondCallFunction;
    }

    /**
     * 模糊匹配
     *
     * @param toolName
     * @return
     */
    private String containActionName(String toolName) {
        for (Map.Entry<String, BaseTool> nameToToolEntry : nameToToolMap.entrySet()) {
            String key = nameToToolEntry.getKey();
            if (toolName.indexOf(key) >= 0) {
                return key;
            }
        }
        return null;
    }

    private List<BaseTool> getTools(){
        List<BaseTool> tools = nameToToolMap.values().stream()
            .collect(
            Collectors.toList());
        return tools;
    }
    protected Map<String, Object> initContext(String query) {

        Map<String, Object> input = new HashMap<>();
        input.put("ai_prefix", aiPrefix);
        input.put("input",query);
        return input;
    }
    InitEntry.Entry entry = null;
    public  String chat(String query) throws Exception {
        return chat(query, null);
    }

    public synchronized String chat(String query, String passthrough) throws Exception {
        if(entry == null){
            entry = init(null);
        }
        String traceId = UUID.randomUUID().toString();

        entry.call(JSON.toJSONString(new InitParam(query,passthrough)), traceId);
        return traceId;
    }

    public InitEntry.Entry init(BiFunction<ContextStack, ? super String, String> afterAction) throws DFlowConstructionException {
        DFlow all = afterAction == null ? DFlow.fromCall("DFlowConversationAgentStart").id("DFlowConversationAgentPredictStart")
            .flatMap((c, p) -> {
                return predict(JSON.parseObject(p, InitParam.class));
            }).id("DFlowConversationAgentPredictInner")
            : DFlow.fromCall("DFlowConversationAgentStart").id("DFlowConversationAgentPredictStart")
                .flatMap((c, p) -> {
                    return predict(JSON.parseObject(p, InitParam.class));
                }).id("DFlowConversationAgentPredictInner2")
                .map(afterAction).id("DFlowConversationAgentPredictAfter");
        return all.init().getEntry("DFlowConversationAgentStart");
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InitParam{
        private String query;
        private String sessionId;
    }

    public DFlow<String> predict(String query) throws DFlowConstructionException {
        return predict(new InitParam(query, null));
    }
    public DFlow<String> predict(String query,String sessionId) throws DFlowConstructionException {
        return predict(new InitParam(query, sessionId));
    }

    public DFlow<String> predict(InitParam query)
        throws DFlowConstructionException {

        return DFlow.just(query).id("DFlowConversationAgentInit")
            .flatMap((ctx,q)->{
                DFlowCtx ctx1 = new DFlowCtx();
                ctx1.setLimit(5);
                Map<String, Object> input = initContext(q.getQuery());
                ctx1.setInputs(input);
                RunnableConfig cfg = new RunnableConfig();
                Map<String, Object> meta = new HashMap<>();
                meta.put("stop", Arrays.asList("Observation:"));
                cfg.setMetadata(meta);
                ctx1.setCfg(cfg);
                ctx.put(DFLOW_CTX,ctx1);
                ctx.put(SESSIONID,q.getSessionId());
                return thinkUntilFinish(ctx, input, null, 5);
            }).id("DFlowConversationAgentPredictLoop")
            .map((ctx,r) ->
                {
                    Map<String, Object> inputs = ctx.get(DFLOW_CTX,DFlowCtx.class).getInputs();
                    return saveHistoryAndReturnResult(ctx.get(SESSIONID), inputs, r);
                }
            ).id("DFlowConversationAgentPredictSave");
    }

    DFlow<String> thinkUntilFinish(ContextStack dctx,Map<String, Object> inputs, Object action, int limit)
        throws AgentException, DFlowConstructionException {
        if (limit <= 0) {
            throw new AgentException("limit exceeded");
        }

        //需要调用llm看看是啥结果
        if (action == null) {
            DFlowCtx ctx = dctx.get(DFLOW_CTX,DFlowCtx.class);
            //调用前先更新一下memory
            updateMemoryValues(dctx.get(SESSIONID),inputs);
            RunnableHashMap i = new RunnableHashMap();
            i.putAll(inputs);
            i.put("tools",getTools());
            i.put("tool_names",getTools());

            Object a2 = invokeFunction(getFirstFunction(), dctx.getId()+dctx.getName() , i,ctx.getCfg());
            return thinkUntilFinish(dctx,inputs, a2, limit);
        }
        //解析
        String res = JSON.toJSONString(action);
        AgentAction agentAction = JSON.parseObject(res, AgentAction.class);
        if (agentAction != null && agentAction.getTool() != null) {
            DFlowCtx ctx = dctx.get(DFLOW_CTX,DFlowCtx.class);
            ctx.setCurrentAction(agentAction);
            dctx.put(DFLOW_CTX, ctx);
            DFlow<ToolExecuteResult> toolFuture
                = executeTool(dctx, agentAction);
            return toolFuture.flatMap((dflowctx,toolExecuteResult) -> {
                return executeToolAndContinue(dflowctx, toolExecuteResult);
                //+ "Thought: ");
            }).id("DFlowConversationAgentPredictExecuteTool");
            //is Action
        } else {
            AgentFinish agentFinish = JSON.parseObject(res, AgentFinish.class);
            if (agentFinish != null && agentFinish.getReturnValues() != null) {

                //记录AI返回结果
                //getMemory().getChatMemory().addAIMessage(agentFinish.getReturnValues().get("output").toString());
                return DFlow.just(agentFinish.getReturnValues().get("output").toString());
            } else {
                throw new AgentException("Not supported result:" + res);
            }
        }
    }

    private DFlow<String> executeToolAndContinue(ContextStack dflowctx,
        ToolExecuteResult toolExecuteResult) throws AgentException, DFlowConstructionException {
        if (toolExecuteResult == null) {
            throw new AgentException("tool result failed");
        }
        //对于工具返回结果也增加记忆
        //if (memory != null) {
        //    memory.getChatMemory().addToolMessage(toolExecuteResult.getOutput());
        //}
        if (toolExecuteResult.isInterrupted()) {
            return DFlow.just(toolExecuteResult.getOutput());
        }
        DFlowCtx ctx = dflowctx.get(DFLOW_CTX,DFlowCtx.class);
        updateMemoryValues(dflowctx.get(SESSIONID,String.class), ctx.getInputs());
        AgentAction agentActionToFill = ctx.getCurrentAction();
        List<AgentAction> actionhistory = ctx.getActionHistory();
        Map<String,Object> input = ctx.getInputs();
        agentActionToFill.setObservation(toolExecuteResult.getOutput());
        if(actionhistory == null){
            actionhistory = new ArrayList<>();
        }
        List<AgentAction> newhistory = new ArrayList<>();
        //input.put("action_history",actionhistory);
        //input.put("todo_action",agentActionToFill);
        //input.put("observation", toolExecuteResult.getOutput());

        newhistory.addAll(actionhistory);
        newhistory.add(agentActionToFill);
        input.put("agent_scratchpad",newhistory);

        ctx.setActionHistory(newhistory);
        ctx.setLimit(ctx.getLimit() - 1);
        dflowctx.put(DFLOW_CTX,ctx);

        RunnableHashMap i = new RunnableHashMap();
        i.putAll(input);
        i.put("tools",getTools());

        Object a2 = invokeFunction(getSecondFunction(), dflowctx.getId()+dflowctx.getName(),i,ctx.getCfg());
        return thinkUntilFinish(dflowctx, input, a2, ctx.getLimit());
    }

    private DFlow<ToolExecuteResult> executeTool(ContextStack ctx, AgentAction action) throws DFlowConstructionException {
        AgentAction agentAction = action;

        String toolName = containActionName(agentAction.getTool());
        if (StringUtils.isEmpty(toolName)) {
            throw new RuntimeException("tool not found");
        }
        BaseTool tool = nameToToolMap.get(toolName);

        DFlow<ToolExecuteResult> toolFuture;
        if( tool instanceof DFlowTool){
            toolFuture = ((DFlowTool)tool).dflowRun(ctx, agentAction.getToolInput());
        }else {
            toolFuture = DFlow.just(
                tool.run(agentAction.getToolInput(), null));
        }
        return toolFuture;
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
    protected String saveHistoryAndReturnResult(String sessionId,Map<String, Object> inputs, String output) {
        String input = String.valueOf(inputs.get("input"));
        if (memory != null) {
            memory.getChatMemory().addUserMessage(sessionId,input);
            memory.getChatMemory().addAIMessage(sessionId,output);
        }
        return output;
    }

    public Runnable<RunnableInput, RunnableOutput> genDefaultFunction(BaseLLM llm, String langchainTemplate, boolean isCH) {
        Runnable<RunnableInput, RunnableOutput> prompt;
        RunnableInputFormatter inputFormatter;
        Runnable model;
        BaseOutputParser outputParser;

        if(langchainTemplate == null){
            langchainTemplate = DefaultPrompt.DEFAULT_PROMPT;
        }
        prompt = new PromptTemplate(langchainTemplate);

        HashMap<String, Function<Object, String>> inputFormatters = new HashMap<>();
        inputFormatters.put("tool_names", new ToolNameFormatter());
        inputFormatters.put("tools", new ToolInputFormatter(llm, isCH));
        inputFormatters.put("history", new HistoryInputFormatter(memory, isCH));
        inputFormatters.put("agent_scratchpad",  new AgentScratchPadFormatter());
        inputFormatter = new RunnableInputFormatter(inputFormatters);

        model = llm;

        outputParser = llm.getStructuredChatOutputParser();
        return new RunnableSeq(Runnable.sequence(inputFormatter,prompt, model,outputParser));
    }

    public static Object invokeFunction(Runnable<RunnableInput, RunnableOutput> func, String chainId,RunnableInput runnableInput, RunnableConfig config) {
        //ExecutionContext 不能序列化，所以只能调用之前临时设置一下
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setChainInstanceId(chainId);
        config.setExecutionContext(executionContext);

        Object result = func.invoke(runnableInput,config);
        //清掉
        config.setExecutionContext(null);
        return result;
    }
    public static class RunnableSeq extends Runnable<RunnableInput, RunnableOutput>{

        RunnableSequence seq;
        public RunnableSeq(RunnableSequence sequence){
            this.seq = sequence;
        }

        @Override
        public RunnableOutput invoke(RunnableInput runnableInput, RunnableConfig config) {
            return seq.invoke(runnableInput,config);
        }

        @Override
        public RunnableOutput stream(RunnableInput runnableInput, RunnableConfig config,
            Consumer<Object> chunkConsumer) {
            return seq.stream(runnableInput,config,chunkConsumer);
        }
    }


    public static class AgentScratchPadFormatter implements Function<Object, String> {
        @Override
        public String apply(Object agentActions) {
            return ((List<AgentAction>)agentActions).stream()
                .map(y -> new AgentScratchPadExtractor().extract(y))
                .collect(Collectors.joining("\n"));
        }
    }

    public static class ToolInputFormatter implements Function<Object, String> {

        public static String genToolsString(List<BaseTool> tools, BaseLLM llm, boolean isCH) {
            String toolDesc = llm == null ? TOOL_DESC :
                llm.getToolDescriptionPrompt(null, isCH);
            List<String> toolStrings = new ArrayList<>();
            for (BaseTool tool : tools) {
                Map<String, Object> inputs = new HashMap<>();
                inputs.put("name_for_model", tool.getName());
                inputs.put("name_for_human", tool.getHumanName());
                inputs.put("description_for_model", tool.getDescription());
                String structSchema;
                if (tool instanceof StructuredTool) {
                    StructuredTool structuredTool = (StructuredTool)tool;
                    structSchema = structuredTool.formatStructSchema();
                } else {
                    structSchema = Pattern.compile("\\}").matcher(Pattern.compile("\\{")
                        .matcher(tool.getArgs().toString()).replaceAll("{{")).replaceAll("}}");
                }
                inputs.put("parameters", !StringUtils.isEmpty(structSchema) ? structSchema : "{}");
                String toolString = PromptConverter.replacePrompt(toolDesc, inputs);
                toolStrings.add(toolString);
            }
            String formattedTools = String.join("\n", toolStrings);
            return formattedTools;
        }

        private BaseLLM llm;
        private boolean isCh;
        public ToolInputFormatter(){}

        public ToolInputFormatter(BaseLLM llm, boolean isCH) {
            this.llm = llm;
            this.isCh = isCH;
        }

        public String apply(Object toolInput) {
            return genToolsString((List<BaseTool>)toolInput, llm, isCh);
        }
    }



}


class DefaultPrompt {

    public static final String TOOL_DESC = "{name_for_model}: Call this tool to interact with the {name_for_human} API. What is the {name_for_human} API useful for? {description_for_model} Parameters: {parameters} Format the arguments as a JSON object.\n";
    public static final String DEFAULT_PROMPT = "<|im_start|>system\n"
        + "You are a helpful assistant.<|im_end|>\n"
        + "{history}\n"
        + "<|im_start|>user\n"
        + "Answer the following questions as best you can. You have access to the following tools:\n"
        + "\n"
        + "{tools}\n"
        + "\n"
        + "Use the following format:\n"
        + "\n"
        + "Question: the input question you must answer\n"
        + "Thought: you should always think about what to do\n"
        + "Action: the action to take, should be one of [{tool_names}]\n"
        + "Action Input: the input to the action\n"
        + "Observation: the result of the action\n"
        + "... (this Thought/Action/Action Input/Observation can be repeated zero or more times)\n"
        + "Thought: I now know the final answer\n"
        + "Final Answer: the final answer to the original input question\n"
        + "\n"
        + "\n"
        + "Begin!\n"
        + "\n"
        + "\n"
        + "Question: {input}\n"
        + "<|im_end|>\n"
        + "<|im_start|>assistant\n"
        //+ "Question: {input}\n"
        + "{agent_scratchpad}\n";
        //+ "{actionhistory}\n"
        //+ "{todo_action}\n"
        //+ "{observation}";
}

