package com.alibaba.langengine.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.config.LangEngineConfiguration;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.messages.SystemMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessageConstant;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessageContent;
import com.alibaba.langengine.core.outputs.Generation;
import com.alibaba.langengine.core.outputs.LLMResult;
import com.alibaba.langengine.core.outputs.context.LlmResultHolder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class RawWrapChatModelLLM extends BaseLLM<ChatCompletionRequest> {

    BaseChatModel baseChatModel;

    public RawWrapChatModelLLM(BaseChatModel baseChatModel) {
        this.baseChatModel = baseChatModel;
    }

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

                    String responseContent = this.run(prompt, stops, consumer, extraAttributes);
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


    private static HashSet<String> roleSet = new HashSet<String>(){{
        this.add("system");
        this.add("assistant");
    }};
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
                m.put("role", roleSet.contains(role) ? role : "user");
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
    private BaseMessage convertToMessage(Map<String, String> baseMessage) {
        String role = baseMessage.get("role");
        String text = baseMessage.get("content");
        switch (role){
            case "assistant":
                return new AIMessage(text);
            case "system":
                return new SystemMessage(text);
            case "user":
                return new HumanMessage(text);
            case "image_url":
                return buildTypedMessage("image_url", text);
            case "audio":
                return buildTypedMessage("audio", text);
            case "video":
                HumanMessage humanMessage = buildTypedMessage("video",text);
                return humanMessage;
            default:
                HumanMessage m = new HumanMessage(text);
                return m;
        }
    }

    @NotNull
    private static HumanMessage buildTypedMessage(String type,String text) {
        HumanMessage humanMessage = new HumanMessage();
        List<ChatMessageContent> contents = Lists.newArrayList();

        ChatMessageContent content = new ChatMessageContent();
        content.setType(type);
        Map<String, Object> imageUrl = Maps.newHashMap();
        imageUrl.put("url", text);
        //imageUrl.put("detail", "low");
        content.setImageUrl(imageUrl);
        contents.add(content);
        humanMessage.setAdditionalKwargs(new HashMap<String, Object>() {{
            put(ChatMessageConstant.CHAT_MESSAGE_CONTENTS_KEY, contents);
        }});
        return humanMessage;
    }

    @Override
    public String run(String prompt, List<String> list, Consumer<String> consumer,
        Map<String, Object> extraAttributes) {

        try {

            // 对于聊天模型，还需要加一些历史内容再里面
            // 取用户的原始输入
            List<Map<String, String>> messageList = extractStructuredContent(prompt);
            List<BaseMessage> messages = processHistoryMessages(prompt,messageList);

            return baseChatModel.run(messages, null, list, null, extraAttributes).getContent();

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

    private List<BaseMessage> processHistoryMessages(String prompt,List<Map<String, String>> messageList) {
        // 转换 BaseMessage 列表为 Message 列表
        List<BaseMessage> messages = messageList.stream()
            .map(this::convertToMessage)
            .collect(Collectors.toList());

        if(messages.size() == 0){
            messages.add(new HumanMessage(prompt));
        }
        return messages;
    }

}
