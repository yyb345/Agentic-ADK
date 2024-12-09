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
package com.alibaba.langengine.autogen.agentchat;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.autogen.Agent;
import com.alibaba.langengine.autogen.CodeUtils;
import com.alibaba.langengine.autogen.agentchat.support.CodeEngine;
import com.alibaba.langengine.autogen.agentchat.support.CodeExecutionResult;
import com.alibaba.langengine.autogen.agentchat.support.DefaultCodeEngine;
import com.alibaba.langengine.autogen.agentchat.support.ReplyResult;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.ChatMessage;
import com.alibaba.langengine.core.messages.FunctionMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionDefinition;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionParameter;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionProperty;
import com.alibaba.langengine.core.outputs.LLMResult;
import com.alibaba.langengine.core.prompt.ChatPromptValue;
import com.alibaba.langengine.core.prompt.PromptValue;
import com.alibaba.langengine.core.tool.StructuredParameter;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.core.util.CharUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A class for generic conversable agents which can be configured as assistant or user proxy.
 *
 * After receiving each message, the agent will send a reply to the sender unless the msg is a termination msg.
 * For example, AssistantAgent and UserProxyAgent are subclasses of this class,
 * configured with different default settings.
 *
 * To modify auto reply, override `generate_reply` method.
 * To disable/enable human response in every turn, set `human_input_mode` to "NEVER" or "ALWAYS".
 * To modify the way to get human input, override `get_human_input` method.
 * To modify the way to execute code blocks, single code block, or function call, override `execute_code_blocks`,
 * `run_code`, and `execute_function` methods respectively.
 * To customize the initial message when a conversation starts, override `generate_init_message` method.
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class ConversableAgent extends Agent {

    /**
     * 是否中止，如果设置该函数用这个判断
     */
    private Predicate<Map<String, Object>> isTermination;

    /**
     * maximum number of consecutive auto replies
     * 连续自动回复的最大数量
     */
    private static final int MAX_CONSECUTIVE_AUTO_REPLY = 100;

    private Map<Agent, List<Map<String, Object>>> oaiMessages;

    private List<Map<String, Object>> oaiSystemMessage;

    private Map<String, Object> codeExecutionConfig;

    private String humanInputMode;

    private Integer maxConsecutiveAutoReply;

    private Map<Agent, Integer> consecutiveAutoReplyCounter;

    private Map<Agent, Integer> maxConsecutiveAutoReplyDict;

    private Map<Agent, Boolean> replyAtReceive;

    private Map<String, Function<Object, String>> functionMap;

    private Map<String, StructuredTool> toolMap;

    private CodeEngine codeEngine = new DefaultCodeEngine();

    // TODO ...
//    private List<Map<String, Object>> replyFuncList;

    public ConversableAgent(String name, BaseLanguageModel llm) {
        this(name, llm, "You are a helpful AI Assistant.");
    }

    public ConversableAgent(String name,
                            BaseLanguageModel llm,
                            String systemMessage) {
        this(name, llm, systemMessage, null);
    }

    public ConversableAgent(String name,
                            BaseLanguageModel llm,
                            String systemMessage,
                            Integer maxConsecutiveAutoReply) {
        this(name, llm, systemMessage, maxConsecutiveAutoReply, null);
    }

    public ConversableAgent(String name,
                            BaseLanguageModel llm,
            String systemMessage,
            Integer maxConsecutiveAutoReply,
            String humanInputMode) {
        this(name, llm, systemMessage, maxConsecutiveAutoReply, humanInputMode, null);
    }

    public ConversableAgent(String name,
                            BaseLanguageModel llm,
            String systemMessage,
            Integer maxConsecutiveAutoReply,
            String humanInputMode,
                            Map<String, Function<Object, String>> functionMap) {
        this(name, llm, systemMessage, maxConsecutiveAutoReply, humanInputMode, functionMap, null);
    }

    public ConversableAgent(String name,
                            BaseLanguageModel llm,
            String systemMessage,
            Integer maxConsecutiveAutoReply,
            String humanInputMode,
                            Map<String, Function<Object, String>> functionMap,
            Map<String, Object> codeExecutionConfig) {
        this(name, llm, systemMessage, maxConsecutiveAutoReply, humanInputMode, functionMap, codeExecutionConfig, null);
    }

    public ConversableAgent(String name,
                            BaseLanguageModel llm,
            String systemMessage,
            Integer maxConsecutiveAutoReply,
            String humanInputMode,
                            Map<String, Function<Object, String>> functionMap,
            Map<String, Object> codeExecutionConfig,
            Map<String, Object> llmConfig) {
        this(name, llm, systemMessage, maxConsecutiveAutoReply, humanInputMode, functionMap, codeExecutionConfig, llmConfig, null);
    }

    public ConversableAgent(String name,
                            BaseLanguageModel llm,
            String systemMessage,
            Integer maxConsecutiveAutoReply,
            String humanInputMode,
                            Map<String, Function<Object, String>> functionMap,
            Map<String, Object> codeExecutionConfig,
            Map<String, Object> llmConfig,
            Object defaultAutoReply) {
        super(name, llm);

        oaiMessages = new HashMap<>();

        oaiSystemMessage = new ArrayList<>();
        Map<String, Object> oaiSystemMessageMap = new HashMap<>();
        oaiSystemMessageMap.put("content", systemMessage);
        oaiSystemMessageMap.put("role", "system");
        oaiSystemMessage.add(oaiSystemMessageMap);

        if(codeExecutionConfig == null) {
            this.codeExecutionConfig = new HashMap<>();
        } else {
            this.codeExecutionConfig = codeExecutionConfig;
        }

        this.humanInputMode = humanInputMode;

        this.maxConsecutiveAutoReply = maxConsecutiveAutoReply != null ? maxConsecutiveAutoReply : MAX_CONSECUTIVE_AUTO_REPLY;

        if(functionMap != null) {
            this.functionMap = functionMap;
        } else {
            this.functionMap = new HashMap<>();
        }

        this.consecutiveAutoReplyCounter = new HashMap<>();
        this.replyAtReceive = new HashMap<>();

        this.maxConsecutiveAutoReplyDict = new HashMap<>();

        // TODO
//        this.replyFuncList = new ArrayList<>();
    }

    public void initiateChat(ConversableAgent recipient, String message) {
        initiateChat(recipient, true, false, message);
    }

    public void initiateChat(ConversableAgent recipient,
                             boolean clearHistory,
                             boolean silent,
                             String message) {
        initiateChat(recipient, clearHistory, silent, message, new HashMap<>());
    }

    public void initiateChat(ConversableAgent recipient,
                             boolean clearHistory,
                             boolean silent,
                             String message,
                             Map<String, Object> context) {
        prepareChat(recipient, clearHistory);
        context.put("message", message);
        message = generateInitMessage(context);
        send(message, recipient, null, silent);
    }


    public String getSystemMessage() {
        return (String) oaiSystemMessage.get(0).get("content");
    }

    public void updateSystemMessage(String systemMessage) {
        oaiSystemMessage.get(0).put("content", systemMessage);
    }

    public void updateMaxConsecutiveAutoReply(int value, Agent sender) {
        if (sender == null) {
            maxConsecutiveAutoReply = value;
            for (Agent key : maxConsecutiveAutoReplyDict.keySet()) {
                maxConsecutiveAutoReplyDict.put(key, value);
            }
        } else {
            maxConsecutiveAutoReplyDict.put(sender, value);
        }
    }

    public int maxConsecutiveAutoReply(Agent sender) {
        if (sender == null) {
            return maxConsecutiveAutoReply;
        } else {
            return maxConsecutiveAutoReplyDict.get(sender);
        }
    }

    public Map<Agent, List<Map<String, Object>>> getChatMessages() {
        return oaiMessages;
    }

    public Map<String, Object> getLastMessage(Agent agent) {
        if (agent == null) {
            int nConversations = oaiMessages.size();
            if (nConversations == 0) {
                return null;
            }
            if (nConversations == 1) {
                for (List<Map<String, Object>> conversation : oaiMessages.values()) {
                    return conversation.get(conversation.size() - 1);
                }
            }
            throw new IllegalArgumentException("More than one conversation is found. Please specify the sender to get the last message.");
        }
        if (!oaiMessages.containsKey(agent)) {
            throw new IllegalArgumentException("The agent '" + agent.getName() + "' is not present in any conversation. No history available for this agent.");
        }
        List<Map<String, Object>> conversation = oaiMessages.get(agent);
        return conversation.get(conversation.size() - 1);
    }

    public static Map<String, Object> messageToDict(Object message) {
        if (message instanceof String) {
            return new HashMap<String, Object>() {{ put("content", message); }};
        } else if (message instanceof Map) {
            return (Map<String, Object>) message;
        } else {
            return (Map<String, Object>) message;
        }
    }

    @Override
    public boolean send(Object message, Agent recipient, Boolean requestReply, Boolean silent) {
        Map<String, Object> messageDict = messageToDict(message);
        boolean valid = appendOaiMessage(messageDict, "assistant", recipient);

        if (valid) {
            recipient.receive(messageDict, this, requestReply, silent);
        } else {
            throw new IllegalArgumentException("Message can't be converted into a valid ChatCompletion message. Either content or function_call must be provided.");
        }

        return true;
    }

    @Override
    public void receive(Object message, Agent sender, Boolean requestReply, Boolean silent) {
        processReceivedMessage(message, sender, silent);

        if ((requestReply == null && replyAtReceive.get(sender) == null) || (requestReply != null && false == requestReply)) {
            return;
        }

        Object reply = generateReply(getChatMessages().get(sender), sender);

        if (reply != null) {
            send(reply, sender, null, silent);
        }
    }

    public void stopReplyAtReceive(Agent sender) {
        if (sender == null) {
            replyAtReceive.clear();
        } else {
            replyAtReceive.put(sender, false);
        }
    }

    public ReplyResult generateOaiReply(List<Map<String, Object>> messages, Agent sender) {
        if (messages == null) {
            for (Map.Entry entry : getOaiMessages().entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                System.out.println("key:" + key.hashCode() + "value:" + value.hashCode());
                System.out.println("sender:" + sender.hashCode());
            }
            messages = oaiMessages.get(sender);
        }

        List<Map<String, Object>> allMessages = new ArrayList<>();
        allMessages.addAll(oaiSystemMessage);
        allMessages.addAll(messages);

        // TODO 需要验证下
//        List<ChatMessage> chatMessages = new ArrayList<>();
//        for (Map<String, Object> message : allMessages) {
//            String role = (String) message.get("role");
//            ChatMessage chatMessage = new ChatMessage();
//            chatMessage.setRole(role);
//            chatMessage.setContent((String) message.get("content"));
//            if(message.get("name") != null) {
//                chatMessage.setName((String) message.get("name"));
//            }
//            if(message.get("function_call") != null) {
//                chatMessage.setFunctionCall((Map) message.get("function_call"));
//            }
//            chatMessages.add(chatMessage);
//        }

        List<PromptValue> promptValueList = new ArrayList<>();
        ChatPromptValue chatPromptValue = new ChatPromptValue();
        promptValueList.add(chatPromptValue);
        List<BaseMessage> baseMessages = new ArrayList<>();
        for (Map<String, Object> message : allMessages) {
            String role = (String) message.get("role");
            String content = (String) message.get("content");

            if(message.get("name") != null) {
                FunctionMessage functionMessage = new FunctionMessage();
                functionMessage.setName((String) message.get("name"));
                functionMessage.setContent(content);
                baseMessages.add(functionMessage);
            } else if(message.get("function_call") != null) {
                AIMessage aiMessage = new AIMessage();
                aiMessage.setContent(content);
                aiMessage.setAdditionalKwargs(new HashMap<>());
                aiMessage.getAdditionalKwargs().put("function_call", message.get("function_call"));
                baseMessages.add(aiMessage);
            } else {
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setRole(role);
                chatMessage.setContent(content);
                baseMessages.add(chatMessage);
            }
        }
        chatPromptValue.setMessages(baseMessages);

        List<FunctionDefinition> functions = null;
        if(getTools() != null && getTools().size() > 0) {
            functions = new ArrayList<>();

            for (StructuredTool tool : getTools()) {
                FunctionDefinition functionDefinition = new FunctionDefinition();
                functionDefinition.setName(tool.getName());
                functionDefinition.setDescription(tool.getDescription());

                FunctionParameter functionParameter = new FunctionParameter();
                functionParameter.setType("object");

                Map<String, FunctionProperty> propertyMap = new HashMap<>();
                for (StructuredParameter structuredParameter : tool.getStructuredSchema().getParameters()) {
                    FunctionProperty functionProperty = new FunctionProperty();
                    functionProperty.setType(structuredParameter.getSchema().get("type").toString());
                    functionProperty.setDescription(structuredParameter.getDescription());
                    propertyMap.put(structuredParameter.getName(), functionProperty);
                }
                functionParameter.setProperties(propertyMap);
                functionParameter.setRequired(tool.getStructuredSchema().getParameters().stream().map(e -> e.getName()).collect(Collectors.toList()));
                functionDefinition.setParameters(functionParameter);

                functions.add(functionDefinition);
            }
        }

        LLMResult llmResult = getLlm().generatePrompt(promptValueList, functions, null);
//        LLMResult llmResult = getLlm().generateChatModel(chatMessages, functions, null);

        ReplyResult replyResult;
        String content = llmResult.getGenerations().get(0).get(0).getText();
        if(content.startsWith("{") && content.endsWith("}") && content.contains("function_call")) {
            replyResult = new ReplyResult(true, JSON.parseObject(content, Map.class));
        } else {
            replyResult = new ReplyResult(true, content);
        }
        return replyResult;
    }

    public ReplyResult generateCodeExecutionReply(List<Map<String, Object>> messages, Agent sender) {
        if (codeExecutionConfig == null || codeExecutionConfig.isEmpty()) {
            return new ReplyResult(false, null);
        }

        if (messages == null) {
            messages = oaiMessages.get(sender);
        }

        Integer lastNMessages = 1;
        if(codeExecutionConfig.containsKey("last_n_messages")) {
            lastNMessages = (Integer) codeExecutionConfig.remove("last_n_messages");
        }

        for (int i = 0; i < Math.min(messages.size(), lastNMessages); i++) {
            Map<String, Object> message = messages.get(messages.size() - (i + 1));

            if (message.get("content") == null) {
                continue;
            }

            List<Pair<String, String>> codeBlocks = CodeUtils.extractCode((String) message.get("content"), false);

            if (codeBlocks.size() == 1 && codeBlocks.get(0).getKey().equalsIgnoreCase("unknown")) {
                continue;
            }

            CodeExecutionResult codeExecutionResult = executeCodeBlocks(codeBlocks);

            codeExecutionConfig.put("last_n_messages", lastNMessages);
            String exitCode2Str = (codeExecutionResult.getExitcode() == 0) ? "execution succeeded" : "execution failed";
            String reply = String.format("exitcode: %d (%s)\nCode output: %s", codeExecutionResult.getExitcode(), exitCode2Str, codeExecutionResult.getLogs());

            return new ReplyResult(true, reply);
        }

        codeExecutionConfig.put("last_n_messages", lastNMessages);

        return new ReplyResult(false, null);
    }

    public ReplyResult generateFunctionCallReply(List<Map<String, Object>> messages, Agent sender) {
        if (messages == null) {
            messages = oaiMessages.get(sender);
        }

        Map<String, Object> message = messages.get(messages.size() - 1);

        if (message.get("function_call") != null) {
            return executeFunction((Map<String, Object>)message.get("function_call"));
        }
        return new ReplyResult(false, null);
    }

    public ReplyResult checkTerminationAndHumanReply(List<Map<String, Object>> messages, Agent sender) {
        if (messages == null) {
            for (Map.Entry entry : getOaiMessages().entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                System.out.println("key:" + key.hashCode() + "value:" + value.hashCode());
                System.out.println("sender:" + sender.hashCode());
            }
            messages = oaiMessages.get(sender);
        }

        Map<String, Object> message = messages.get(messages.size() - 1);
        String reply = "";
        String noHumanInputMsg = "";

        if (humanInputMode.equals("ALWAYS")) {
            reply = getHumanInput(String.format("Provide feedback to %s. Press enter to skip and use auto-reply, or type 'exit' to end the conversation: ", sender.getName()));
            noHumanInputMsg = (reply.isEmpty()) ? "NO HUMAN INPUT RECEIVED." : "";
            reply = (reply.isEmpty() && isTerminationMsg(message)) ? "exit" : reply;
        } else {
            if (consecutiveAutoReplyCounter.get(sender) != null
                    && consecutiveAutoReplyCounter.get(sender) >=
//                    maxConsecutiveAutoReplyDict.get(sender)
                    100 // TODO ...
            ) {
                if (humanInputMode.equals("NEVER")) {
                    reply = "exit";
                } else {
                    boolean terminate = isTerminationMsg(message);
                    reply = getHumanInput((terminate) ? String.format("Please give feedback to %s. Press enter or type 'exit' to stop the conversation: ", sender.getName()) : String.format("Please give feedback to %s. Press enter to skip and use auto-reply, or type 'exit' to stop the conversation: ", sender.getName()));
                    noHumanInputMsg = (reply.isEmpty()) ? "NO HUMAN INPUT RECEIVED." : "";
                    reply = (reply.isEmpty() && terminate) ? "exit" : reply;
                }
            } else if (isTerminationMsg(message)) {
                if (humanInputMode.equals("NEVER")) {
                    reply = "exit";
                } else {
                    reply = getHumanInput(String.format("Please give feedback to %s. Press enter or type 'exit' to stop the conversation: ", sender.getName()));
                    noHumanInputMsg = (reply.isEmpty()) ? "NO HUMAN INPUT RECEIVED." : "";
                    reply = (reply.isEmpty()) ? "exit" : reply;
                }
            }
        }

        if (!noHumanInputMsg.isEmpty()) {
            System.out.println(colored(String.format("\n>> %s", noHumanInputMsg), "red"));
        }

        if (reply.equals("exit")) {
            consecutiveAutoReplyCounter.put(sender, 0);
            return new ReplyResult(true, null);
        }

        if (!StringUtils.isEmpty(reply)
//                || maxConsecutiveAutoReplyDict.get(sender) == 0
                // TODO ...
        ) {
            consecutiveAutoReplyCounter.put(sender, 0);
            return new ReplyResult(true, reply);
        }

        if(consecutiveAutoReplyCounter.get(sender) == null) {
            consecutiveAutoReplyCounter.put(sender, 0);
        }
        consecutiveAutoReplyCounter.put(sender, consecutiveAutoReplyCounter.get(sender) + 1);

        if (!humanInputMode.equals("NEVER")) {
            System.out.println(colored("\n>> USING AUTO REPLY...", "red"));
        }

        return new ReplyResult(false, null);
    }

    /**
     * Reply based on the conversation history and the sender.
     *
     *         Either messages or sender must be provided.
     *         Register a reply_func with `None` as one trigger for it to be activated when `messages` is non-empty and `sender` is `None`.
     *         Use registered auto reply functions to generate replies.
     *         By default, the following functions are checked in order:
     *         1. check_termination_and_human_reply
     *         2. generate_function_call_reply
     *         3. generate_code_execution_reply
     *         4. generate_oai_reply
     *         Every function returns a tuple (final, reply).
     *         When a function returns final=False, the next function will be checked.
     *         So by default, termination and human reply will be checked first.
     *         If not terminating and human reply is skipped, execute function or code and return the result.
     *         AI replies are generated only when no code execution is performed.
     *
     * @param messages
     * @param sender
     * @return
     */
    public Object generateReply(List<Map<String, Object>> messages, Agent sender) {
        if (messages == null && sender == null) {
            String errorMsg = "Either messages or sender must be provided.";
            log.error(errorMsg);
            throw new AssertionError(errorMsg);
        }

        if (messages == null) {
            messages = oaiMessages.get(sender);
        }

        ReplyResult replyResult = checkTerminationAndHumanReply(messages, sender);
        if(replyResult.isFinalFlag()) {
            return replyResult.getReply() != null ? replyResult.getReply() : replyResult.getOutput();
        }

        replyResult = generateFunctionCallReply(messages, sender);
        if(replyResult.isFinalFlag()) {
            return replyResult.getReply() != null ? replyResult.getReply() : replyResult.getOutput();
        }

        replyResult = generateCodeExecutionReply(messages, sender);
        if(replyResult.isFinalFlag()) {
            return replyResult.getReply() != null ? replyResult.getReply() : replyResult.getOutput();
        }

        replyResult = generateOaiReply(messages, sender);
        if(replyResult.isFinalFlag()) {
            return replyResult.getReply() != null ? replyResult.getReply() : replyResult.getOutput();
        }

        return null;
    }

    private String getHumanInput(String prompt) {
        System.out.print(prompt);
        Scanner scanner = new Scanner(System.in);
        String reply = scanner.nextLine();
        return reply;
    }

    private CodeExecutionResult runCode(String code, String lang, String filename, Map<String, Object> kwargs) {
        return CodeUtils.executeCode(code, lang, filename, kwargs);
    }

    private CodeExecutionResult executeCodeBlocks(List<Pair<String, String>> codeBlocks) {
        String logsAll = "";
        CodeExecutionResult result = null;
        for (int i = 0; i < codeBlocks.size(); i++) {
            Pair<String, String> codeBlock = codeBlocks.get(i);
            String lang = codeBlock.getKey();
            String code = codeBlock.getValue();
            if (lang.isEmpty()) {
                lang = CodeUtils.inferLang(code, codeEngine);
            }
            System.out.println("\n>> EXECUTING CODE BLOCK " + i + " (inferred language is " + lang + ")...");
            if (lang.equals("bash") || lang.equals("shell") || lang.equals("sh")) {
                if(lang.equals("shell") || lang.equals("bash")) {
                    lang = "sh";
                }
                result = runCode(code, lang, null, codeExecutionConfig);
            } else if (lang.equals("python") || lang.equals("Python")) {
                String filename = null;
                if (code.startsWith("# filename: ")) {
                    filename = code.substring(11, code.indexOf("\n")).trim();
                }
                result = runCode(code, "python", filename, codeExecutionConfig);
            } else {
                result = new CodeExecutionResult();
                result.setExitcode(1);
                result.setLogs("unknown language " + lang);
                result.setImage(null);
            }
            if (result.getImage() != null) {
//                codeExecutionConfig.setUseDocker(image);
            }
            logsAll += "\n" + result.getLogs();
            if (result.getExitcode() != 0) {
                result.setLogs(logsAll);
                return result;
            }
        }

        result.setLogs(logsAll);
        return result;
    }

    private static String formatJsonStr(String jstr) {
        StringBuilder result = new StringBuilder();
        boolean insideQuotes = false;
        char lastChar = ' ';

        for (int i = 0; i < jstr.length(); i++) {
            char currentChar = jstr.charAt(i);

            if (lastChar != '\\' && currentChar == '"') {
                insideQuotes = !insideQuotes;
            }

            lastChar = currentChar;

            if (!insideQuotes && currentChar == '\n') {
                continue;
            }

            if (insideQuotes && currentChar == '\n') {
                result.append("\\n");
            } else if (insideQuotes && currentChar == '\t') {
                result.append("\\t");
            } else {
                result.append(currentChar);
            }
        }

        return result.toString();
    }

    private ReplyResult executeFunction(Map<String, Object> functionCall) {
        String funcName = (String) functionCall.get("name");
        String content;
        boolean isExecSuccess = false;
        if(getToolMap() != null && getToolMap().containsKey(funcName) ) {
            StructuredTool tool = getToolMap().get(funcName);
            String arguments = (String) functionCall.get("arguments");
            if (arguments != null) {
                System.out.println(colored(String.format("\n>> EXECUTING FUNCTION %s...", funcName), "magenta"));
            }
            try {
                ToolExecuteResult toolExecuteResult = tool.run(arguments);
                isExecSuccess = true;
                content = toolExecuteResult.getOutput();
            } catch (Exception e) {
                content = String.format("Error: %s", e.getMessage());
            }
        } else {
            content = String.format("Error: Function %s not found.", funcName);
        }

        Map<String, Object> reply = new HashMap<>();
        reply.put("name", funcName);
        reply.put("role", "function");
        reply.put("content", content);
        return new ReplyResult(isExecSuccess, reply);
    }

    protected String generateInitMessage(Map<String, Object> context) {
        return (String) context.get("message");
    }

//    private void registerFunction(Map<String, Function<Object, String>> functionMap) {
//        this.functionMap.putAll(functionMap);
//    }

    public boolean canExecuteFunction(String name) {
        return functionMap.containsKey(name);
    }

    public Map<String, Function<Object, String>> getFunctionMap() {
        return functionMap;
    }

    @Override
    public void reset() {
        clearHistory(null);
        resetConsecutiveAutoReplyCounter(null);
        stopReplyAtReceive(null);
    }

    private String colored(String value, String color) {
        if("red".equals(color)) {
            return String.format("\033[31m%s\033[31m", value);
        } else if("yellow".equals(color)) {
            return String.format("\033[31m%s\033[31m", value);
        }
        return value;
    }

    private boolean isTerminationMsg(Map<String, Object> message) {
        if(isTermination != null) {
            return isTermination.test(message);
        }
        return message.get("content") != null ? message.get("content").toString().trim().endsWith("TERMINATE") : false;
    }

    private boolean appendOaiMessage(Object message, String role, Agent conversationId) {
        Map<String, Object> messageDict = messageToDict(message);
        Map<String, Object> oaiMessage = new HashMap<>();

        if (messageDict.containsKey("content")) {
            oaiMessage.put("content", messageDict.get("content"));
        } else if (messageDict.containsKey("function_call")) {
            oaiMessage.put("content", null);
        } else {
            return false;
        }

        oaiMessage.put("function_call", messageDict.get("function_call"));
        oaiMessage.put("name", messageDict.get("name"));
        oaiMessage.put("context", messageDict.get("context"));

        // Set message role
        if (messageDict.get("role") != null && messageDict.get("role").equals("function")) {
            oaiMessage.put("role", "function");
        } else {
            oaiMessage.put("role", role);
        }

        // Only messages with role "assistant" can have a function call
        if (messageDict.get("function_call") != null) {
            oaiMessage.put("role", "assistant");
            oaiMessage.put("function_call", messageToDict(oaiMessage.get("function_call")));
        }

        if(oaiMessages.get(conversationId) == null) {
            oaiMessages.put(conversationId, new ArrayList<>());
        }

        oaiMessages.get(conversationId).add(oaiMessage);

        return true;
    }

    private void printReceivedMessage(Object message, Agent sender) {
        Map<String, Object> messageDict = messageToDict(message);

        System.out.println(
                colored(sender.getName(), "yellow") + " (to " + getName() + "):\n"
        );

        if (messageDict.get("role") != null && messageDict.get("role").equals("function")) {
            String funcPrint = "***** Response from calling function \"" + messageDict.get("name") + "\" *****";
            System.out.println(funcPrint);
            System.out.println(messageDict.get("content"));
            System.out.println(CharUtils.repeat("*", funcPrint.length()));
        } else {
            Object content = messageDict.get("content");
            if (content != null) {
                if (messageDict.containsKey("context")) {
//                    content = OpenAIWrapper.instantiate(
//                            content,
//                            messageDict.get("context"),
//                            this.llmConfig != null && this.llmConfig.get("allow_format_str_template", false)
//                    );
                }
                System.out.println(content);
            }

            if (messageDict.containsKey("function_call")) {
                Map<String, Object> functionCall = (Map<String, Object>) messageDict.get("function_call");
                String funcPrint = "***** Suggested function Call: " + (functionCall.get("name") != null ? functionCall.get("name") : "(No function name found)" )+ " *****";
                System.out.println(funcPrint);
                System.out.println("Arguments: \n" + (functionCall.get("arguments") != null ? functionCall.get("arguments") : "(No arguments found)"));
                System.out.println(StringUtils.repeat("*", funcPrint.length()));
            }
        }

        System.out.println("\n" + CharUtils.repeat("-", 80));
    }

    private void processReceivedMessage(Object message, Agent sender, Boolean silent) {
        Map<String, Object> messageDict = messageToDict(message);

        boolean valid = appendOaiMessage(messageDict, "user", sender);

        if (!valid) {
            throw new IllegalArgumentException("Received message can't be converted into a valid ChatCompletion message. Either content or function_call must be provided.");
        }

        if (!silent) {
            printReceivedMessage(messageDict, sender);
        }
    }

    private void prepareChat(ConversableAgent recipient, boolean clearHistory) {
        resetConsecutiveAutoReplyCounter(recipient);
        recipient.resetConsecutiveAutoReplyCounter(this);
        replyAtReceive.put(recipient, true);
        recipient.getReplyAtReceive().put(this, true);

        if (clearHistory) {
            clearHistory(recipient);
            recipient.clearHistory(this);
        }
    }

    public void resetConsecutiveAutoReplyCounter(Agent sender) {
        if (sender == null) {
            consecutiveAutoReplyCounter.clear();
        } else {
            consecutiveAutoReplyCounter.put(sender, 0);
        }
    }

    public void clearHistory(Agent agent) {
        if (agent == null) {
            oaiMessages.clear();
        } else {
            if(oaiMessages.get(agent) != null) {
                oaiMessages.get(agent).clear();
            }
        }
    }

    public Map<String, StructuredTool> getToolMap() {
        return toolMap;
    }

    public void setToolMap(Map<String, StructuredTool> toolMap) {
        this.toolMap = toolMap;
    }

    public String getHumanInputMode() {
        return humanInputMode;
    }

    public void setHumanInputMode(String humanInputMode) {
        this.humanInputMode = humanInputMode;
    }

    public Integer getMaxConsecutiveAutoReply() {
        return maxConsecutiveAutoReply;
    }

    public void setMaxConsecutiveAutoReply(Integer maxConsecutiveAutoReply) {
        this.maxConsecutiveAutoReply = maxConsecutiveAutoReply;
    }

    public Map<Agent, Boolean> getReplyAtReceive() {
        return replyAtReceive;
    }

    public Map<Agent, List<Map<String, Object>>> getOaiMessages() {
        return oaiMessages;
    }

    public void setOaiMessages(Map<Agent, List<Map<String, Object>>> oaiMessages) {
        this.oaiMessages = oaiMessages;
    }

    public void setCodeEngine(CodeEngine codeEngine) {
        this.codeEngine = codeEngine;
    }

    public Predicate<Map<String, Object>> getIsTermination() {
        return isTermination;
    }

    public void setIsTermination(Predicate<Map<String, Object>> isTermination) {
        this.isTermination = isTermination;
    }
}
