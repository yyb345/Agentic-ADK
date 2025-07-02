package com.alibaba.langengine.core.dflow.agent.tool;

import java.io.Closeable;
import java.io.IOException;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.InitEntry.Entry;
import com.alibaba.dflow.func.ValidClosure;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.DFlowConstructionException;
import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.dflow.agent.DFlowConversationAgentExecutor;
import com.alibaba.langengine.core.dflow.agent.tool.DFlowTool;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JedisBasedPullingJobDFlowTool extends StructuredTool implements DFlowTool, ValidClosure {
    private static final String TOOLPREFIX = "agenttool_";
    private static String CALLENTRYNAMEPREFIX = "agentTool_";

    private JedisPool jedisPool;

    public JedisBasedPullingJobDFlowTool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public String getCallEntryName(){
        return CALLENTRYNAMEPREFIX+getName();
    }

    public JobEntry pull(String sessionId) throws IOException {
        try(Jedis jedis = jedisPool.getResource()) {
            String d = jedis.rpop(TOOLPREFIX+getName()+sessionId);
            if(d != null){
                return JSON.parseObject(d, JobEntry.class);
            }
        }
        return null;
    }

    public void report(String traceId, String result) throws Exception {
        DFlow.call(new Entry(getCallEntryName()) , result, traceId);
    }

    public DFlow<ToolExecuteResult> dflowRun(ContextStack context, String toolInput) throws DFlowConstructionException {
        return DFlow.just(toolInput)
            .flatMap((contextStack, input) -> {
                try(Jedis jedis = jedisPool.getResource()) {
                    JobEntry jobEntry = new JobEntry();
                    jobEntry.setInput(input);
                    jobEntry.setFunctionName(getName());
                    jobEntry.setTraceId(contextStack.getId());
                    String job = JSON.toJSONString(jobEntry);
                    log.info("tool call {} id:{}",job, contextStack.getId());
                    jedis.lpush(TOOLPREFIX+ getName()+ contextStack.get(DFlowConversationAgentExecutor.SESSIONID),job );
                }
                return DFlow.fromCall(getCallEntryName()).id(getCallEntryName())
                    .map((c, s) -> {
                        ToolExecuteResult toolExecuteResult = new ToolExecuteResult();
                        toolExecuteResult.setOutput(s);
                        return toolExecuteResult;
                    }, ToolExecuteResult.class).id(getName()+"_tool_result");
            }, ToolExecuteResult.class).id(getName()+"_result");

    }

    @Override
    public ToolExecuteResult run(String s, ExecutionContext executionContext) {
        return null;
    }

    @Override
    public ToolExecuteResult execute(String s) {
        return null;
    }

    @Data
    public static class JobEntry{
        private String functionName;
        private String input;
        private String traceId;
    }
    public interface JedisPool {
        Jedis getResource();
    }
    public interface Jedis extends Closeable{
        String rpop(String s);

        void lpush(String s, String job);

        void set(String id, String input);
    }
}
