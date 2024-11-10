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
package com.alibaba.langengine.xingchen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.config.LangEngineConfiguration;
import com.alibaba.langengine.core.model.BaseLLM;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.core.outputs.Generation;
import com.alibaba.langengine.core.outputs.LLMResult;
import com.alibaba.langengine.core.outputs.context.LlmResultHolder;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.xingchen.ApiClient;
import com.alibaba.xingchen.api.ChatApiSub;
import com.alibaba.xingchen.auth.HttpBearerAuth;
import com.alibaba.xingchen.model.CharacterKey;
import com.alibaba.xingchen.model.ChatReqParams;
import com.alibaba.xingchen.model.ChatReqParams.ChatReqParamsBuilder;
import com.alibaba.xingchen.model.ChatResult;
import com.alibaba.xingchen.model.Choice;
import com.alibaba.xingchen.model.Message;
import com.alibaba.xingchen.model.ModelParameters;
import com.alibaba.xingchen.model.ResultDTOChatResult;
import com.alibaba.xingchen.model.UserProfile;

import io.reactivex.Flowable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static com.alibaba.langengine.xingchen.XingchenConfiguration.XINGCHEN_API_KEY;

@Slf4j
@Data
public class XingchenRawLLM extends BaseLLM<ChatCompletionRequest> {

    /**
     * 星辰对应的apiKey是哪个
     */
    private String apiKey = XINGCHEN_API_KEY;

    private String modelName;
    /**
     * 模型参数，不能为空
     */
    private Double temperature;
    private Double topP;
    private Integer topK;
    //double frequencyPenalty;
    //double presencePenalty;
    //

    /**
     * 是否流式输出内容
     */
    private Boolean stream = false;

    /**
     * 生成LLM结果。
     *
     * @param prompts          提示信息列表。
     * @param stops            停止词列表。
     * @param executionContext 执行上下文。
     * @param consumer         消费者处理函数。
     * @return LLM结果实例。
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
                        List<Generation> cacheVal = LangEngineConfiguration.CurrentCache.get(prompt, llmString);
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

                    String responseContent = this.run(prompt, stops, consumer, null);
                    generation.setText(responseContent);
                    generation.setGenerationInfo(new HashMap());
                    llmResult.setLlmOutput(LlmResultHolder.getResult());
                    if (LangEngineConfiguration.CurrentCache != null) {
                        LangEngineConfiguration.CurrentCache.update(prompt, llmString, generations);
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
        return executionContext != null ? executionContext : new ExecutionContext();
    }

    /**
     * 处理历史消息。
     *
     * @param prompt 包含历史消息的输入数据。
     * @return 处理后的消息列表。
     */
    private List<Message> processHistoryMessages(String prompt,List<Map<String, String>> messageList) {
        // 转换 BaseMessage 列表为 Message 列表
        List<Message> messages = messageList.stream()
            .map(this::convertToMessage)
            .collect(Collectors.toList());

        if(messages.size() == 0){
            messages.add(Message.builder().content(prompt).role("user").build());
        }
        return messages;
    }

    public static List<Map<String, String>> extractStructuredContent(String text) {
        List<Map<String, String>> contents = new ArrayList<>();

        try {
            // 正则表达式匹配system、assistant和user的内容
            String regex = "<\\|im_start\\|>(?<role>system|assistant|user|[a-zA-Z0-9_\\u4e00-\\u9fff]+)\\s*(?<content>.*?)<\\|im_end\\|>";

            // 编译正则表达式
            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(text);

            // 遍历所有匹配结果
            while (matcher.find()) {
                // 提取角色和内容
                String role = matcher.group("role");
                String content = matcher.group("content").trim(); // 去除前后空白字符
                Map<String, String> m = new HashMap<>();
                // 将提取的内容放入Map中
                m.put("role", role);
                m.put("content", content);
                contents.add(m);
            }
        } catch (Exception e) {
            log.info("extractStructuredContent error", e);
        }

        return contents;
    }

    /**
     * 将 BaseMessage 转换为 Message。
     *
     * @param baseMessage 基本消息对象。
     * @return 转换后的消息对象。
     */
    private Message convertToMessage(Map<String, String> baseMessage) {
        String role = baseMessage.get("role");
        String finalRole = "assistant";
        switch (role){
            case "user":
            case "system":
                finalRole = role;
                break;
            default:
                finalRole = "assistant";
        }
        return Message.builder().content(baseMessage.get("content")).role(finalRole).build();
    }

    @Override
    public String run(String prompt, List<String> list, Consumer<String> consumer,
        Map<String, Object> extraAttributes) {

        try {

            // 对于聊天模型，还需要加一些历史内容再里面
            // 取用户的原始输入
            List<Map<String, String>> messageList = extractStructuredContent(prompt);
            List<Message> messages = processHistoryMessages(prompt,messageList);
            String botName = extractBotName(messageList);

            List<Message> systemMsg = messages.stream().filter(message -> message.getRole().equals("system"))
                .collect(Collectors.toList());
            String system = systemMsg.size() > 0 ? systemMsg.get(0).getContent() : "You are a helpful assistant";

            ModelParameters modelParameters = ModelParameters
                .builder()
                .seed(new Random().nextLong() & Long.MAX_VALUE)
                .topP(topP)
                .topK(topK)
                .modelName(modelName)
                .temperature(temperature)
                .build();

            modelParameters = applyDefaultValues(modelParameters);

            ChatReqParamsBuilder<?, ?> inner = ChatReqParams
                .builder()
                .botProfile(CharacterKey.builder().name(botName).content(system).build())
                // 也考虑参数中配置的值
                .modelParameters(modelParameters)
                .userProfile(UserProfile.builder().userId(UUID.randomUUID().toString()).build())
                .messages(messages.stream().filter(message -> !message.getRole().equals("system"))
                    .collect(Collectors.toList()));

            ChatReqParams chatReqParams = inner.build();

            ChatApiSub api = XingchenClientRepo.getChatApiSub(getApiKey(), getStream());
            //ArrayList<String> resultContent = Lists.newArrayList();
            AtomicReference<String> resultContent = new AtomicReference<>("");
            if (stream) {
                Flowable<ChatResult> response = api.streamOut(chatReqParams);
                response.doOnError(Throwable::printStackTrace).blockingForEach(message -> {
                    if (consumer != null) {
                        consumer.accept(message.getChoices().get(0).getMessages().get(0).getContent());
                    }
                    resultContent.set(message.getChoices().get(0).getMessages().get(0).getContent());
                });

                return resultContent.get();
            } else {
                ResultDTOChatResult result = api.chat(chatReqParams);
                LlmResultHolder.setResult(JSON.parseObject(JSON.toJSONString(result)));
                List<Choice> choices = result.getData().getChoices();
                return choices.get(0).getMessages().get(0).getContent();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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

    private String extractBotName(List<Map<String, String>> messageList) {
        for (Map<String, String> message : messageList) {
            if (!"user".equals(message.get("role"))
                && !"system".equals(message.get("role"))
                && !"assistant".equals(message.get("role"))) {
                return message.get("role");
            }
        }
        return "assistant";
    }

    public static class XingchenClientRepo {

        /**
         * 请求对应的基础路径
         */
        public static String basePath = "https://nlp.aliyuncs.com";

        private static final Map<String, ChatApiSub> XINGCHEN_CLIENT_REPO = new ConcurrentHashMap<>();

        public static ChatApiSub getChatApiSub(String apiKey, Boolean stream) {

            String key = apiKey + stream;
            if (XINGCHEN_CLIENT_REPO.containsKey(key)) {
                return XINGCHEN_CLIENT_REPO.get(key);
            } else {
                ChatApiSub chatApiSub = createChatApiSub(apiKey, stream);
                XINGCHEN_CLIENT_REPO.put(key, chatApiSub);
                return chatApiSub;
            }

        }

        private static ChatApiSub createChatApiSub(String apiKey, Boolean stream) {
            ChatApiSub api = new ChatApiSub();
            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath(basePath);
            apiClient.addDefaultHeader("X-DashScope-SSE", stream ? "enable" : "disable");

            // Configure HTTP bearer authorization: Authorization
            HttpBearerAuth authorization = (HttpBearerAuth)apiClient.getAuthentication("Authorization");
            authorization.setBearerToken(apiKey);
            api.setApiClient(apiClient);

            return api;
        }
    }

    /**
     * 应用默认值，如果新的ModelParameters对象没有对应的值。
     *
     * @param modelParameters 新的ModelParameters对象
     * @return 更新了默认值的ModelParameters对象
     */
    private ModelParameters applyDefaultValues(ModelParameters modelParameters) {
        ModelParameters defaultParameters = ModelParameters.builder().seed(1683806810L).topP(0.8).temperature(0.8)
            .build();

        if (modelParameters == null) {
            return defaultParameters;
        }

        if (modelParameters.getTopP() == null) {
            modelParameters.setTopP(defaultParameters.getTopP());
        }
        if (modelParameters.getSeed() == null) {
            modelParameters.setSeed(defaultParameters.getSeed());
        }
        if (modelParameters.getTemperature() == null) {
            modelParameters.setTemperature(defaultParameters.getTemperature());
        }
        return modelParameters;
    }

}
