package com.alibaba.langengine.core.model;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.config.LangEngineConfiguration;
import com.alibaba.langengine.core.messages.*;
import com.alibaba.langengine.core.model.fastchat.completion.chat.*;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.core.model.fastchat.runs.ToolCallFunction;
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
        this.add("user");
        this.add("function");
        this.add("image_url");
        this.add("audio");
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
    private List<BaseMessage> convertToMessage(Map<String, String> baseMessage) {
        String role = baseMessage.get("role");
        String text = baseMessage.get("content");
        switch (role){
            case "assistant":
                return Collections.singletonList(new AIMessage(text));
            case "system":
                return Collections.singletonList(new SystemMessage(text));
            case "user":
                return Collections.singletonList(new HumanMessage(text));
            case "image_url":
                return Collections.singletonList(buildTypedMessage("image_url", text));
            case "audio":
                return Collections.singletonList(buildTypedMessage("audio", text));
            case "video":
                HumanMessage humanMessage = buildTypedMessage("video",text);
                return Collections.singletonList(humanMessage);
            case "function":
                return parseTools(text).stream().collect(Collectors.toList());
            default:
                HumanMessage m = new HumanMessage(text);
                return Collections.singletonList(m);
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
            List<BaseMessage> allMessages = new ArrayList<>();
            List<FunctionDefinition> functions = null;
            for (BaseMessage message : messages) {
                if(message instanceof FunctionMessage){
                    functions.add((FunctionDefinition)message.getAdditionalKwargs().get("def"));
                    message.setAdditionalKwargs(null);
                }else {
                    //TODO function简单的要不要？
                    allMessages.add(message);
                }
            }

            return baseChatModel.run(allMessages, functions, list, null, extraAttributes).getContent();

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
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if(messages.size() == 0){
            messages.add(new HumanMessage(prompt));
        }
        return messages;
    }

    public static List<FunctionMessage> parseTools(String inputText) {
        // 正则表达式匹配 <tools> 标签中的 JSON 对象
        String regex = "<tools>\\s*(\\{.*?\\})\\s*(\\{.*?\\})?\\s*</tools>";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(inputText);

        List<JSONObject> tools = new ArrayList<>();

        if (matcher.find()) {
            // 提取第一个 JSON 对象
            String json1 = matcher.group(1);
            tools.add(JSON.parseObject(json1));

            // 如果存在第二个 JSON 对象，则提取
            String json2 = matcher.group(2);
            if (json2 != null && !json2.isEmpty()) {
                tools.add(JSON.parseObject(json2));
            }
        }
        List<FunctionMessage> toolsResults = new ArrayList<>();
        //list中如果type = function，取出function字段变成FunctionDefinition
        if(tools.size() != 0){
            for(JSONObject jsonObject : tools){
                if(jsonObject.getString("type").equals("function")){
                    new FunctionDefinition();
                    JSONObject func = jsonObject.getJSONObject("function");
                    FunctionMessage funcMessage = new FunctionMessage();
                    funcMessage.setName(func.getString("name"));
                    funcMessage.setContent(func.getString("description"));
                    FunctionDefinition functionDefinition = JSON.parseObject(func.toJSONString(), FunctionDefinition.class);
                    funcMessage.setAdditionalKwargs(new HashMap<String, Object>(){{
                        put("def", functionDefinition);
                    }});
                }
            }
        }

        return toolsResults;
    }
    public static void main(String args[]){
        String tool = "<tool_call>\n"
                + "{\"name\": \"get_current_temperature\", \"arguments\": {\"location\": \"San Francisco, California, United States\", \"unit\": \"celsius\"}}\n"
                + "</tool_call>\n"
                + "<tool_call>\n"
                + "{\"name\": \"get_temperature_date\", \"arguments\": {\"location\": \"San Francisco, California, United States\", \"date\": \"2024-10-01\", \"unit\": \"celsius\"}}\n"
                + "</tool_call>";
        String item = "{\"name\": \"get_current_temperature\", \"arguments\": {\"location\": \"San Francisco, California, United States\", \"unit\": \"celsius\"}}\n";
        ToolCallFunction tools = JSON.parseObject(item, ToolCallFunction.class);

        String text = "You are Qwen, created by Alibaba Cloud. You are a helpful assistant.\n"
                + "\n"
                + "Current Date: 2024-09-30\n"
                + "\n"
                + "# Tools\n"
                + "\n"
                + "You may call one or more functions to assist with the user query.\n"
                + "\n"
                + "You are provided with function signatures within <tools></tools> XML tags:\n"
                + "<tools>\n"
                + "{\"type\": \"function\", \"function\": {\"name\": \"get_current_temperature\", \"description\": \"Get current temperature at a location.\", \"parameters\": {\"type\": \"object\", \"properties\": {\"location\": {\"type\": \"string\", \"description\": \"The location to get the temperature for, in the format \\\"City, State, Country\\\".\"}, \"unit\": {\"type\": \"string\", \"enum\": [\"celsius\", \"fahrenheit\"], \"description\": \"The unit to return the temperature in. Defaults to \\\"celsius\\\".\"}}, \"required\": [\"location\"]}}}\n"
                + "{\"type\": \"function\", \"function\": {\"name\": \"get_temperature_date\", \"description\": \"Get temperature at a location and date.\", \"parameters\": {\"type\": \"object\", \"properties\": {\"location\": {\"type\": \"string\", \"description\": \"The location to get the temperature for, in the format \\\"City, State, Country\\\".\"}, \"date\": {\"type\": \"string\", \"description\": \"The date to get the temperature for, in the format \\\"Year-Month-Day\\\".\"}, \"unit\": {\"type\": \"string\", \"enum\": [\"celsius\", \"fahrenheit\"], \"description\": \"The unit to return the temperature in. Defaults to \\\"celsius\\\".\"}}, \"required\": [\"location\", \"date\"]}}}\n"
                + "</tools>\n"
                + "\n"
                + "For each function call, return a json object with function name and arguments within <tool_call></tool_call> XML tags:\n"
                + "<tool_call>\n"
                + "{\"name\": <function-name>, \"arguments\": <args-json-object>}\n"
                + "</tool_call>";

        System.out.println(parseTools(text));

    }

}
