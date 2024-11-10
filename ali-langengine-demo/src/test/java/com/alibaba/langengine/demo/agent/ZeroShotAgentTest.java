/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.demo.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.agent.AgentExecutor;
import com.alibaba.langengine.core.agent.mrkl.ZeroShotAgent;
import com.alibaba.langengine.dashscope.DashScopeModelName;
import com.alibaba.langengine.demo.agent.tool.SecSysTool;
import com.alibaba.langengine.core.callback.CallbackManager;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.callback.StdOutCallbackHandler;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.util.JacksonUtils;
import com.alibaba.langengine.dashscope.model.DashScopeLLM;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import com.alibaba.langengine.openai.tools.*;
import com.alibaba.langengine.tool.LLMMathTool;
import com.alibaba.langengine.tool.ToolLoaders;
import com.alibaba.langengine.tool.bing.WebSearchAPITool;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

/**
 * 根据工具的描述和请求内容的来决定使用哪个工具（最常用）
 * 仅根据工具的描述来确定使用哪个工具。要求为每个Tool提供一个描述，不限制Tool数量
 *
 * @author xiaoxuan.lp
 */
public class ZeroShotAgentTest {

    @Test
    public void test_openai_run() {
        // success
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(1024);

        //含 Bing搜索工具、数学计算工具、图片抠图工具 等等
        List<BaseTool> baseTools = new ArrayList<>();

        LLMMathTool llmMathTool = new LLMMathTool();
        baseTools.add(llmMathTool);

        WebSearchAPITool webSearchAPITool = new WebSearchAPITool();
        baseTools.add(webSearchAPITool);

//        CvUnetImageMattingTool cvUnetImageMattingTool = new CvUnetImageMattingTool();
//        baseTools.add(cvUnetImageMattingTool);

        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm, true);

//        String question = "Who founded alibaba? What is his current age raised to the 0.43 power?";
        String question = "阿里巴巴是谁创立的？他当前的岁数的 0.43 次方 是多少？";

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", question);
        Map<String, Object> response = agentExecutor.run(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_dashscopellm_run() {
        // success
        DashScopeLLM llm = new DashScopeLLM();
        llm.setModel(DashScopeModelName.QWEN_MAX);

        //含 Bing搜索工具、数学计算工具、图片抠图工具 等等
        List<BaseTool> baseTools = new ArrayList<>();

        LLMMathTool llmMathTool = new LLMMathTool();
        baseTools.add(llmMathTool);

        WebSearchAPITool webSearchAPITool = new WebSearchAPITool();
        baseTools.add(webSearchAPITool);

//        CvUnetImageMattingTool cvUnetImageMattingTool = new CvUnetImageMattingTool();
//        baseTools.add(cvUnetImageMattingTool);

        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm, true);

//        String question = "阿里巴巴是谁创立的？";
        String question = "阿里巴巴是谁创立的？他当前的岁数的 0.43 次方 是多少？";

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", question);
        Map<String, Object> response = agentExecutor.run(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test() {
        // success
        ChatOpenAI llm = new ChatOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);
        llm.setTemperature(0d);
        llm.setMaxTokens(1024);

        List<BaseTool> baseTools = ToolLoaders.loadLools(
            Arrays.asList(new String[] {"serpapi", "llm-math", "cv_unet_image-matting"}), llm);
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "Who is Leo DiCaprio's girlfriend? What is her current age raised to the 0.43 power?");
        //        inputs.put("input", "25 ^ 0.63 = ?");
        //        inputs.put("input", "帮我抠一张图，图片URL链接是https://sunleepy-test.oss-cn-zhangjiakou.aliyuncs.com/test/xiaoxuan2.png");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_serialize() throws JsonProcessingException {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);
        llm.setTemperature(0d);
        llm.setMaxTokens(1024);

        List<BaseTool> baseTools = ToolLoaders.loadLools(
            Arrays.asList(new String[] {"serpapi", "llm-math", "cv_unet_image-matting"}), llm);
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "Who is Leo DiCaprio's girlfriend? What is her current age raised to the 0.43 power?");

        String agentJson = agentExecutor.serialize();
        AgentExecutor newAgentExecutor = JacksonUtils.MAPPER.readValue(agentJson, AgentExecutor.class);
        System.out.println(newAgentExecutor);

        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println("response1:" + JSON.toJSONString(response));

        response = newAgentExecutor.call(inputs);
        System.out.println("response2:" + JSON.toJSONString(response));
    }

    @Test
    public void test_callbackManager() {
        CallbackManager callbackManager = new CallbackManager();
        callbackManager.addHandler(new StdOutCallbackHandler());

        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(1024);

        List<BaseTool> baseTools = ToolLoaders.loadLools(
            Arrays.asList(new String[] {"serpapi", "llm-math", "cv_unet_image-matting"}), llm);
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        agentExecutor.setCallbackManager(callbackManager);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "Who is Leo DiCaprio's girlfriend? What is her current age raised to the 0.43 power?");
        //        inputs.put("input", "25 ^ 0.63 = ?");
        //        inputs.put("input", "帮我抠一张图，图片URL链接是https://sunleepy-test.oss-cn-zhangjiakou.aliyuncs.com/test/xiaoxuan2.png");

        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setInputs(inputs);
        executionContext.setChain(agentExecutor);
        Map<String, Object> response = agentExecutor.run(inputs, executionContext, null, null);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_async() throws ExecutionException, InterruptedException {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = ToolLoaders.loadLools(
            Arrays.asList(new String[] {"serpapi", "llm-math", "cv_unet_image-matting"}), llm);
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        //        inputs.put("input", "Who is Leo DiCaprio's girlfriend? What is her current age raised to the 0.43
        //        power?");
        inputs.put("input",
            "帮我抠一张图，图片URL链接是https://sunleepy-test.oss-cn-zhangjiakou.aliyuncs.com/test/xiaoxuan2.png");
        CompletableFuture<Map<String, Object>> future = agentExecutor.callAsync(inputs);
        System.out.println(JSON.toJSONString(future.get()));
    }

    @Test
    public void test_errorCode() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = ToolLoaders.loadLools(Arrays.asList(new String[] {"error-res"}), llm);
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        //        inputs.put("input", "taobao.trade.fullinfo.get报错，具体错误码信息：{\"error_response\":{\"code\":15,
        //        \"msg\":\"Remote service error\",\"sub_code\":\"isp.trade-service-unavailable\",
        //        \"sub_msg\":\"交易服务不可用\",\"request_id\":\"15s0f1au5kmdd\"}}");
        inputs.put("input", "你是谁");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_tools() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setMaxTokens(2048);
        List<BaseTool> baseTools = new ArrayList();
        SecSysTool tool = new SecSysTool();
        //        tool.setLlm(llm);
        baseTools.add(tool);
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        ZeroShotAgent agent = new ZeroShotAgent();
        agent.setLlm(llm);
        agent.setTools(baseTools);
        agent.init(true);
        //        agentExecutor.setTools(baseTools);
        agentExecutor.setAgent(agent);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "查询案件信息,123456,并总结");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_CreateImageTool() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = new ArrayList<>();
        CreateImageTool createImageTool = new CreateImageTool(
            System.getenv("OPENAI_API_KEY"), 60000
        );
        createImageTool.setModel("dall-e-3");
        baseTools.add(createImageTool);
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "帮我生成一个小猫咪的图片");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_CreateImageVariationTool() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = new ArrayList<>();
        baseTools.add(new CreateImageTool());
        baseTools.add(new CreateImageVariationTool());
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input",
            "帮我把这个图片（https://img.alicdn.com/imgextra/i3/O1CN01Fs92fK2506eHPDoXm_!!6000000007463-2-tps-256-256"
                + ".png）变成另外一个相似的图片");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_CreateImageEditTool() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = new ArrayList<>();
        baseTools.add(new CreateImageTool());
        baseTools.add(new CreateImageVariationTool());
        baseTools.add(new CreateImageEditTool());
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input",
            "帮我把这张图片（https://img.alicdn.com/imgextra/i3/O1CN01Fs92fK2506eHPDoXm_!!6000000007463-2-tps-256-256"
                + ".png）加个蒙层图片（https://img.alicdn.com/imgextra/i1/O1CN01qDahCg1mNUAROEfEA_!!6000000004942-2-tps-256"
                + "-256.png），并生成一张带红色帽子的企鹅图片");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_AudioTranscriptionsTool() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = new ArrayList<>();
        baseTools.add(new CreateImageTool());
        baseTools.add(new CreateImageVariationTool());
        baseTools.add(new CreateImageEditTool());
        baseTools.add(new AudioTranscriptionTool());
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input",
            "帮我把这个语音文件（https://sunleepy-test.oss-cn-zhangjiakou.aliyuncs.com/test/demo.mp4）翻译成文本");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_ModerationTool() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = new ArrayList<>();
        baseTools.add(new CreateImageTool());
        baseTools.add(new CreateImageEditTool());
        baseTools.add(new ModerationTool());
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "帮我看下以下这句话（你好）的内容安全");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_MultiModalChineseStableDiffusionTool() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = new ArrayList<>();
        baseTools.add(new ModerationTool());
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "帮我生成一个小猫咪的图片");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_text2Speech() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = new ArrayList<>();
        Text2SpeechTool speechTool = new Text2SpeechTool(
            System.getenv("openai_api_key"), 10000
        );
        baseTools.add(speechTool);
        //baseTools.add(new MultiModalChineseStableDiffusionTool());
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "帮我把这段文字转换为语音：你好啊，你是小猪佩奇");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_OfaImageCaptionMugeBaseZhTool() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = new ArrayList<>();
        baseTools.add(new ModerationTool());
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input",
            "帮我把这张图片（https://img.alicdn.com/imgextra/i3/O1CN01Fs92fK2506eHPDoXm_!!6000000007463-2-tps-256-256"
                + ".png）配一段优美的文案");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_NlpPalm20TextGenerationCommodityChineseBaseTool() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = new ArrayList<>();
        baseTools.add(new ModerationTool());
//        baseTools.add(new NlpPalm20TextGenerationCommodityChineseBaseTool());
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "帮我通过关键词：垃圾桶，双层，可拆卸，加高，加高双层，把手，垃圾桶，内附，万向轮，生成一段商品文案");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_MplugImageCaptioningCocoBaseZhTool() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = new ArrayList<>();
        baseTools.add(new ModerationTool());
//        baseTools.add(new MplugImageCaptioningCocoBaseZhTool());
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input",
            "帮我把以下这句话（http://xingchen-data.oss-cn-zhangjiakou.aliyuncs"
                + ".com/maas/visual-question-answering/visual_question_answering.png）生成一句图片描述");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_NlpStructbertWordSegmentationChineseBaseTool() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = new ArrayList<>();
        baseTools.add(new ModerationTool());
//        baseTools.add(new NlpStructbertWordSegmentationChineseBaseTool());
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "帮我把以下这句话（阿里巴巴集团的使命是让天下没有难做的生意）进行中文分词");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_NlpConvaiText2sqlPretrainCnTool() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = new ArrayList<>();
        baseTools.add(new ModerationTool());
//        baseTools.add(new NlpConvaiText2sqlPretrainCnTool());
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input",
            "帮我分析下这个表格（https://sunleepy-test.oss-cn-zhangjiakou.aliyuncs.com/test/table1.json），上个月收益率超过3的有几个基金？");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_CvGooglenetPglVideoSummarizationTool() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = new ArrayList<>();
        baseTools.add(new ModerationTool());
//        baseTools.add(new CvGooglenetPglVideoSummarizationTool());
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input",
            "帮我分析下这个视频（https://sunleepy-test.oss-cn-zhangjiakou.aliyuncs.com/test/video_category_test_video"
                + ".mp4），提炼出重要信息");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_CvUnetPersonImageCartoonCompoundModelsTool() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = new ArrayList<>();
        baseTools.add(new ModerationTool());
//        baseTools.add(new CvUnetPersonImageCartoonCompoundModelsTool());
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input",
            "帮我把这张图片（https://modelscope.oss-cn-beijing.aliyuncs.com/demo/image-cartoon/cartoon.png）变成卡通图片");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_CvUnetPersonImageCartoon3dCompoundModelsTool() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = new ArrayList<>();
        baseTools.add(new ModerationTool());
//        baseTools.add(new CvUnetPersonImageCartoonCompoundModelsTool());
//        baseTools.add(new CvUnetPersonImageCartoon3dCompoundModelsTool());
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input",
            "帮我把这张图片（https://modelscope.oss-cn-beijing.aliyuncs.com/demo/image-cartoon/cartoon.png）变成3D卡通图片");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_CvGpenImagePortraitEnhancementTool() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = new ArrayList<>();
        baseTools.add(new ModerationTool());
//        baseTools.add(new CvGpenImagePortraitEnhancementTool());
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input",
            "帮我把这张老照片（https://sunleepy-test.oss-cn-zhangjiakou.aliyuncs.com/test/zhoulunfa.jpeg）修复成新照片");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_CvDdcolorImageColorizationTool() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = new ArrayList<>();
        baseTools.add(new ModerationTool());
//        baseTools.add(new CvGpenImagePortraitEnhancementTool());
//        baseTools.add(new CvDdcolorImageColorizationTool());
        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input",
            "帮我把这张照片（https://sunleepy-test.oss-cn-zhangjiakou.aliyuncs.com/test/black_and_white.jpeg）上个色吧。");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_dashscopellm_call() {
        DashScopeLLM llm = new DashScopeLLM();
        llm.setModel("qwen-7b-chat-v1");

        //含 Bing搜索工具、数学计算工具、图片抠图工具 等等
        List<BaseTool> baseTools = new ArrayList<>();

        LLMMathTool llmMathTool = new LLMMathTool();
        baseTools.add(llmMathTool);

        WebSearchAPITool webSearchAPITool = new WebSearchAPITool();
        baseTools.add(webSearchAPITool);

//        CvUnetImageMattingTool cvUnetImageMattingTool = new CvUnetImageMattingTool();
//        baseTools.add(cvUnetImageMattingTool);

        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);

        String question = "Who founded alibaba? What is his current age raised to the 0.43 power?";

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", question);
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }
}
