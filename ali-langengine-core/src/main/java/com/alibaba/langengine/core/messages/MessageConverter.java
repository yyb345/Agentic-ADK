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
package com.alibaba.langengine.core.messages;

import ch.qos.logback.core.util.StringCollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessageConstant;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessageContent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Message转换器
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class MessageConverter {

    public static final String HUMAN_PREFIX = "Human";
    public static final String AI_PREFIX = "AI";
    public static final String SYSTEM_PREFIX = "System";
    public static final String FUNCTION_PREFIX = "Function";
    public static final String TOOL_PREFIX = "Tool";


    public static String getBufferString(List<BaseMessage> messages) {
        return getBufferString(messages, HUMAN_PREFIX, AI_PREFIX, SYSTEM_PREFIX, FUNCTION_PREFIX, TOOL_PREFIX);
    }
    public static String getBufferString(List<BaseMessage> messages,
                                         String humanPrefix,
                                         String aiPrefix,
                                         String systemPrefix,
                                         String functionPrefix,
                                         String toolPrefix) {
        List<String> stringMessages = new ArrayList<>();
        messages.forEach(message -> {
            String role;
            if(message instanceof HumanMessage) {
                role = humanPrefix;
            } else if (message instanceof AIMessage) {
                role = aiPrefix;
            } else if (message instanceof SystemMessage) {
                role = systemPrefix;
            } else if (message instanceof FunctionMessage) {
                role = functionPrefix;
            } else if (message instanceof ToolMessage) {
                role = toolPrefix;
            } else if (message instanceof com.alibaba.langengine.core.messages.ChatMessage) {
                role = ((com.alibaba.langengine.core.messages.ChatMessage) message).getRole();
            } else {
                throw new RuntimeException("Got unsupported message type: " + message.getType());
            }
            // 判断是否为空
            if(message.getContent() != null) {
                if (message.getContent().contains(role + ":")) {
                    stringMessages.add(message.getContent());
                } else if (message.getContent().startsWith(role)) {
                    stringMessages.add(message.getContent());
                } else {
                    stringMessages.add(String.format("%s: %s", role, message.getContent()));
                }
            }
        });
        return String.join("\n", stringMessages);
    }

    public static List<ChatMessage> convertMessageToChatMessage(List<BaseMessage> messages) {
        List<ChatMessage> chatMessages = messages.stream().map(MessageConverter::convertMessageToChatMessage).filter(Objects::nonNull).collect(Collectors.toList());
        return deduplicateMessages(chatMessages);
    }

    public static ChatMessage convertMessageToChatMessage(BaseMessage message) {
        // Assuming BaseMessage, ChatMessage, HumanMessage, AIMessage, SystemMessage, and FunctionMessage are classes
        // you have defined in Java

        ChatMessage chatMessage = new ChatMessage();

        if (message instanceof com.alibaba.langengine.core.messages.ChatMessage) {
            chatMessage.setRole(((com.alibaba.langengine.core.messages.ChatMessage)message).getRole());
            chatMessage.setContent(message.getContent());
        } else if (message instanceof HumanMessage) {
            chatMessage.setRole("user");
            chatMessage.setContent(message.getContent());
            if (message.getAdditionalKwargs() != null && message.getAdditionalKwargs().containsKey(ChatMessageConstant.CHAT_MESSAGE_CONTENTS_KEY)) {
                chatMessage.setContentWithPojo((List<ChatMessageContent>)(message.getAdditionalKwargs().get(ChatMessageConstant.CHAT_MESSAGE_CONTENTS_KEY)));
            }
        } else if (message instanceof MultiHumanMessage) {
            chatMessage.setRole("user");
            chatMessage.setContentWithPojo(((MultiHumanMessage) message).getChatMessageContent());
        }  else if (message instanceof MultiAIMessage) {
            chatMessage.setRole("assistant");
            chatMessage.setContentWithPojo(((MultiAIMessage) message).getChatMessageContent());
        } else if (message instanceof AIMessage) {
            // 后面看下要不要排除
            if(StringUtils.isEmpty(message.getContent()) && message.getAdditionalKwargs() == null) {
                log.warn("MessageConverter_content is null, message is {}", JSON.toJSONString(message));
                return null;
            }
            chatMessage.setRole("assistant");
            chatMessage.setContent(message.getContent());

            try {
                if(!StringUtils.isEmpty(message.getContent())) {
                    AgentAction agentAction = JSON.parseObject(message.getContent(), AgentAction.class);
                    if(agentAction != null) {
                        log.info("MessageContent is a agentAction, message is {}", message.getContent());
                        if(!CollectionUtils.isEmpty(agentAction.getActions())) {
                            String observation = agentAction.getActions().get(0).getObservation();
                            if(!StringUtils.isEmpty(observation)) {
                                chatMessage.setContent(observation);
                            }
                        } else if(!StringUtils.isEmpty(agentAction.getObservation())) {
                            String observation = agentAction.getObservation();
                            if(!StringUtils.isEmpty(observation)) {
                                chatMessage.setContent(observation);
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                log.info("MessageContent is not agentAction format and the error ignore, content is {}", message.getContent());
            }

            if (message.getAdditionalKwargs() != null && message.getAdditionalKwargs().containsKey("function_call")) {
                chatMessage.setFunctionCall((Map<String, Object>)(message.getAdditionalKwargs().get("function_call")));
                if(StringUtils.isEmpty(message.getContent())) {
                    chatMessage.setContent("");
                }
            }
            if (message.getAdditionalKwargs() != null && message.getAdditionalKwargs().containsKey("tool_calls")) {
                chatMessage.setToolCalls((List<Map<String, Object>>) (message.getAdditionalKwargs().get("tool_calls")));
//                if ("".equals(message.getContent())) {
//                    chatMessage.setContent(null);
//                }
                if(StringUtils.isEmpty(message.getContent())) {
                    chatMessage.setContent("");
                }
            }
            chatMessage.setPrefix(((AIMessage) message).getPrefix());
        } else if (message instanceof SystemMessage) {
            chatMessage.setRole("system");
            chatMessage.setContent(message.getContent());
        } else if (message instanceof ToolMessage) {
            chatMessage.setRole("tool");
            if(message.getContent() == null) {
                chatMessage.setContent("");
            } else {
                chatMessage.setContent(message.getContent());
            }
            chatMessage.setToolCallId(((ToolMessage) message).getTool_call_id());
        } else if (message instanceof FunctionMessage) {
            chatMessage.setRole("function");
            chatMessage.setContent(message.getContent());
            chatMessage.setName(((FunctionMessage) message).getName());
        } else if (message instanceof com.alibaba.langengine.core.messages.ChatMessage) {
            chatMessage.setRole(((com.alibaba.langengine.core.messages.ChatMessage)message).getRole());
            chatMessage.setContent(message.getContent());
        } else {
            throw new IllegalArgumentException("Got unknown type " + message.getClass().getSimpleName());
        }

        if (message.getAdditionalKwargs() != null && message.getAdditionalKwargs().containsKey("name")) {
            chatMessage.setName((String)message.getAdditionalKwargs().get("name"));
        }


        return chatMessage;
    }

    public static BaseMessage convertChatMessageToMessage(ChatMessage chatMessage) {
        if("assistant".equals(chatMessage.getRole())) {
            AIMessage aiMessage = new AIMessage();
            if(chatMessage.getFunctionCall() != null && !chatMessage.getFunctionCall().isEmpty()) {
                Map<String, Object> functionCallMap = new HashMap<>();
                functionCallMap.put("function_call", chatMessage.getFunctionCall());
                aiMessage.setToolUse(true);
                aiMessage.setAdditionalKwargs(functionCallMap);
            } else if(chatMessage.getToolCalls() != null && !chatMessage.getToolCalls().isEmpty()) {
                Map<String, Object> functionCallMap = new HashMap<>();
                functionCallMap.put("tool_calls", chatMessage.getToolCalls());
                aiMessage.setToolUse(true);
                aiMessage.setAdditionalKwargs(functionCallMap);
            }
//            else if(chatMessage.getToolCalls() != null && chatMessage.getToolCalls().size() > 0) {
//                Map<String, Object> functionCallMap = new HashMap<>();
//                functionCallMap.put("function_call", chatMessage.getFunctionCall());
//                aiMessage.setAdditionalKwargs(functionCallMap);
//            }
            else if(chatMessage.getContent() != null) {
                aiMessage.setContent(chatMessage.getContent().toString());
            }

            return aiMessage;
        }
        return null;
    }

    public static List<ChatMessage> deduplicateMessages(List<ChatMessage> chatMessages) {
        return chatMessages.stream()
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                    List<ChatMessage> result = new ArrayList<>();
                    ChatMessage previous = null;
                    for (ChatMessage current : list) {
                        if (previous == null ||
                                (previous.getRole() != null && !previous.getRole().equals(current.getRole())) ||
                                (previous.getContent() != null && !previous.getContent().equals(current.getContent()))) {
                            result.add(current);
                            previous = current;
                        }
                    }
                    return result;
                }));
    }

    public static void main(String[] args) {
        AIMessage message = new AIMessage();
        message.setContent("{\"actions\":[{\"observation\":\"{\\\"code\\\":0,\\\"data\\\":[{\\\"max_price\\\":450.0,\\\"webcode\\\":\\\"shopee\\\",\\\"min_price\\\":450.0,\\\"supply_status\\\":\\\"low\\\",\\\"planform_id\\\":\\\"29085717680\\\",\\\"group_id\\\":\\\"93dd166bceb26b6881431ffda223d72f\\\",\\\"cate_name_path_en\\\":[\\\"Women Clothes>>>Plus Size Clothes\\\"],\\\"id\\\":\\\"5039d6ac7b555af5cf35edcdb2b1dbfe\\\",\\\"sale_cnt\\\":1,\\\"title\\\":\\\"Bohemian Dress, long maxi dress with floral print in Bohemian style, Indian dress for curvy women, women's fashion clothing.\\\",\\\"country_id\\\":\\\"th\\\",\\\"scene\\\":\\\"hot_item\\\"},{\\\"max_price\\\":685.0,\\\"webcode\\\":\\\"shopee\\\",\\\"min_price\\\":553.0,\\\"supply_status\\\":\\\"low\\\",\\\"planform_id\\\":\\\"40400680512\\\",\\\"group_id\\\":\\\"e37f84dd6bf58ada60acb0264f9da0e6\\\",\\\"cate_name_path_en\\\":[\\\"Women Clothes>>>Dresses\\\"],\\\"id\\\":\\\"70ec95fa583124ebfd4b9c69c52971da\\\",\\\"sale_cnt\\\":0,\\\"title\\\":\\\"YUMEIREN Women's Clothing, Long Sleeve Mini Dress, Fashion Ruffles Fold V Neck Lace Up Holiday Dress\\\",\\\"country_id\\\":\\\"th\\\",\\\"scene\\\":\\\"hot_item\\\"},{\\\"max_price\\\":1946.0,\\\"webcode\\\":\\\"shopee\\\",\\\"min_price\\\":1946.0,\\\"supply_status\\\":\\\"low\\\",\\\"planform_id\\\":\\\"40650482250\\\",\\\"group_id\\\":\\\"74e2c5dd1472c75e8c04312af7c9f517\\\",\\\"cate_name_path_en\\\":[\\\"Women Clothes>>>Muslim Wear\\\"],\\\"id\\\":\\\"944168605aacf24170431f82758d4242\\\",\\\"sale_cnt\\\":0,\\\"title\\\":\\\"Women's Clothing Lady Satin Solid Dress with Belt Fashion Long Sleeve Elegant Long Party Dress Wearable Coat\\\",\\\"country_id\\\":\\\"th\\\",\\\"scene\\\":\\\"hot_item\\\"}],\\\"message\\\":\\\"\\\",\\\"request_id\\\":\\\"2151ffa217491675101293724d1b26\\\"}\",\"prevId\":\"\",\"tool\":\"360000000000244001\",\"toolInput\":\"{\\\"message\\\": \\\"Shopee泰国2025-05-10至2025-05-18 Dresses类目下女性商品数据\\\", \\\"sessionId\\\": \\\"\\\"}\"}],\"log\":\"<think>\\n好的，我现在需要处理用户的数据检索请求。首先，用户给了一个步骤，要求从Shopee平台的Dresses类目中，时间段是2025年5月10日到18日，区域是泰国，关键词是女性。根据之前的指示，我需要先确定是否需要调用工具。\\n\\n首先，用户的数据检索步骤明确提到了召回平台、类目、时间段和调研区域，这些都是必须包含在查询中的。关键词虽然用户提供了，但根据注意事项，Original Query必须包含召回平台、类目、时间段和区域，不能缺少。所以我的第一步是构建原始查询。\\n\\n原始查询应该按照用户给的格式，比如例子中的结构。用户给的例子是“Temu,Shopee,Shein马来西亚...”，所以这里应该用Shopee，泰国，时间段，类目，再加上关键词。不过根据注意事项里的说明，Original Query需要包含平台、类目、时间段、区域，而关键词可能可以作为补充，但不能省略前面的必要部分。\\n\\n所以，原始查询应该是：“Shopee泰国2025-05-10至2025-05-18 Dresses类目下女性商品数据”。这里需要确保平台、类目、时间段、区域都准确无误。\\n\\n接下来，检查是否需要调用工具。根据步骤中的内容，用户需要执行数据检索，因此必须调用工具。这里提供的工具是360000000000244001，也就是产品搜索功能。工具的参数需要message和可选的sessionId。用户没有提供sessionId，所以可能只需要message参数。\\n\\n现在，我需要构造第一个工具调用，使用原始查询作为message参数的值。调用工具后，如果返回结果为空或者错误，就需要进行第二次尝试，也就是广域查询。广域查询需要减少关键词的限制，但必须保留平台、类目、时间段和区域。例如，去掉“女性”这个关键词，但保留其他部分。所以第二次查询应该是：“Shopee泰国2025-05-10至2025-05-18 Dresses类目下商品数据”。\\n\\n如果第一次调用失败，执行第二次调用。如果第二次也失败，就终止并通知用户。现在需要先执行第一次调用。因此，生成对应的tool_call结构，使用原始查询作为message参数。然后等待工具返回结果，如果结果有效，继续处理；否则再试第二次。\\n\\n现在，根据用户提供的步骤，直接生成对应的tool_call。所以第一步是调用工具，参数message为原始查询的字符串。因此，正确的tool_call应该包含这些内容。\"}");

        String content = message.getContent();
        try {
            if(!StringUtils.isEmpty(message.getContent())) {
                AgentAction agentAction = JSON.parseObject(message.getContent(), AgentAction.class);
                if(agentAction != null) {
                    log.info("MessageContent is a agentAction, message is {}", message.getContent());
                    if(!CollectionUtils.isEmpty(agentAction.getActions())) {
                        String observation = agentAction.getActions().get(0).getObservation();
                        if(!StringUtils.isEmpty(observation)) {
                            content = observation;
                        }
                    } else if(!StringUtils.isEmpty(agentAction.getObservation())) {
                        String observation = agentAction.getObservation();
                        if(!StringUtils.isEmpty(observation)) {
                            content = observation;
                        }
                    }
                }
            }
        } catch (Throwable t) {
            log.info("MessageContent is not agentAction format and the error ignore, content is {}", message.getContent());
        }
        log.info(content);
    }
}
