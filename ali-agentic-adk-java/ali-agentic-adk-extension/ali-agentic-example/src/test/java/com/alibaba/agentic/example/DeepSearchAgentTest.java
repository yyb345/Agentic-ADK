package com.alibaba.agentic.example;

import com.alibaba.agentic.core.engine.delegation.DelegationLlm;
import com.alibaba.agentic.core.engine.delegation.DelegationTool;
import com.alibaba.agentic.core.engine.delegation.domain.LlmRequest;
import com.alibaba.agentic.core.engine.delegation.domain.LlmResponse;
import com.alibaba.agentic.core.engine.node.FlowCanvas;
import com.alibaba.agentic.core.engine.node.FlowNode;
import com.alibaba.agentic.core.engine.node.sub.ConditionalContainer;
import com.alibaba.agentic.core.engine.node.sub.LlmFlowNode;
import com.alibaba.agentic.core.engine.node.sub.ToolFlowNode;
import com.alibaba.agentic.core.engine.utils.DelegationUtils;
import com.alibaba.agentic.core.executor.InvokeMode;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.models.DashScopeLlm;
import com.alibaba.agentic.core.runner.Runner;
import com.alibaba.agentic.core.tools.BaseTool;
import com.alibaba.agentic.core.tools.DashScopeTools;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
// import com.alibaba.langengine.jsonrepair.JsonSafeParser; // Commented out due to unavailable dependency
import groovy.lang.Tuple2;
import io.reactivex.rxjava3.core.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

/**
 * This is a code sample of deep search agent, which gives a quick start of Agentic-ADK.
 * And it can run and provide output in a few loop.
 * Now, if max count of loop reaches, the program cannot exit gracefully.
 * The optimization is warmly welcomed.
 *
 * @author baliang.smy
 * @date 2025/8/5 15:59
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class })
@ActiveProfiles("testing")
public class DeepSearchAgentTest {

    private static final int max = 15;

    static Map<String, Object> badRequests = new HashMap<>();
    static List<LlmRequest.Message> context = new ArrayList<>();
    // action, result
    //static Map<String, Object> stateMap = new HashMap<>();

    static NodeId preNode = null;
    static Action action = null;

    private static int count = 0;

    enum NodeId {
        deepSearchAgent,
        llmNode,
        search,
        evaluatorNode;
    }

    enum ResultKey {
        query,
        text;
    }

    enum Action {
        search,
        think,
        result
    }


    @Test
    public void deepSearch() {

        DeepSearchAgent deepSearchAgent = new DeepSearchAgent();
        Evaluator evaluator = new Evaluator();

        DelegationLlm.register(new MyLlm());
        DelegationTool.register(deepSearchAgent);
        DelegationLlm.register(evaluator);

        FlowCanvas flowCanvas = new FlowCanvas();

        ToolFlowNode deepSearchNode = new ToolFlowNode(null, deepSearchAgent);
        deepSearchNode.setId(NodeId.deepSearchAgent.name());


        FlowNode llmNode = new LlmFlowNode(new LlmRequest()
                .setModel("myLlm")
                .setModelName("qwen3-235b-a22b"))
                .setId(NodeId.llmNode.name())
                .setNext(deepSearchNode);

        FlowNode searchNode = new ToolFlowNode(
                List.of(),
                new DashScopeTools() {
                    @Override
                    public String name() { return "search"; }

                    @Override
                    public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                        String query = (String) DelegationUtils.getResultOfNode(systemContext, NodeId.deepSearchAgent.name(), "query");
                        args.put("prompt", query);
                        preNode = NodeId.search;
                        return super.run(args, systemContext).map(result -> {
                            context.add(new LlmRequest.Message("tool", (String) result.get("text")));
                            result.put(ResultKey.text.name(), result.get("text"));
                            result.put(ResultKey.query.name(), query);
                            return result;
                        });
                    }
                }
                        .setAppId("5845862de55340179393a57d78067365"))
                .setId(NodeId.search.name())
                .next(new LlmFlowNode(new LlmRequest()
                        .setModel("evaluator")
                        .setModelName("qwen3-235b-a22b")
                ).setId(NodeId.evaluatorNode.name())
                        .setNext(deepSearchNode));



        flowCanvas.setRoot(deepSearchNode
                .nextOnCondition(
                        List.of(
                                new ConditionalContainer() {
                                    @Override
                                    public Boolean eval(SystemContext systemContext) {
                                        return Action.think.equals(action);
                                    }
                                }.setFlowNode(llmNode),
                                new ConditionalContainer() {

                                    @Override
                                    public Boolean eval(SystemContext systemContext) {
                                        return Action.search.equals(action);
                                    }
                                }.setFlowNode(searchNode)
                        )
                ));


        Flowable<Result> flowable = new Runner().run(flowCanvas, new Request()
                .setInvokeMode(InvokeMode.SYNC)
                .setParam(Map.of("query", "如何快速赚到一个小目标")));
        flowable.blockingIterable().forEach(event -> System.out.println(String.format("run result: %s", event)));
        System.out.println("count:" + count);
    }

    /**
     * 测试简单问题的回答能力
     * 验证代理能够直接回答不需要搜索的简单问题
     */
    @Test
    public void testSimpleQuestionAnswering() {
        DeepSearchAgent deepSearchAgent = new DeepSearchAgent();
        MyLlm myLlm = new MyLlm();
        
        DelegationLlm.register(myLlm);
        DelegationTool.register(deepSearchAgent);

        FlowCanvas flowCanvas = new FlowCanvas();

        ToolFlowNode deepSearchNode = new ToolFlowNode(null, deepSearchAgent);
        deepSearchNode.setId(NodeId.deepSearchAgent.name());

        FlowNode llmNode = new LlmFlowNode(new LlmRequest()
                .setModel("myLlm")
                .setModelName("qwen3-235b-a22b"))
                .setId(NodeId.llmNode.name())
                .setNext(deepSearchNode);

        flowCanvas.setRoot(deepSearchNode
                .nextOnCondition(
                        List.of(
                                new ConditionalContainer() {
                                    @Override
                                    public Boolean eval(SystemContext systemContext) {
                                        return Action.think.equals(action);
                                    }
                                }.setFlowNode(llmNode)
                        )
                ));

        // 重置计数器和状态
        count = 0;
        preNode = null;
        action = null;
        context.clear();
        badRequests.clear();

        Flowable<Result> flowable = new Runner().run(flowCanvas, new Request()
                .setInvokeMode(InvokeMode.SYNC)
                .setParam(Map.of("query", "什么是人工智能?")));
        
        flowable.blockingIterable().forEach(event -> System.out.println(String.format("Simple question result: %s", event)));
        System.out.println("Simple question count:" + count);
    }

    /**
     * 测试搜索评估功能
     * 验证代理能够评估搜索结果的质量并记录不良请求
     */
    @Test
    public void testSearchEvaluation() {
        Evaluator evaluator = new Evaluator();
        DelegationLlm.register(evaluator);

        // 模拟搜索结果
        Map<String, Object> searchResult = new HashMap<>();
        searchResult.put("query", "人工智能定义");
        searchResult.put("text", "人工智能是计算机科学的一个分支，它企图了解智能的实质，并生产出一种新的能以人类智能相似的方式做出反应的智能机器。");

        // 创建系统上下文并设置搜索结果
        SystemContext systemContext = new SystemContext();
        // 这里需要模拟系统上下文的设置，实际测试中可能需要使用Mock框架

        // 重置计数器
        count = 0;
        
        LlmRequest llmRequest = new LlmRequest()
                .setModel("evaluator")
                .setModelName("qwen3-235b-a22b");

        // 测试评估器的调用
        Flowable<LlmResponse> responseFlowable = evaluator.invoke(llmRequest, systemContext);
        
        responseFlowable.blockingIterable().forEach(response -> {
            System.out.println("Evaluator response: " + response);
        });
        
        System.out.println("Bad requests recorded: " + badRequests.size());
    }

    /**
     * 测试解析LLM结果功能
     * 验证DeepSearchAgent能够正确解析来自LLM的不同类型响应
     */
    @Test
    public void testParseLlmResult() {
        DeepSearchAgent agent = new DeepSearchAgent();
        
        // 测试搜索动作解析
        String searchResponse = "<action-search>{\"query\": \"人工智能最新发展\"}</action-search>";
        Tuple2<Action, String> searchResult = agent.parseLlmResult(searchResponse);
        assert Action.search.equals(searchResult.getV1());
        assert "人工智能最新发展".equals(searchResult.getV2());

        // 测试思考动作解析
        String thinkResponse = "<action-reflect>需要进一步思考这个问题</action-reflect>";
        Tuple2<Action, String> thinkResult = agent.parseLlmResult(thinkResponse);
        assert Action.think.equals(thinkResult.getV1());

        // 测试直接结果解析
        String directResponse = "这是直接的答案";
        Tuple2<Action, String> directResult = agent.parseLlmResult(directResponse);
        assert Action.result.equals(directResult.getV1());
        assert "这是直接的答案".equals(directResult.getV2());
        
        System.out.println("Parse LLM result test passed");
    }

    /**
     * 测试循环限制功能
     * 验证代理在达到最大迭代次数时能正确抛出异常
     */
    @Test(expected = RuntimeException.class)
    public void testMaxIterationLimit() {
        // 设置计数器接近限制
        count = max;
        
        DeepSearchAgent agent = new DeepSearchAgent();
        MyLlm llm = new MyLlm();
        Evaluator evaluator = new Evaluator();
        
        // 尝试再次执行应该抛出异常
        SystemContext context = new SystemContext();
        agent.run(new HashMap<>(), context).blockingFirst();
    }

    // DeepSearch 主agent
    public static class DeepSearchAgent implements BaseTool {

        @Override
        public String name() {
            return "deepSearch";
        }

        Tuple2<Action, String> parseLlmResult(String text) {
            if (text.contains("<action-search>")) {
                text = text.replace("<action-search>","").replace("</action-search>", "");
                return Tuple2.tuple(Action.search, JSON.parseObject(text).getString("query"));
            }
            if (text.contains("<action-reflect>")) {
                return Tuple2.tuple(Action.think, text);
            }
            return Tuple2.tuple(Action.result, text);
        }

        @Override
        public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
            count++;
            if (count > max) {
                throw new RuntimeException("max count reached");
            }
            if (preNode == null) {
                action = Action.think;
                return Flowable.just(Collections.emptyMap());
            }
            if (NodeId.evaluatorNode.equals(preNode)) {
                action = Action.think;
                return Flowable.just(Collections.emptyMap());
            } else if (NodeId.llmNode.equals(preNode)) {
                return Flowable.fromCallable(() -> {
                    LlmResponse llmResponse = DelegationUtils.getResultOfNode(systemContext, NodeId.llmNode.name(), LlmResponse.class);
                    String text = llmResponse.getChoices().get(0).getText();
                    Tuple2<Action, String> tuple2 = parseLlmResult(text);
                    action = tuple2.getV1();
                    Map<String, Object> result = new HashMap<>();
                    if (Action.search.equals(action)) {
                        result.put(ResultKey.query.name(), tuple2.getV2());
                    }
                    result.put(ResultKey.text.name(), tuple2.getV2());
                    return result;
                });
            } else {
                throw new RuntimeException("invalid preNode");
            }

        }
    }


    public static class MyLlm extends DashScopeLlm {

        private String userQuery = null;

        private final String promptTemplate = "You are an advanced AI research agent. You are specialized in multistep reasoning. \n" +
                "Using your best knowledge, conversation with the user and lessons learned, answer the user question with absolute certainty.\n" +
                "\n" +
                "\n" +
                "You have conducted the following actions:\n" +
                "<context>\n" +
                "%s\n" +
                "</context>\n" +
                "\n" +
                "Based on the current context, you must choose one of the following actions or return the answer directly :\n" +
                "<actions>\n" +
                "\n" +
                "\n" +
                "<action-search>\n" +
                "- Use web search to find relevant information\n" +
                "- Build a search request based on the deep intention behind the original question and the expected answer format\n" +
                "- Always prefer a single search request, only add another request if the original question covers multiple aspects or elements and one query is not enough, each request focus on one specific aspect of the original question \n" +
                "- Avoid those unsuccessful search requests and queries:\n" +
                "<bad-requests>\n" +
                "%s\n" +
                "</bad-requests>\n" +
                "</action-search>\n" +
                "\n" +
                "\n" +
                "<action-reflect>\n" +
                "- Think slowly and planning lookahead. Examine <question>, <context>, previous conversation with users to identify knowledge gaps. \n" +
                "- Reflect the gaps and plan a list key clarifying questions that deeply related to the original question and lead to the answer\n" +
                "</action-reflect>\n" +
                "\n" +
                "\n" +
                "</actions>\n" +
                "\n" +
                "Think step by step, choose the action, then respond by matching the schema of that action. \n" +
                "\n";

        @Override
        public String model() {
            return "myLlm";
        }

        @Override
        public Flowable<LlmResponse> invoke(LlmRequest llmRequest, SystemContext systemContext) {
            count++;
            if (count > max) {
                throw new RuntimeException("max count reached");
            }
            Map<String, Object> result = DelegationUtils.getResultOfNode(systemContext, NodeId.deepSearchAgent.name(), Map.class);
            if (userQuery == null) {
                userQuery = (String) DelegationUtils.getRequestParameter(systemContext, "query");
            }
            // context, bad-requests
            String prompt = String.format(promptTemplate,
                    CollectionUtils.isEmpty( context) ? "": JSON.toJSONString(context),
                    MapUtils.isEmpty(badRequests) ? "": JSON.toJSONString(badRequests));
            llmRequest.setMessages(List.of(new LlmRequest.Message("system", prompt), new LlmRequest.Message("user", userQuery)));
            llmRequest.setExtraParams(Map.of("enable_thinking", false));
            preNode = NodeId.llmNode;
            return super.invoke(llmRequest, systemContext).doOnNext(response -> {
                context.add(new LlmRequest.Message("assistant", response.getChoices().get(0).getText()));
            });
        }
    }

    // 对search的结果进行评估，是否是回答了query
    public static class Evaluator extends DashScopeLlm {

        private String prompt = "You are an evaluator of answer definitiveness. Analyze if the given answer provides a definitive response or not.\n" +
                "\n" +
                "<rules>\n" +
                "First, if the answer is not a direct response to the question, it must return false.\n" +
                "\n" +
                "Definitiveness means providing a clear, confident response. The following approaches are considered definitive:\n" +
                "  1. Direct, clear statements that address the question\n" +
                "  2. Comprehensive answers that cover multiple perspectives or both sides of an issue\n" +
                "  3. Answers that acknowledge complexity while still providing substantive information\n" +
                "  4. Balanced explanations that present pros and cons or different viewpoints\n" +
                "\n" +
                "The following types of responses are NOT definitive and must return false:\n" +
                "  1. Expressions of personal uncertainty: \"I don't know\", \"not sure\", \"might be\", \"probably\"\n" +
                "  2. Lack of information statements: \"doesn't exist\", \"lack of information\", \"could not find\"\n" +
                "  3. Inability statements: \"I cannot provide\", \"I am unable to\", \"we cannot\"\n" +
                "  4. Negative statements that redirect: \"However, you can...\", \"Instead, try...\"\n" +
                "  5. Non-answers that suggest alternatives without addressing the original question\n" +
                "  \n" +
                "Note: A definitive answer can acknowledge legitimate complexity or present multiple viewpoints as long as it does so with confidence and provides substantive information directly addressing the question.\n" +
                "</rules>\n" +
                "\n" +
                "<examples>\n" +
                "Question: \"What are the system requirements for running Python 3.9?\"\n" +
                "Answer: \"I'm not entirely sure, but I think you need a computer with some RAM.\"\n" +
                "Evaluation: {\n" +
                "  \"think\": \"The answer contains uncertainty markers like 'not entirely sure' and 'I think', making it non-definitive.\",\n" +
                "  \"pass\": false\n" +
                "}\n" +
                "\n" +
                "Question: \"What are the system requirements for running Python 3.9?\"\n" +
                "Answer: \"Python 3.9 requires Windows 7 or later, macOS 10.11 or later, or Linux.\"\n" +
                "Evaluation: {\n" +
                "  \"think\": \"The answer makes clear, definitive statements without uncertainty markers or ambiguity.\",\n" +
                "  \"pass\": true\n" +
                "}\n" +
                "\n" +
                "Question: \"Who will be the president of the United States in 2032?\"\n" +
                "Answer: \"I cannot predict the future, it depends on the election results.\"\n" +
                "Evaluation: {\n" +
                "  \"think\": \"The answer contains a statement of inability to predict the future, making it non-definitive.\",\n" +
                "  \"pass\": false\n" +
                "}\n" +
                "\n" +
                "Question: \"Who is the sales director at Company X?\"\n" +
                "Answer: \"I cannot provide the name of the sales director, but you can contact their sales team at sales@companyx.com\"\n" +
                "Evaluation: {\n" +
                "  \"think\": \"The answer starts with 'I cannot provide' and redirects to an alternative contact method instead of answering the original question.\",\n" +
                "  \"pass\": false\n" +
                "}\n" +
                "\n" +
                "\n" +
                "\n" +
                "Question: \"如何证明哥德巴赫猜想是正确的？\"\n" +
                "Answer: \"目前尚无完整证明，但2013年张益唐证明了存在无穷多对相差不超过7000万的素数，后来这个界被缩小到246。\"\n" +
                "Evaluation: {\n" +
                "  \"think\": \"The answer begins by stating no complete proof exists, which is a non-definitive response, and then shifts to discussing a related but different theorem about bounded gaps between primes.\",\n" +
                "  \"pass\": false\n" +
                "}\n" +
                "\n" +
                "Question: \"Wie kann man mathematisch beweisen, dass P ≠ NP ist?\"\n" +
                "Answer: \"Ein Beweis für P ≠ NP erfordert, dass man zeigt, dass mindestens ein NP-vollständiges Problem nicht in polynomieller Zeit lösbar ist. Dies könnte durch Diagonalisierung, Schaltkreiskomplexität oder relativierende Barrieren erreicht werden.\"\n" +
                "Evaluation: {\n" +
                "  \"think\": \"The answer provides concrete mathematical approaches to proving P ≠ NP without uncertainty markers, presenting definitive methods that could be used.\",\n" +
                "  \"pass\": true\n" +
                "}\n" +
                "\n" +
                "Question: \"Is universal healthcare a good policy?\"\n" +
                "Answer: \"Universal healthcare has both advantages and disadvantages. Proponents argue it provides coverage for all citizens, reduces administrative costs, and leads to better public health outcomes. Critics contend it may increase wait times, raise taxes, and potentially reduce innovation in medical treatments. Most developed nations have implemented some form of universal healthcare with varying structures and degrees of coverage.\"\n" +
                "Evaluation: {\n" +
                "  \"think\": \"The answer confidently presents both sides of the debate with specific points for each perspective. It provides substantive information directly addressing the question without expressions of personal uncertainty.\",\n" +
                "  \"pass\": true\n" +
                "}\n" +
                "\n" +
                "Question: \"Should companies use AI for hiring decisions?\"\n" +
                "Answer: \"There are compelling arguments on both sides of this issue. Companies using AI in hiring can benefit from reduced bias in initial screening, faster processing of large applicant pools, and potentially better matches based on skills assessment. However, these systems can also perpetuate historical biases in training data, may miss nuanced human qualities, and raise privacy concerns. The effectiveness depends on careful implementation, human oversight, and regular auditing of these systems.\"\n" +
                "Evaluation: {\n" +
                "  \"think\": \"The answer provides a balanced, detailed examination of both perspectives on AI in hiring. It acknowledges complexity while delivering substantive information with confidence.\",\n" +
                "  \"pass\": true\n" +
                "}\n" +
                "\n" +
                "Question: \"Is nuclear energy safe?\"\n" +
                "Answer: \"I'm not an expert on energy policy, so I can't really say if nuclear energy is safe or not. There have been some accidents but also many successful plants.\"\n" +
                "Evaluation: {\n" +
                "  \"think\": \"The answer contains explicit expressions of personal uncertainty ('I'm not an expert', 'I can't really say') and provides only vague information without substantive content.\",\n" +
                "  \"pass\": false\n" +
                "}\n" +
                "</examples>";

        @Override
        public String model() {
            return "evaluator";
        }

        @Override
        public Flowable<LlmResponse> invoke(LlmRequest llmRequest, SystemContext systemContext) {
            count++;
            if (count > max) {
                throw new RuntimeException("max count reached");
            }
            Map<String, String> result = DelegationUtils.getResultOfNode(systemContext, NodeId.search.name(), Map.class);
            String userQuery = String.format("Question:\"%s\",Answer:\"%s\"", result.get(ResultKey.query.name()), result.get(ResultKey.text.name()));
            preNode = NodeId.evaluatorNode;
            llmRequest.setExtraParams(Map.of("enable_thinking", false));
            llmRequest.setMessages(List.of(new LlmRequest.Message("system", prompt), new LlmRequest.Message("user", userQuery)));
            return super.invoke(llmRequest, systemContext).doOnNext(llmResponse -> {
                Map<String, Object> paramMap =  JSON.parseObject(llmResponse.getChoices().get(0).getText(), Map.class);
                if (!Boolean.TRUE.equals(paramMap.get("pass"))) {
                    badRequests.put(result.get(ResultKey.query.name()), paramMap.get("think"));
                }
            });
        }
    }


}
