import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.func.SafeObject;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.DFlowConstructionException;
import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.dflow.util.CopyContextDFlowHelper;
import com.alibaba.langengine.core.dflow.agent.DFlowConversationAgentExecutor;
import com.alibaba.langengine.core.dflow.agent.DFlowDeepCrawlerAgent;
import com.alibaba.langengine.core.dflow.agent.DFlowDeepCrawlerAgent.CrawlerParam;
import com.alibaba.langengine.core.dflow.agent.DFlowToolCallAgent;
import com.alibaba.langengine.core.dflow.agent.flow.DFlowPlanningFlow;
import com.alibaba.langengine.core.memory.impl.ConversationBufferMemory;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.runnables.RunnableConfig;
import com.alibaba.langengine.core.runnables.RunnableInput;
import com.alibaba.langengine.core.runnables.RunnableOutput;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.StructuredParameter;
import com.alibaba.langengine.core.tool.StructuredSchema;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.model.dashscope.DashScopeLLM;

import org.jetbrains.annotations.NotNull;

public class LocalTest2 {
    public static void main(String args[])
        throws Exception {
        try {
            DFlow.globalInitForTest();
            System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");

//            testAgentTool();
            //testManus();
        testDeepCrawlerAgent();
//            testDefaultTool();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void lisen(){
        //开启本地http监听

    }

    private static void testAgentTool() throws Exception {
        CopyContextDFlowHelper helper = new CopyContextDFlowHelper("test", (ctx,p)->{
            return DFlow.just(p)
                .map((c,x)->{
                    System.out.println("startInner"+c.getId()+c.get("x"));
                    return x;
                }).id("a4");
        });
        DFlow.directRun("a", (y)->{
            return DFlow.just(y)
                .map((c,x)->{
                    c.put("x",x.getParam());
                    System.out.println("start"+c.getId());
                    return x.getParam();
                }).id("a1").flatMap((a,b)->{
                    return helper.run(a, "");
                }).id("a2").map((c,b)->{
                    System.out.println("end"+c.getId()+c.get("x"));
                    return b;
                }).id("id3");
        });
    }

    private static void testDeepCrawlerAgent() throws Exception {
        DashScopeLLM llm = new DashScopeLLM();
        llm.setModel("qwen-plus-latest");
        llm.setToken("sk-x");

        ConversationBufferMemory memory = new ConversationBufferMemory();

        //        DFlowManusAgent agent = new DFlowManusAgent(memory);
        DFlowDeepCrawlerAgent agent = new DFlowDeepCrawlerAgent("manus",memory,llm);
        agent.setDflowCrawler((ctx,url)->{return new MockCrawler().run(url);});
        CrawlerParam param = new CrawlerParam();
        param.setPrompt("找到最便宜的4090");
        param.setUrl("https://s.taobao.com/search?q=4090");

        DFlow.directRun(DFlow.InitParam.of(JSON.toJSONString(param)),(y)->DFlow.just(y)
            .flatMap(x -> {
                System.out.println("====" + x);
                return agent.run(x.getParam());
            }).id("a1")
        );

        //等
        System.in.read();
    }


    private static void testManus() throws Exception {
        DashScopeLLM llm = new DashScopeLLM();
        llm.setModel("qwen-plus-latest");
        llm.setToken("sk-x");

        ConversationBufferMemory memory = new ConversationBufferMemory();

//        DFlowManusAgent agent = new DFlowManusAgent(memory);
        DFlowToolCallAgent agent = new DFlowToolCallAgent("manus",memory, getBaseTools(),llm);
        agent.setSystemPrompt("你是 一个搭配助手,帮用户搭配穿衣");
        agent.setNextStepPrompt("你可以用GoogleSearch 搜索信息. 用dapei工具给用户出搭配建议，用chuan工具真正的穿上\n"
            + "\n"
            + "GoogleSearch: Perform web information retrieval\n"
            + "\n"
            + "dapai: 出搭配方案\n chuan：真的穿上"
            + "\n"
            + "Terminate: End the current interaction when the task is complete or when you need additional information from the user. Use this tool to signal that you've finished addressing the user's request or need clarification before proceeding further.\n"
            + "\n"
            + "Based on user needs, proactively select the most appropriate tool or combination of tools. For complex tasks, you can break down the problem and use different tools step by step to solve it. After using each tool, clearly explain the execution results and suggest the next steps.\n"
            + "\n"
            + "Always maintain a helpful, informative tone throughout the interaction. If you encounter any limitations or need more details, clearly communicate this to the user before terminating.\n"
        );
        agent.setFunctionChoose(agent.genQwen25Function(llm));

        DFlowPlanningFlow planningFlow = new DFlowPlanningFlow("manus", agent);
        planningFlow.setFinalizePlanFunction(planningFlow.genQwen25FinalizeFunction(llm));
        planningFlow.setInitPlanFunction(planningFlow.genQwen25InitFunction(llm));

        DFlow.directRun(DFlow.InitParam.of("春天我应该怎么穿？"),(y)->DFlow.just(y)
            .flatMap(x -> {
                System.out.println("====" + x);
                return planningFlow.execute(x.getParam());
            }).id("a1")
            .flatMap(x -> {
                System.out.println("====" + x);
                return planningFlow.execute("休闲一点，在室内");
            }).id("a2")
            .map(x -> {
                System.out.println("====" + x);
                return x;
            }).id("a3"));
        //等
        System.in.read();
    }

    private static String mergeFunc(ContextStack contextStack, String[] strings) {
        return strings[0];
    }

    private static DFlow mapToParHost(int i) throws DFlowConstructionException {
        return DFlow.just(i).id("just" + i)
            .map(p -> {
                System.out.println("处理第" + p + "个节点");
                return "";
            }).id("childprocess");
    }

    private static DFlow<String> mapToSeqBatch(String p) throws DFlowConstructionException {
        return DFlow.just(p)
            .map(para -> "").id("dd")
            .onErrorReturn(c -> {
                //在此处理单任务失败
                throw new Exception("Failed finnally");
            });
    }

    public static void testDefaultTool() throws Exception {

        DashScopeLLM llm = new DashScopeLLM();
        llm.setModel("qwen-turbo-latest");
        llm.setToken("sk-x");
        List<BaseTool> baseTools = getBaseTools();
        ConversationBufferMemory memory = new ConversationBufferMemory();

        //memory.setMemoryKey("history");
        //memory.setAiPrefix("<|im_start|>assistant\n");
        //memory.setHumanPrefix("<|im_start|>user\n");
        //memory.getChatMemory().setMessageHistoryWrapper(new MessageHistoryWrapper(){
        //
        //    @Override
        //    public void modifyMessage(BaseMessage message) {
        //        if(message.getContent().startsWith("<|im_start|>")){
        //            return;
        //        }
        //        if(message instanceof HumanMessage) {
        //            message.setContent("<|im_start|>user\n"+message.getContent()+"\n<|im_end|>");
        //        } else if (message instanceof AIMessage) {
        //            message.setContent("<|im_start|>assistant\n"+message.getContent()+"\n<|im_end|>");
        //        } else if(message instanceof SystemMessage){
        //            message.setContent("<|im_start|>system\n"+message.getContent()+"\n<|im_end|>");
        //        } else if(message instanceof ChatMessage){
        //            message.setContent("<|im_start|>"+((ChatMessage)message).getRole()+"\n"+message.getContent()
        //            +"\n<|im_end|>");
        //        }
        //    }
        //});

        DFlowConversationAgentExecutor agentExecutor = new DFlowConversationAgentExecutor();
        agentExecutor.setFunction(new Runnable<RunnableInput, RunnableOutput>() {
            @Override
            public RunnableOutput invoke(RunnableInput runnableInput, RunnableConfig config) {
                return null;
            }

            @Override
            public RunnableOutput stream(RunnableInput runnableInput, RunnableConfig config,
                Consumer<Object> chunkConsumer) {
                return null;
            }
        });
        agentExecutor.setFunction(agentExecutor.genDefaultFunction(llm, null, false));
        agentExecutor.setTools(baseTools);
        //SKConversationAgentExecutor.genToolsString(baseTools,llm,false),
        //SKConversationAgentExecutor.genToolsNames(baseTools));

        agentExecutor.setMemory(memory);

        SafeObject<Boolean> cont = new SafeObject<>();
        cont.data = false;
        DFlow.fromCall("begin").id("a")
            .flatMap(x -> {
                System.out.println("====" + x);
                return agentExecutor.predict("春天我应该怎么穿？");
            }).id("a1")
            //.flatMap(x -> {
            //    System.out.println("====" + x);
            //    return agentExecutor.predict("休闲一点，在室内");
            //}).id("a2")
            //.flatMap(x -> {
            //    System.out.println("====" + x);
            //    return agentExecutor.predict("不错，可以给我穿上吗？");
            //}).id("a3")
            //.flatMap(x -> {
            //    System.out.println("====" + x);
            //    return agentExecutor.predict("夏天呢？");
            //}).id("a4")
            .init().getEntry("begin").call("1");

    }

    @NotNull
    private static List<BaseTool> getBaseTools() {
        StructuredTool tool = new TestTool();
        tool.setName("chuan");
        StructuredSchema schema = new StructuredSchema();
        List<StructuredParameter> params = new ArrayList<>();
        StructuredParameter p = new StructuredParameter();
        p.setName("上衣");
        p.setDescription("上衣id");
        params.add(p);
        p = new StructuredParameter();
        p.setName("下衣");
        p.setDescription("下衣id");
        params.add(p);
        schema.setParameters(params);
        tool.setStructuredSchema(schema);
        tool.setHumanName("穿衣工具");
        tool.setDescription("一个可以把搭配工具结果的具体id的衣服穿上的工具，调用前必须先获得搭配工具dapei的结果");
        StructuredTool tool2 = new TestTool();
        tool2.setName("dapei");
        StructuredSchema schema2 = new StructuredSchema();
        List<StructuredParameter> params2 = new ArrayList<>();
        StructuredParameter p2 = new StructuredParameter();
        p2.setName("衣服描述");
        p2.setDescription("穿搭描述");
        params2.add(p2);
        schema2.setParameters(params2);
        tool2.setStructuredSchema(schema2);
        tool2.setHumanName("搭配工具");
        tool2.setDescription("一个可以把为用户出具具体穿搭衣服id的工具,在穿搭前必须和用户问到比较详细的衣服描述");

        StructuredTool tool3 = new TestTool();
        tool3.setName("googlesearch");
        StructuredSchema schema3 = new StructuredSchema();
        List<StructuredParameter> params3 = new ArrayList<>();
        StructuredParameter p3 = new StructuredParameter();
        p3.setName("param");
        p3.setDescription("搜索词");
        params3.add(p3);
        schema3.setParameters(params3);
        tool3.setStructuredSchema(schema3);
        tool3.setHumanName("搜索工具");
        tool3.setDescription("可以互联网搜索的工具");

        List<BaseTool> baseTools = Arrays.asList(tool, tool2,new GoogleSearch());
        return baseTools;
    }
}

class TestTool extends StructuredTool {

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        ToolExecuteResult result = new ToolExecuteResult();
        System.out.println("tool input:"+toolInput);
        if (getName().equals("chuan")) {
            result.setOutput("穿上了工作服_蓝_宽松长袖衬衫_579797063");
        } else if (getName().equals("dapei")) {
            result.setOutput("上衣id：1， 下衣id：2");
        } else if(getName().equals("googlesearch")){
            result.setOutput("google搜索结果: 春天适合衬衫");
        }
        //result.setInterrupted(true);
        return result;
    }

    @Override
    public ToolExecuteResult execute(String toolInput) {
        return run(toolInput, null);
    }
}

