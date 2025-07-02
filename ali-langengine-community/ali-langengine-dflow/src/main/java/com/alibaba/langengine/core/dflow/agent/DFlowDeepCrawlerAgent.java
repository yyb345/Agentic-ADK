package com.alibaba.langengine.core.dflow.agent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.DFlowConstructionException;
import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.dflow.agent.DFlowConversationAgentExecutor.RunnableSeq;
import com.alibaba.langengine.core.dflow.agent.DFlowToolCallAgent.ToolDFlowCtx;
import com.alibaba.langengine.core.dflow.agent.formatter.HistoryInputFormatter;
import com.alibaba.langengine.core.dflow.agent.formatter.Qwen25ToolInputFormatter;
import com.alibaba.langengine.core.dflow.agent.outputparser.Qwen25FunctionOutputParser;
import com.alibaba.langengine.core.dflow.agent.outputparser.ResearchResultOutputParser;
import com.alibaba.langengine.core.dflow.agent.outputparser.ValidationResultOutputParser;
import com.alibaba.langengine.core.memory.BaseChatMemory;
import com.alibaba.langengine.core.model.BaseLLM;
import com.alibaba.langengine.core.outputparser.BaseOutputParser;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.runnables.RunnableHashMap;
import com.alibaba.langengine.core.runnables.RunnableInput;
import com.alibaba.langengine.core.runnables.RunnableInputFormatter;
import com.alibaba.langengine.core.runnables.RunnableOutput;

import lombok.Data;

public class DFlowDeepCrawlerAgent extends DFlowBaseAgent {

    public DFlowDeepCrawlerAgent(String name, BaseChatMemory memory, BaseLLM baseLLM) {
        super(name, memory);
        setResearchFunction(genResearchFunction(baseLLM));
        setValidationFunction(genValidationFunction(baseLLM));
        setSummaryFunction(genSummaryFunction(baseLLM));
    }
    public DFlowDeepCrawlerAgent(String name, BaseChatMemory memory) {
        super(name, memory);
    }

    Runnable<RunnableInput, RunnableOutput> researchFunction;

    private Supplier<Runnable<RunnableInput, RunnableOutput>> researchFunctionGetter;

    public void setResearchFunction(Supplier<Runnable<RunnableInput, RunnableOutput>> researchFunctionGetter) {
        this.researchFunctionGetter = researchFunctionGetter;
    }

    public void setResearchFunction(Runnable<RunnableInput, RunnableOutput> researchFunction) {
        this.researchFunction = researchFunction;
    }

    public Runnable<RunnableInput, RunnableOutput> getResearchFunction() {
        if (researchFunctionGetter != null) {
            return researchFunctionGetter.get();
        }
        return researchFunction;
    }

    Runnable<RunnableInput, RunnableOutput> validationFunction;

    private Supplier<Runnable<RunnableInput, RunnableOutput>> validationFunctionGetter;

    public void setValidationFunction(Supplier<Runnable<RunnableInput, RunnableOutput>> researchFunctionGetter) {
        this.validationFunctionGetter = researchFunctionGetter;
    }

    public void setValidationFunction(Runnable<RunnableInput, RunnableOutput> validationFunction) {
        this.validationFunction = validationFunction;
    }

    public Runnable<RunnableInput, RunnableOutput> getValidationFunction() {
        if (validationFunctionGetter != null) {
            return validationFunctionGetter.get();
        }
        return validationFunction;
    }

    Runnable<RunnableInput, RunnableOutput> summaryFunction;
    private Supplier<Runnable<RunnableInput, RunnableOutput>> summaryFunctionGetter;
    public void setSummaryFunction(Supplier<Runnable<RunnableInput, RunnableOutput>> summaryFunctionGetter) {
        this.summaryFunctionGetter = summaryFunctionGetter;
    }
    public void setSummaryFunction(Runnable<RunnableInput, RunnableOutput> summaryFunction) {
        this.summaryFunction = summaryFunction;
    }
    public Runnable<RunnableInput, RunnableOutput> getSummaryFunction() {
        if (summaryFunctionGetter != null) {
            return summaryFunctionGetter.get();
        }
        return summaryFunction;
    }
    private BiConsumer<String,String> logger;

    public void setLogger(BiConsumer<String,String> logger) {
        this.logger = logger;
    }
    protected void log(String sessionid,String log){
        if(logger != null){
            logger.accept(sessionid, log);
        }
    }
    private BiFunction<ContextStack, String, DFlow<String>> dflowCrawler;

    public void setDflowCrawler(BiFunction<ContextStack, String, DFlow<String>> dflowCrawler) {
        this.dflowCrawler = dflowCrawler;
    }

    public BiFunction<ContextStack, String, DFlow<String>> getDflowCrawler() {
        return dflowCrawler;
    }
    //====================

    @Data
    public static class CrawlerParam {
        private String prompt;
        private String url;
    }

    public DFlow<String> run(CrawlerParam query) throws DFlowConstructionException {
        return run(JSON.toJSONString(query));
    }

    public DFlow<String> run(CrawlerParam query,String sessionId) throws DFlowConstructionException {
        return run(JSON.toJSONString(query),sessionId);
    }

    @Data
    public static class CrawlerDFlowCtx extends DFlowCtx {
        private String job;
        private List<CrawlerParam> innerJobs;
        private String crawContent = "";
    }

    protected DFlowCtx initCtx(String query) {
        CrawlerParam crawlerParam = JSON.parseObject(query, CrawlerParam.class);
        CrawlerDFlowCtx ctx1 = new CrawlerDFlowCtx();
        initCtx(ctx1, crawlerParam.getPrompt());
        ctx1.setJob(crawlerParam.getPrompt());
        ctx1.setInnerJobs(Arrays.asList(crawlerParam));
        return ctx1;
    }

    protected CrawlerDFlowCtx getFromContext(ContextStack ctx) {
        return JSON.parseObject(ctx.get(DFLOW_CTX), CrawlerDFlowCtx.class);
    }

    @Data
    public static class ValidationResult {
        private boolean valid;
        private String reason;
        private String subQuery;
    }
    @Data
    public static class ResearchResult {
        private List<CrawlerParam> urls;
        private String think;
    }

    @Override
    protected DFlow<String> step(ContextStack ctx) throws DFlowConstructionException {

        return craw(ctx)
            .flatMap((c, content) -> {
                RunnableHashMap input = new RunnableHashMap();
                CrawlerDFlowCtx dctx = getFromContext(c);
                input.put("job", dctx.getJob());
                input.put("content", content);

                String res = invokeFunc(getValidationFunction(), c, input).toString();
                ValidationResult validationResult = JSON.parseObject(res, ValidationResult.class);

                log(c.get(SESSIONID),"Msg(检查):"+validationResult.getReason());

                if (validationResult.isValid()) {
                    dctx.getResults().add(getMiddleData(content, validationResult));
                    dctx.setState(AgentState.FINISHED);
                    saveBack(c, dctx);
                    return DFlow.just("任务完成");
                }
                return researchNext(c, content, dctx, validationResult).id("DFlowDeepCrawlerAgentResearch");
            }).id("DFlowDeepCrawlerAgentValidationAndResearch")
            .map((c, content) -> {
                RunnableHashMap input = new RunnableHashMap();
                CrawlerDFlowCtx dctx = getFromContext(c);
                if(getStatus(c) == AgentState.RUNNING && dctx.getCurrentStep() < maxSteps - 1){
                    return content;
                }
                input.put("job", dctx.getJob());
                input.put("content", dctx.getResults().stream().collect(Collectors.joining("\n\n\n")));
                String finalResult = invokeFunc(getSummaryFunction(), c, input).toString();
                setStatus(c, AgentState.FINISHED);
//                addAIMessage(c, finalResult);
                dctx.getResults().clear();
                dctx.getResults().add(finalResult);
                saveBack(c, dctx);
                return finalResult;
            }).id("DFlowDeepCrawlerAgentSummary");

    }

    protected DFlow<String> researchNext(ContextStack c, String content, CrawlerDFlowCtx dctx, ValidationResult validationResult) {
        RunnableHashMap input;
        input = new RunnableHashMap();
        input.put("job", dctx.getJob());
        input.put("content", content);
        input.put("subQuery", validationResult.getSubQuery());
        input.put("failReason", validationResult.getReason());
        String result = invokeFunc(getResearchFunction(), c, input).toString();
        ResearchResult researchResult = JSON.parseObject(result, ResearchResult.class);

        if(researchResult.getUrls() == null || researchResult.getUrls().isEmpty()){
            setStatus(c, AgentState.FINISHED);
            return DFlow.just(researchResult.getThink());
        }
        CrawlerDFlowCtx dctx1 = getFromContext(c);

        dctx1.setInnerJobs(researchResult.getUrls());
        dctx1.getResults().add(getMiddleData(content, validationResult));
        saveBack(c, dctx1);
        return DFlow.just(researchResult.getThink());
    }

    protected String getMiddleData(String content, ValidationResult validationResult) {
        return content;
    }

    protected DFlow<String> craw(ContextStack ctx) throws DFlowConstructionException {
        CrawlerDFlowCtx dctx = getFromContext(ctx);
        List<CrawlerParam> urls = dctx.getInnerJobs();
        if (urls == null || urls.isEmpty()) {
            return DFlow.just(dctx.getCrawContent());
        }
        CrawlerParam url = urls.get(0);
        log(ctx.get(SESSIONID), "Msg(请求页面):" + url.prompt);
        urls.remove(0);
        saveBack(ctx, dctx);

        return dflowCrawler.apply(ctx, JSON.toJSONString(url))
            .flatMap((c, x) -> {
                CrawlerDFlowCtx dctx1 = getFromContext(c);
                dctx1.setCrawContent(dctx1.getCrawContent() + "\n" + x);
                saveBack(c, dctx1);
                return craw(c);
            }).id("DFlowDeepCrawlerAgentCraw");
    }

    public Runnable<RunnableInput, RunnableOutput> genResearchFunction(BaseLLM llm) {
        Runnable<RunnableInput, RunnableOutput> prompt;
        Runnable model;
        BaseOutputParser outputParser;

        prompt = new PromptTemplate(DefaultPrompt.RESEARCH_PROMPT);

        model = llm;

        outputParser = new ResearchResultOutputParser();
        return new RunnableSeq(Runnable.sequence(prompt, model,outputParser));
    }
    public Runnable<RunnableInput, RunnableOutput> genValidationFunction(BaseLLM llm) {
        Runnable<RunnableInput, RunnableOutput> prompt;
        Runnable model;
        BaseOutputParser outputParser;

        prompt = new PromptTemplate(DefaultPrompt.VALIDATE_PROMPT);

        model = llm;

        outputParser = new ValidationResultOutputParser();
        return new RunnableSeq(Runnable.sequence(prompt, model,outputParser));
    }

    public Runnable<RunnableInput, RunnableOutput> genSummaryFunction(BaseLLM llm) {
        Runnable<RunnableInput, RunnableOutput> prompt;
        Runnable model;

        prompt = new PromptTemplate(DefaultPrompt.SUMMARY_PROMPT);

        model = llm;

        return new RunnableSeq(Runnable.sequence(prompt, model));
    }

    class DefaultPrompt {
        public static final String RESEARCH_PROMPT = "<|im_start|>system\n"
            + " You are a web analysis expert, according to the original job, scraped contents, the reason the contents is not enough, to find proper urls you need to explore more.\n"
            + " Original Job: {job}\n"
            + "\n"
            + "\n"
            + " Answer with json format:\n"
            + "```\n"
            + "{\n"
            + "\t\"urls\":[\n"
            + "\t    \"{\"prompt\":\"GetDetail\", \"url\":https://xx.com\"},\n"
            + "\t    \"{\"url\":https://bb.com/afs\"}\n"
            + "\t],\n"
            + "\t\"think\":\"I need to look into the detail to see whether it is right\"\n"
            + "}\n"
            + "```\n"
            + "<|im_end|>\n"
            + "<|im_start|>user\n"
            + " Related Contents:\n"
            + " {content}\n"
            + "\n"
            + " FailReason:\n"
            + " {failReason}\n"
            + "\n"
            + "<|im_end|>\n"
            + "<|im_start|>assistant\n";

        public static final String VALIDATE_PROMPT = "<|im_start|>system\n"
            + "Determine whether the scraped contents can completely match the job and do not need more information in urls.\n"
            + "\n"
            + "Original Job: {job}\n"
            + "\n"
            + "\n"
            + "Answer with json format:\n"
            + "```\n"
            + "{\n"
            + "\t\"valid\":false,\n"
            + "\t\"reason\":\"The page need login to get more info\"\n"
            + "}\n"
            + "```\n"
            + "\n"
            + "\n"
            + "<|im_end|>\n"
            + "<|im_start|>user\n"
            + " Related Contents:\n"
            + " {content}\n"
            + "\n"
            + "<|im_end|>\n"
            + "<|im_start|>assistant\n";
        public static final String SUMMARY_PROMPT = "<|im_start|>system\n"
            + " You are a AI content analysis expert, good at summarizing content. Please summarize a specific and detailed answer or report based on the job.\n"
            + "\n"
            + " Original Job: {job}\n"
            + "\n"
            + "<|im_end|>\n"
            + "<|im_start|>user\n"
            + " Related Contents:\n"
            + " {content}\n"
            + "\n"
            + "<|im_end|>\n"
            + "<|im_start|>assistant\n"
            + " \n";
    }
}
