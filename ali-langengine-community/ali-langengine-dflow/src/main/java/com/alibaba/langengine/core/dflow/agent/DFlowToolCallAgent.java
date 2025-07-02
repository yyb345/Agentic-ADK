package com.alibaba.langengine.core.dflow.agent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.DFlowConstructionException;
import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.dflow.agent.DFlowConversationAgentExecutor.RunnableSeq;
import com.alibaba.langengine.core.dflow.agent.formatter.HistoryInputFormatter;
import com.alibaba.langengine.core.dflow.agent.formatter.Qwen25ToolInputFormatter;
import com.alibaba.langengine.core.dflow.agent.formatter.ToolNameFormatter;
import com.alibaba.langengine.core.dflow.agent.outputparser.Qwen25FunctionOutputParser;
import com.alibaba.langengine.core.dflow.agent.tool.DFlowTool;
import com.alibaba.langengine.core.dflow.agent.tool.Terminate;
import com.alibaba.langengine.core.memory.BaseChatMemory;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.model.BaseLLM;
import com.alibaba.langengine.core.model.fastchat.runs.ToolCallFunction;
import com.alibaba.langengine.core.outputparser.BaseOutputParser;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.runnables.RunnableHashMap;
import com.alibaba.langengine.core.runnables.RunnableInput;
import com.alibaba.langengine.core.runnables.RunnableInputFormatter;
import com.alibaba.langengine.core.runnables.RunnableOutput;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DFlowToolCallAgent extends DFlowReActAgent {

    protected static final String TOOL_CALL_REQUIRED = "Tool calls required but none provided";
    private static final Logger logger = LoggerFactory.getLogger(DFlowToolCallAgent.class);

    protected String systemPrompt = "You are an agent that can execute tool calls";
    protected String nextStepPrompt = "If you want to stop interaction, use `terminate` tool/function call.";

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
    public void setNextStepPrompt(String nextStepPrompt) {
        this.nextStepPrompt = nextStepPrompt;
    }
    private Map<String, BaseTool> nameToToolMap = new HashMap<>();

    private List<BaseTool> getTools() {
        List<BaseTool> tools = nameToToolMap.values().stream()
            .collect(
                Collectors.toList());
        return tools;
    }

    public void addTools(List<BaseTool> tools) {
        for (BaseTool tool : tools) {
            nameToToolMap.put(tool.getName(), tool);
        }
    }

    @Data
    static class ToolDFlowCtx extends DFlowCtx {
        List<ToolCallFunction> toolsToolCalls;
    }

    protected ToolDFlowCtx getFromContext(ContextStack ctx) {
        return JSON.parseObject(ctx.get(DFLOW_CTX), ToolDFlowCtx.class);
    }

    public DFlowToolCallAgent(String name, BaseChatMemory memory, List<BaseTool> tools, BaseLLM llm) {
        this(name, memory,tools);
        setFunctionChoose(genQwen25Function(llm));
    }

    public DFlowToolCallAgent(String name, BaseChatMemory memory, List<BaseTool> tools) {
        super(name, memory);
        addTools(Arrays.asList(new Terminate()));
        addTools(tools);
    }
    Runnable<RunnableInput, RunnableOutput> chooseFunction;

    private Supplier<Runnable<RunnableInput, RunnableOutput>> chooseFunctionGetter;
    public void setFunctionChoose(Runnable<RunnableInput, RunnableOutput> runnable) {
        this.chooseFunction = runnable;
    }
    public void setFunctionChoose(Supplier<Runnable<RunnableInput, RunnableOutput>> runnable) {
        this.chooseFunctionGetter = runnable;
    }
    public Runnable<RunnableInput, RunnableOutput> getFunctionChoose() {
        if(chooseFunctionGetter != null){
            Runnable<RunnableInput, RunnableOutput> res = chooseFunctionGetter.get();
            if(res != null){
                return res;
            }
        }
        return chooseFunction;
    }

    @Override
    public DFlow<Boolean> think(ContextStack ctx) throws DFlowConstructionException {

        try {
            ToolDFlowCtx dctx = getFromContext(ctx);
            Map<String, Object> i = getFromContext(ctx).getInputs();
            RunnableHashMap input = new RunnableHashMap();
            input.putAll(i);
            input.put("tools", getTools());
            input.put("systemPrompt", systemPrompt);
            input.put("agentInstruction", getAgentInstruct(ctx));

            input.put("isStuck", ctx.get(IS_STUCK));
            if(nextStepPrompt != null){
                if("true".equals(ctx.get(IS_STUCK))) {
                    input.put("nextStepPrompt", stuckPrompt+"\n"+nextStepPrompt);
                }else{
                    input.put("nextStepPrompt", nextStepPrompt);
                }
            }

            updateMemoryValues(ctx.get(SESSIONID),input);

            String res = invokeFunc(getFunctionChoose(), ctx, input).toString();

            ToolCallResult result = JSON.parseObject(res, ToolCallResult.class);

            log(ctx.get(SESSIONID), "Msg(thinking):"+result.getContent());

            List<ToolCallFunction> toolcalls = result.getToolCalls();

            if(StringUtils.isNotBlank(result.getContent())) {
                addAIMessage(ctx, result.getContent());
            }

            if (toolcalls != null && toolcalls.size() > 0) {
                dctx.setToolsToolCalls(toolcalls);
                addAIMessage(ctx, toolcalls.stream().map(
                    x->"<tool_call>"+JSON.toJSONString(x)+"</tool_call>\n"
                ).collect(Collectors.joining("\n")));
                saveBack(ctx, dctx);
                return DFlow.just(true);
                //is Action
            } else {
                return DFlow.just(false);
            }
        } catch (Exception e) {
            logger.error("Error during step execution: " + e.getMessage());
            addAIMessage(ctx, "Error encountered while processing: " + e.getMessage());
            ToolDFlowCtx d = getFromContext(ctx);
            d.setState(AgentState.ERROR);
            saveBack(ctx, d);
            return DFlow.just(false);
        }
    }

    protected void addToolMessage(ContextStack ctx, String content) {
        memory.getChatMemory().addToolMessage(ctx.get(SESSIONID), content);
    }

    @Override
    public DFlow<String> act(ContextStack ctx) throws DFlowConstructionException {
        ToolDFlowCtx d = getFromContext(ctx);
        if (d.getToolsToolCalls() == null || d.getToolsToolCalls().isEmpty()) {
            List<BaseMessage> messages = getHistory(ctx);
            // Return last message content if no tool calls
            return DFlow.just(messages.isEmpty() ? "No content or commands to execute" :
                messages.get(messages.size() - 1).getContent());
        }

        DFlow[] toolCalls = new DFlow[d.getToolsToolCalls().size()];
        for (int i = 0; i < toolCalls.length; i++) {
            toolCalls[i] = executeTool(ctx, d.getToolsToolCalls().get(i))
                .map((c, x) -> {
                    addToolMessage(c, x.getOutput());
                    //log(c.get(SESSIONID), "toolResult:"+x.getOutput());
                    return x.getOutput();
                }).id("DFlowToolCallAgentExecuteTool" + i);
        }
        ctx.put("_tools_calling", d.getToolsToolCalls().stream().map(
            x -> x.getName() + " executed"
        ).collect(Collectors.joining("\n")));
        return DFlow.zip(toolCalls, null, (c, results) -> {
            return c.get("_tools_calling");
        }, new com.alibaba.fastjson.TypeReference<String>() {}).id("DFlowToolCallAgentExecuteTool");

    }

    protected DFlow<ToolExecuteResult> executeTool(ContextStack contextStack, ToolCallFunction action)
        throws DFlowConstructionException {

        BaseTool tool = nameToToolMap.get(action.getName());
        if (tool == null) {
            return DFlow.just(new ToolExecuteResult("Error: Unknown tool '" + action.getName() + "'"));
        }

        if (isSpecialTool(action.getName())) {
            ToolDFlowCtx s = getFromContext(contextStack);
            if(action.getArguments().contains("success")) {
                setStatus(contextStack,AgentState.FINISHED);
            }else{
                setStatus(contextStack, AgentState.TERMINATED);
            }
        }
        //log(contextStack.get(SESSIONID), "Executing tool: " + action.getName());

        DFlow<ToolExecuteResult> toolFuture;
        if (tool instanceof DFlowTool) {
            toolFuture = ((DFlowTool)tool).dflowRun(contextStack, action.getArguments())
                .onErrorReturn(e -> new ToolExecuteResult("调用工具失败"));
        } else {
            try {
                toolFuture = DFlow.just(
                    tool.run(action.getArguments(), null));
            }catch (Throwable e){
                toolFuture = DFlow.just(new ToolExecuteResult("调用工具失败"));
            }
        }
        return toolFuture;
    }

    protected boolean isSpecialTool(String name) {
        // Implement logic to check if tool name is in special tools list
        return "terminate".equals(name.toLowerCase());
    }

    public Runnable<RunnableInput, RunnableOutput> genQwen25Function(BaseLLM llm) {
        Runnable<RunnableInput, RunnableOutput> prompt;
        RunnableInputFormatter inputFormatter;
        Runnable model;
        BaseOutputParser outputParser;

        prompt = new PromptTemplate(DefaultPrompt.DEFAULT_PROMPT);

        HashMap<String, Function<Object, String>> inputFormatters = new HashMap<>();
        inputFormatters.put("tools", new Qwen25ToolInputFormatter());
        inputFormatters.put("history", new HistoryInputFormatter());
        inputFormatter = new RunnableInputFormatter(inputFormatters);

        model = llm;

        outputParser = new Qwen25FunctionOutputParser();
        return new RunnableSeq(Runnable.sequence(inputFormatter,prompt, model,outputParser));
    }

    @Data
    public static class ToolCallResult{
        private String content;
        private List<ToolCallFunction> toolCalls;
    }
    class DefaultPrompt {
        public static final String DEFAULT_PROMPT = "<|im_start|>system\n"
            + "{systemPrompt}\n"
            + "# Tools\n"
            + "\n"
            + "You may call one or more functions to assist with the user query.\n"
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
            + "{history}\n"
            + "<|im_start|>user\n{nextStepPrompt}<|im_end|>\n"
            + "<|im_start|>assistant";
    }
}

