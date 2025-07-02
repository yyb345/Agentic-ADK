package com.alibaba.langengine.core.dflow.agent.formatter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.memory.BaseChatMemory;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.ChatMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.messages.MessageConverter;
import com.alibaba.langengine.core.messages.SystemMessage;
import com.alibaba.langengine.core.messages.ToolMessage;
import com.alibaba.langengine.core.model.fastchat.runs.ToolCallFunction;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

public class HistoryInputFormatter implements Function<Object, String> {

    boolean isChatML = false;
    BaseChatMemory memory;

    public HistoryInputFormatter() {}

    public HistoryInputFormatter(BaseChatMemory memory, boolean isChatML) {
        this.isChatML = isChatML;
        this.memory = memory;
    }

    @Data
    public static class Message extends BaseMessage{
        String type;
    }
    public String apply(Object q) {
        List<BaseMessage> input = new ArrayList<>();
        if(q instanceof String){
            if(StringUtils.isBlank(q.toString())){
                return "";
            }
            List<Message> i = JSON.parseArray((String)q, Message.class);
            input.addAll(i);
        }else {
            input = (List<BaseMessage>)q;
        }
        if (!isChatML && memory != null) {
            return MessageConverter.getBufferString(input, memory.getHumanPrefix(), memory.getAiPrefix(),
                memory.getSystemPrefix(), null, memory.getToolPrefix());
        }
        StringBuilder sb = new StringBuilder();
        for (BaseMessage message : input) {
            if ("human".equals(message.getType())) {
                sb.append("<|im_start|>user\n" + message.getContent() + "\n<|im_end|>");
            } else if ("ai".equals(message.getType())) {
                sb.append("<|im_start|>assistant\n" + message.getContent() + "\n<|im_end|>");
            } else if ("system".equals(message.getType())) {
                sb.append("<|im_start|>system\n" + message.getContent() + "\n<|im_end|>");
            } else if ("chat".equals(message.getType())) {
                sb.append(
                    "<|im_start|>" + ((ChatMessage)message).getRole() + "\n" + message.getContent() + "\n<|im_end|>");
            } else if ("tool".equals(message.getType())) {
                sb.append(
                    "<|im_start|>user\n<tool_response>" + message.getContent() + "</tool_response><|im_end|>");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}