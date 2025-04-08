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
package com.alibaba.langengine.minimax.model;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.config.LangEngineConfiguration;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.model.BaseLLM;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.core.outputs.Generation;
import com.alibaba.langengine.core.outputs.LLMResult;
import com.alibaba.langengine.core.outputs.context.LlmResultHolder;

import com.alibaba.langengine.minimax.model.model.MiniMaxMessage;
import com.alibaba.langengine.minimax.model.model.MiniMaxParameters;
import com.alibaba.langengine.minimax.model.model.MiniMaxResult;
import com.alibaba.langengine.minimax.model.service.MinimaxService;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import static com.alibaba.langengine.minimax.MiniMaxConfiguration.*;


/**
 * @author aihe.ah qiongjin.wq
 * @time 2023/11/11
 * 功能说明：
 * 参考文档：https://api.minimax.chat/document/guides/chat?id=6433f37294878d408fc82953
 */
@Data
@Slf4j
public class MiniMaxLlm extends BaseLLM<ChatCompletionRequest> {

    private static final Pattern STREAM_DATA_PATTERN = Pattern.compile("data: (\\{.*\\})");

    /**
     * minmax 接口密钥
     */
    private String apiKey = MINIMAX_API_KEY;

    /**
     * groupId, 对应url参数
     */
    private String groupId = MINIMAX_GROUP_ID;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 角色名称
     */
    private String botName;

    /**
     * minmax 服务
     */
    private MinimaxService minimaxService;

    /**
     * 模型参数，对应body内容
     */
    private MiniMaxParameters miniMaxParameters;

    public MiniMaxLlm() {
        this.minimaxService = new MinimaxService("https://api.minimax.chat",
                Duration.ofSeconds(Long.parseLong(MINIMAX_API_TIMEOUT)), true, apiKey);
    }

    public MiniMaxLlm(String apiKey) {
        this.apiKey = apiKey;
        this.minimaxService = new MinimaxService("https://api.minimax.chat",
            Duration.ofSeconds(Long.parseLong(MINIMAX_API_TIMEOUT)), true, apiKey);
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        this.minimaxService = new MinimaxService("https://api.minimax.chat",
            Duration.ofSeconds(Long.parseLong(MINIMAX_API_TIMEOUT)), true, apiKey);
    }

    /**
     * 执行的中间流程一致，可以再抽一层
     *
     * @param prompts
     * @param stops
     * @param executionContext
     * @param consumer
     * @return
     */
    @Override
    public LLMResult generate(
        List<String> prompts,
        List<String> stops,
        ExecutionContext executionContext,
        Consumer<String> consumer,
        Map<String, Object> extraAttributes
    ) {
        executionContext = ensureExecutionContext(executionContext);

        try {
            this.onLlmStart(this, prompts, executionContext, consumer);
            LLMResult llmResult;
            if (executionContext.getLlmResult() != null) {
                llmResult = executionContext.getLlmResult();
            } else {
                String llmString = "";
                if (stops != null && stops.size() > 0) {
                    llmString = JSON.toJSONString(stops);
                }

                llmResult = new LLMResult();
                List<List<Generation>> generationsList = new ArrayList();
                if (LangEngineConfiguration.CurrentCache != null) {
                    for (int i = 0; i < prompts.size(); ++i) {
                        String prompt = (String)prompts.get(i);
                        List<Generation> cacheVal = LangEngineConfiguration.CurrentCache.get(executionContext, prompt, llmString);
                        if (cacheVal != null) {
                            generationsList.add(cacheVal);
                        }
                    }
                }

                if (generationsList.size() > 0) {
                    llmResult.setGenerations(generationsList);
                } else {
                    llmResult.setGenerations(generationsList);
                    List<Generation> generations = new ArrayList();
                    generationsList.add(generations);
                    Generation generation = new Generation();
                    generations.add(generation);
                    String prompt = prompts.get(0);
                    if (MapUtils.isEmpty(executionContext.getInputs())) {
                        HashMap<String, Object> inputs = new HashMap<>();
                        inputs.put("input", prompt);
                        inputs.put("history", new ArrayList<>());
                        executionContext.setInputs(inputs);
                    }
                    // 历史内容【不同处理流程，单独抽出抽象方法】
                    List<MiniMaxMessage> miniMaxMessages = processHistoryMessages(executionContext.getInputs());
                    MiniMaxParameters parameters = JSON.parseObject(JSON.toJSONString(miniMaxParameters),
                        MiniMaxParameters.class);
                    parameters.setMessages(miniMaxMessages);

                    String newPrompt = JSON.toJSONString(parameters);

                    String responseContent = this.run(newPrompt, stops, consumer, null);
                    generation.setText(responseContent);

                    // 获取并使用 MiniMaxResult
                    Map<String, Object> miniMaxResult = LlmResultHolder.getResult();
                    llmResult.setLlmOutput(miniMaxResult);
                    generation.setGenerationInfo(miniMaxResult);
                    if (LangEngineConfiguration.CurrentCache != null) {
                        LangEngineConfiguration.CurrentCache.update(executionContext, prompt, llmString, generations);
                    }
                }
            }

            this.onLlmEnd(this, prompts, llmResult, executionContext, consumer);
            return llmResult;
        } catch (Throwable var13) {
            this.onLlmError(this, prompts, var13, executionContext, consumer);
            throw var13;
        } finally {
            LlmResultHolder.clear();
        }
    }

    private ExecutionContext ensureExecutionContext(ExecutionContext executionContext) {
        if (executionContext == null) {
            executionContext = new ExecutionContext();
        }
        if (executionContext.getInputs() == null) {
            executionContext.setInputs(new HashMap<>());
        }
        return executionContext;
    }

    private List<MiniMaxMessage> processHistoryMessages(Map<String, Object> inputs) {

        if (CollectionUtils.isNotEmpty(miniMaxParameters.getMessages())) {
            List<MiniMaxMessage> messages = miniMaxParameters.getMessages();
            addCurrentInputToMessages(inputs, messages);
            // 过滤为空的内容
            messages = messages.stream().filter(message -> StringUtils.isNotEmpty(message.getText()))
                .collect(Collectors.toList());
            return messages;
        }

        List<BaseMessage> messageList = retrieveHistory(inputs);

        // 转换 BaseMessage 列表为 Message 列表
        List<MiniMaxMessage> miniMaxMessages = messageList.stream()
            .map(this::convertToMessage)
            // text不能为空
            .filter(message -> StringUtils.isNotEmpty(message.getText()))
            .collect(Collectors.toList());

        // 添加当前输入信息到消息列表
        addCurrentInputToMessages(inputs, miniMaxMessages);

        return miniMaxMessages;
    }

    private MiniMaxMessage convertToMessage(BaseMessage baseMessage) {
        String senderType = "human".equals(baseMessage.getType()) ? "USER" : "BOT";
        String senderName = "human".equals(baseMessage.getType()) ? userName : botName;
        return MiniMaxMessage.builder().text(baseMessage.getContent()).senderType(senderType).senderName(senderName)
            .build();
    }

    private void addCurrentInputToMessages(Map<String, Object> inputs, List<MiniMaxMessage> miniMaxMessages) {
        String currentInput = String.valueOf(inputs.get("input"));
        miniMaxMessages.add(MiniMaxMessage.builder().text(currentInput).senderType("USER").senderName(userName)
            .build());
    }

    private List<BaseMessage> retrieveHistory(Map<String, Object> inputs) {
        Object history = inputs.get("history");
        if (history instanceof String) {
            return JSON.parseArray((String)history, BaseMessage.class);
        } else if (history instanceof List) {
            return (List<BaseMessage>)history;
        }
        return new ArrayList<>();
    }

    /**
     * 运行MiniMax处理流程。
     *
     * @param prompt   输入提示
     * @param list     输入列表
     * @param consumer 处理结果的消费者
     * @return 处理结果
     */
    @Override
    public String run(String prompt, List<String> list, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        MiniMaxParameters parameters = parseParameters(prompt);
        parameters = applyDefaultValues(parameters);
        this.miniMaxParameters = parameters;
        MiniMaxResult result = processMinimax(parameters, consumer);
        storeResult(result);
        return result.getReply();
    }

    @Override
    public ChatCompletionRequest buildRequest(List<ChatMessage> chatMessages, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return null;
    }

    @Override
    public String runRequest(ChatCompletionRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return "";
    }

    @Override
    public String runRequestStream(ChatCompletionRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return "";
    }

    /**
     * 解析MiniMax参数。
     *
     * @param prompt 输入提示
     * @return 解析后的参数
     */
    private MiniMaxParameters parseParameters(String prompt) {
        try {
            return JSON.parseObject(prompt, MiniMaxParameters.class);
        } catch (Exception e) {
            MiniMaxParameters defaultParameters = createDefaultParameters(prompt);
            return defaultParameters;
        }
    }

    /**
     * 创建默认的MiniMax参数。
     *
     * @param prompt 输入提示
     * @return 默认参数
     */
    private MiniMaxParameters createDefaultParameters(String prompt) {
        MiniMaxParameters parameters = JSON.parseObject(JSON.toJSONString(miniMaxParameters), MiniMaxParameters.class);
        parameters.setMessages(Lists.newArrayList(
            MiniMaxMessage.builder().senderType("USER").senderName(userName).text(prompt).build()
        ));
        return parameters;
    }

    /**
     * 处理MiniMax业务逻辑。
     *
     * @param parameters 参数
     * @param consumer   结果消费者
     * @return 处理结果
     */
    private MiniMaxResult processMinimax(MiniMaxParameters parameters, Consumer<String> consumer) {
        if (BooleanUtils.isNotTrue(parameters.getStream())) {
            return minimaxService.createCompletion(getGroupId(), parameters);
        } else {
            return processStreamedMinimax(parameters, consumer);
        }
    }

    /**
     * 处理流式MiniMax业务逻辑。
     *
     * @param parameters 参数
     * @param consumer   结果消费者
     * @return 处理结果
     */
    private MiniMaxResult processStreamedMinimax(MiniMaxParameters parameters, Consumer<String> consumer) {
        List<String> answerContentList = new ArrayList<>();
        AtomicReference<MiniMaxResult> miniMaxResultRef = new AtomicReference<>(new MiniMaxResult());

        minimaxService.streamCompletion(getGroupId(), parameters)
            .doOnError(Throwable::printStackTrace)
            .blockingForEach(e -> {
                miniMaxResultRef.set(e);
                updateAnswerContentList(answerContentList, e);
                String text = safelyGetText(e);
                if (StringUtils.isNotEmpty(text)) {
                    notifyConsumer(consumer, answerContentList);
                }
            });

        MiniMaxResult miniMaxResult = miniMaxResultRef.get();
        return miniMaxResult;
    }

    /**
     * 更新答案内容列表。
     *
     * @param answerContentList 答案内容列表
     * @param result            结果
     */
    private void updateAnswerContentList(List<String> answerContentList, MiniMaxResult result) {
        String answer = result.getReply();
        // 修复下面的问题，如果为空，就打印对应的日志

        String text = safelyGetText(result);
        if (StringUtils.isEmpty(answer)) {
            answer = text;
        }
        if (StringUtils.isEmpty(answer)) {
            log.warn("MiniMax result is empty. {}", JSON.toJSONString(result));
            return;
        }

        if (isStop(result)) {
            answerContentList.clear();
            answerContentList.add(answer);
            return;
        }

        answerContentList.add(answer);
    }

    private static boolean isStop(MiniMaxResult result) {
        return Optional.ofNullable(result) // 包装Result对象
            .map(MiniMaxResult::getChoices) // 如果result不是null，应用Result::getChoices
            .filter(choices -> !choices.isEmpty()) // 确保choices列表不为空
            .map(choices -> choices.get(0)) // 获取第一个Choice对象
            .map(MiniMaxResult.Choice::getFinishReason) // 如果Choice对象不是null，应用Choice::getFinishReason
            .map(String::toLowerCase) // 转换为小写
            .filter("stop"::equals) // 检查是否包含"stop"
            .isPresent(); // 判断是否存在匹配的值
    }

    public static String safelyGetText(MiniMaxResult result) {
        return Optional.ofNullable(result) // 包装Result对象
            .map(MiniMaxResult::getChoices) // 如果result不是null，应用Result::getChoices
            .filter(choices -> !choices.isEmpty()) // 确保choices列表不为空
            .map(choices -> choices.get(0)) // 获取第一个Choice对象
            .map(MiniMaxResult.Choice::getMessages) // 如果Choice对象不是null，应用Choice::getMessages
            .filter(messages -> !messages.isEmpty()) // 确保messages列表不为空
            .map(messages -> messages.get(0)) // 获取第一个Message对象
            .map(MiniMaxMessage::getText) // 如果Message对象不是null，应用Message::getText
            .orElse(null); // 如果任何步骤中的值是null，则返回null
    }

    /**
     * 通知消费者处理结果。
     *
     * @param consumer          消费者
     * @param answerContentList 答案内容列表
     */
    private void notifyConsumer(Consumer<String> consumer, List<String> answerContentList) {
        String joinedAnswer = String.join("", answerContentList);
        if (joinedAnswer != null && consumer != null) {
            consumer.accept(joinedAnswer);
        }
    }

    /**
     * 存储结果。
     *
     * @param result 结果
     */
    private void storeResult(MiniMaxResult result) {
        LlmResultHolder.setResult(JSON.parseObject(JSON.toJSONString(result)));
    }

    public String executeWithApacheHttp(String urlString, String apiKey, String groupId, Object parameters) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(urlString + groupId);

            // Set headers.
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");

            // Set parameters.
            if (parameters != null) {
                StringEntity entity = new StringEntity(JSON.toJSONString(parameters), StandardCharsets.UTF_8);
                httpPost.setEntity(entity);
            }

            // Execute and get the response.
            try (CloseableHttpResponse response = client.execute(httpPost)) {
                System.out.println(response.getStatusLine());
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return EntityUtils.toString(entity, StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            System.out.println("Error occurred while sending POST request to URL: " + urlString);
            e.printStackTrace();
        }
        return null;
    }

    private MiniMaxParameters applyDefaultValues(
        MiniMaxParameters miniMaxParameters) {

        MiniMaxParameters parameters
            = new MiniMaxParameters();
        parameters.setModel("abab5.5-chat");
        parameters.setStream(false);
        parameters.setTokensToGenerate(1024L);
        parameters.setTemperature(0.9F);
        parameters.setTopP(0.95F);
        parameters.setMaskSensitiveInfo(false);
        //parameters.setReplyConstraints(
        //    MiniMaxParameters.ReplyConstraints.builder().senderType("BOT")
        //        .senderName(assistantDto.getName()).build());
        //parameters.setBotSetting(MiniMaxParameters.BotSetting.builder().botName("MM智能助理")
        //    .content("MM智能助理是一款由MiniMax自研的，没有调用其他产品的接口的大型语言模型。MiniMax是一家中国科技公司，一直致力于进行大模型相关的研究。").build());
        if (miniMaxParameters == null) {
            return parameters;
        }

        if (miniMaxParameters.getTopP() == null) {
            miniMaxParameters.setTopP(parameters.getTopP());
        }
        if (miniMaxParameters.getTokensToGenerate() == null) {
            miniMaxParameters.setTokensToGenerate(parameters.getTokensToGenerate());
        }
        if (miniMaxParameters.getStream() == null) {
            miniMaxParameters.setStream(parameters.getStream());
        }

        if (miniMaxParameters.getReplyConstraints() == null) {
            miniMaxParameters.setReplyConstraints(parameters.getReplyConstraints());
        }
        if (miniMaxParameters.getTemperature() == null) {
            miniMaxParameters.setTemperature(parameters.getTemperature());
        }
        if (miniMaxParameters.getModel() == null) {
            miniMaxParameters.setModel(parameters.getModel());
        }
        if (miniMaxParameters.getBotSetting() == null) {
            miniMaxParameters.setBotSetting(parameters.getBotSetting());
        }
        if (miniMaxParameters.getMaskSensitiveInfo() == null) {
            miniMaxParameters.setMaskSensitiveInfo(parameters.getMaskSensitiveInfo());
        }

        return miniMaxParameters;
    }

    @Override
    public String getTraceInfo() {
        return JSON.toJSONString(this.miniMaxParameters);
    }

    @Override
    public String getLlmFamilyName() {
        if (miniMaxParameters.getModel() == null) {
            return null;
        }
        return miniMaxParameters.getModel();
    }
}
