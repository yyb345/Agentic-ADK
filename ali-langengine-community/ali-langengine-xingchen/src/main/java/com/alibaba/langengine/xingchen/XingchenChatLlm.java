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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
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
import com.alibaba.xingchen.model.ext.chat.ChatContext;

import com.google.common.collect.Lists;
import io.reactivex.Flowable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static com.alibaba.langengine.xingchen.XingchenConfiguration.XINGCHEN_API_KEY;

/**
 * @author aihe.ah
 * @time 2023/11/10
 * 功能说明：通义星辰的LLM实现
 * 接口文档：https://tongyi.aliyun.com/xingchen/document/java_sdk
 * seed生成时，随机数的种子，用于控制模型生成的随机性。如果使用相同的种子，每次运行生成的结果都将相同；当需要复现模型的生成结果时，可以使用相同的种子。
 *
 * topP生成时，核采样方法的概率阈值。例如，取值为0.8时，仅保留累计概率之和大于等于0.8的概率分布中的token，作为随机采样的候选集。取值范围为(0,1.0)
 * * ，取值越大，生成的随机性越高；取值越低，生成的随机性越低。默认值
 * * 0.8。注意，取值不要大于等于1
 * temperature  较高的值将使输出更加随机，而较低的值将使输出更加集中和确定。可选，默认取值0.9
 */
@Slf4j
@Data
public class XingchenChatLlm extends BaseLLM<ChatCompletionRequest> {

    /**
     * 星辰对应的apiKey是哪个
     */
    private String apiKey = XINGCHEN_API_KEY;

    /**
     * 用的定义的角色ID是哪个，角色id或者content两者其中之一不能为空
     */
    private CharacterKey characterKey;

    /**
     * 模型参数，不能为空
     */
    private ModelParameters modelParameters = ModelParameters
        .builder()
        .seed(1683806810L)
        .topP(0.8)
        .temperature(0.8)
        .build();

    /**
     * 星辰对应的用户信息，userid不能为空
     */
    private UserProfile userProfile;

    /**
     * 星尘对应的sessionId,若传了则星尘会优先使用你传过来的 history，没传则从后台根据 sessionId 获取最多轮条对话历史
     */
    private boolean usingChatHistory= false;

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

                    // 对于聊天模型，还需要加一些历史内容再里面
                    // 取用户的原始输入
                    List<Message> messages = processHistoryMessages(executionContext.getInputs());

                    modelParameters = applyDefaultValues(modelParameters);

                    ChatReqParamsBuilder<?, ?> inner = ChatReqParams
                        .builder()
                        .botProfile(characterKey)
                        // 也考虑参数中配置的值
                        .modelParameters(modelParameters)
                        .userProfile(userProfile)
                        .messages(messages);
                    if(usingChatHistory){
                        inner = inner.context(ChatContext.builder().useChatHistory(usingChatHistory).build());
                    }
                    ChatReqParams chatReqParams = inner.build();

                    String newPrompt = JSON.toJSONString(chatReqParams);

                    String responseContent = this.run(newPrompt, stops, consumer, null);
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
     * @param inputs 包含历史消息的输入数据。
     * @return 处理后的消息列表。
     */
    private List<Message> processHistoryMessages(Map<String, Object> inputs) {
        List<BaseMessage> messageList = retrieveHistory(inputs);

        // 转换 BaseMessage 列表为 Message 列表
        List<Message> messages = messageList.stream()
            .map(this::convertToMessage)
            .collect(Collectors.toList());

        // 添加当前输入信息到消息列表
        addCurrentInputToMessages(inputs, messages);

        return messages;
    }

    /**
     * 从输入数据中检索历史消息。
     *
     * @param inputs 输入数据。
     * @return 历史消息列表。
     */
    private List<BaseMessage> retrieveHistory(Map<String, Object> inputs) {
        Object history = inputs.get("history");
        if (history instanceof String) {
            List<BaseMessage> result = JSON.parseArray((String)history, BaseMessage.class);
            if(result != null){
                return result;
            }
        } else if (history instanceof List) {
            return (List<BaseMessage>)history;
        }
        return new ArrayList<>();
    }

    /**
     * 将 BaseMessage 转换为 Message。
     *
     * @param baseMessage 基本消息对象。
     * @return 转换后的消息对象。
     */
    private Message convertToMessage(BaseMessage baseMessage) {
        String role = "human".equals(baseMessage.getType()) ? "user" : "assistant";
        return Message.builder().content(baseMessage.getContent()).role(role).build();
    }

    /**
     * 将当前输入添加到消息列表中。
     *
     * @param inputs   输入数据。
     * @param messages 消息列表。
     */
    private void addCurrentInputToMessages(Map<String, Object> inputs, List<Message> messages) {
        String currentInput = String.valueOf(inputs.get("input"));
        messages.add(Message.builder().content(currentInput).role("user").build());
    }

    @Override
    public String run(String prompt, List<String> list, Consumer<String> consumer, Map<String, Object> extraAttributes) {

        try {
            ChatReqParams chatReqParams = null;
            try {
                chatReqParams = JSON.parseObject(prompt, ChatReqParams.class);
            } catch (Exception e) {
                chatReqParams = ChatReqParams.builder().botProfile(characterKey).modelParameters(modelParameters)
                    .userProfile(userProfile).messages(Lists.newArrayList(Message.builder().content(prompt).role("user")
                        .build())).build();
            }

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

    @Override
    public String getTraceInfo() {
        HashMap<Object, Object> traceInfo = new HashMap<>();
        traceInfo.put("characterKey", characterKey);
        traceInfo.put("modelParameters", modelParameters);
        return JSON.toJSONString(traceInfo);
    }

}
